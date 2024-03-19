package com.microsoft.walletlibrary.requests.openid4vci

import com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredential
import com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredentialContent
import com.microsoft.walletlibrary.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.walletlibrary.did.sdk.util.controlflow.InvalidSignatureException
import com.microsoft.walletlibrary.networking.entities.openid4vci.RawOpenID4VCIResponse
import com.microsoft.walletlibrary.networking.entities.openid4vci.credentialmetadata.CredentialConfiguration
import com.microsoft.walletlibrary.networking.entities.openid4vci.credentialmetadata.CredentialMetadata
import com.microsoft.walletlibrary.networking.entities.openid4vci.credentialoffer.CredentialOffer
import com.microsoft.walletlibrary.networking.entities.openid4vci.request.RawOpenID4VCIRequest
import com.microsoft.walletlibrary.networking.formatters.OpenId4VciIssuanceRequestFormatter
import com.microsoft.walletlibrary.networking.operations.PostOpenID4VCINetworkOperation
import com.microsoft.walletlibrary.requests.RootOfTrust
import com.microsoft.walletlibrary.requests.VerifiedIdIssuanceRequest
import com.microsoft.walletlibrary.requests.requirements.AccessTokenRequirement
import com.microsoft.walletlibrary.requests.requirements.Requirement
import com.microsoft.walletlibrary.requests.styles.RequesterStyle
import com.microsoft.walletlibrary.requests.styles.VerifiedIdStyle
import com.microsoft.walletlibrary.util.LibraryConfiguration
import com.microsoft.walletlibrary.util.OpenId4VciRequestException
import com.microsoft.walletlibrary.util.OpenId4VciValidationException
import com.microsoft.walletlibrary.util.VerifiedIdExceptions
import com.microsoft.walletlibrary.util.VerifiedIdResult
import com.microsoft.walletlibrary.util.getResult
import com.microsoft.walletlibrary.verifiedid.OpenId4VciVerifiedId
import com.microsoft.walletlibrary.verifiedid.VerifiedId

internal class OpenId4VciIssuanceRequest(
    // Attributes describing the requester (eg. name, logo).
    override val requesterStyle: RequesterStyle,

    // Information describing the requirements needed to complete the flow.
    override val requirement: Requirement,

    // Root of trust of the requester (eg. linked domains).
    override val rootOfTrust: RootOfTrust,

    // Attributes describing the Verified ID (eg. name, issuer, logo, background and text colors).
    override val verifiedIdStyle: VerifiedIdStyle,

    private val credentialOffer: CredentialOffer,

    private val credentialMetadata: CredentialMetadata,

    private val credentialConfiguration: CredentialConfiguration,

    private val libraryConfiguration: LibraryConfiguration
) : VerifiedIdIssuanceRequest {

    private val requestFormatter = OpenId4VciIssuanceRequestFormatter(libraryConfiguration)

    override suspend fun complete(): VerifiedIdResult<VerifiedId> {
        val result = getResult {
            val response = sendIssuanceRequest()
            mapToVerifiedId(response)
        }
        return result
    }

    override fun isSatisfied(): Boolean {
        val validationResult = requirement.validate()
        // TODO("Add logging")
        return !validationResult.isFailure
    }

    override suspend fun cancel(message: String?): VerifiedIdResult<Unit> {
        TODO("Not yet implemented")
    }

    private suspend fun sendIssuanceRequest(): RawOpenID4VCIResponse {
        val accessToken =
            (requirement as? AccessTokenRequirement)?.accessToken
                ?: throw OpenId4VciValidationException("Access token is missing in requirement.", "accesstoken-missing")

        val credentialEndpoint = credentialMetadata.credential_endpoint
            ?: throw OpenId4VciValidationException(
                "Credential endpoint is missing in credential metadata.",
                VerifiedIdExceptions.MALFORMED_CREDENTIAL_METADATA_EXCEPTION.value
            )

        val rawRequest = requestFormatter.format(credentialOffer, credentialEndpoint, accessToken)
        PostOpenID4VCINetworkOperation(
            credentialEndpoint,
            libraryConfiguration.serializer.encodeToString(RawOpenID4VCIRequest.serializer(), rawRequest),
            accessToken,
            libraryConfiguration.httpAgentApiProvider,
            libraryConfiguration.serializer
        ).fire().onSuccess { response -> return response }
            .onFailure {
                throw OpenId4VciRequestException("Failed to send issuance request. ${it.message}", "post-failure")
            }
        throw OpenId4VciRequestException("Failed to send issuance request.", "unknown")
    }

    private fun mapToVerifiedId(rawResponse: RawOpenID4VCIResponse): VerifiedId {
        val issuerName = credentialMetadata.transformLocalizedIssuerDisplayDefinitionToRequesterStyle().name
        val credential = rawResponse.credential ?: throw OpenId4VciValidationException(
            "Credential is missing in response.",
            "No credential"
        )
        val raw = verifyAndUnWrapIssuanceResponse(credential)
        return OpenId4VciVerifiedId(
            raw = raw,
            issuerName = issuerName,
            credentialConfiguration = credentialConfiguration
        )
    }

    private fun verifyAndUnWrapIssuanceResponse(jwsTokenString: String): VerifiableCredential {
        val jwsToken = JwsToken.deserialize(jwsTokenString)
/*        if (!jwtValidator.verifySignature(jwsToken))
            throw InvalidSignatureException("Signature is not Valid on Issuance Response.")*/
        val verifiableCredentialContent = libraryConfiguration.serializer.decodeFromString(VerifiableCredentialContent.serializer(), jwsToken.content())
        return VerifiableCredential(verifiableCredentialContent.jti, jwsTokenString, verifiableCredentialContent)
    }
}