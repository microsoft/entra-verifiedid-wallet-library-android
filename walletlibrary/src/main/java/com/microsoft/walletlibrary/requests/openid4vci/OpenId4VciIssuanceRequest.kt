package com.microsoft.walletlibrary.requests.openid4vci

import com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredential
import com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredentialContent
import com.microsoft.walletlibrary.did.sdk.crypto.protocols.jose.JwaCryptoHelper
import com.microsoft.walletlibrary.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.walletlibrary.did.sdk.util.controlflow.InvalidSignatureException
import com.microsoft.walletlibrary.did.sdk.util.controlflow.ValidatorException
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
import com.microsoft.walletlibrary.requests.requirements.OpenId4VCIPinRequirement
import com.microsoft.walletlibrary.requests.requirements.Requirement
import com.microsoft.walletlibrary.requests.styles.RequesterStyle
import com.microsoft.walletlibrary.requests.styles.VerifiedIdStyle
import com.microsoft.walletlibrary.util.LibraryConfiguration
import com.microsoft.walletlibrary.util.OpenId4VciRequestException
import com.microsoft.walletlibrary.util.OpenId4VciValidationException
import com.microsoft.walletlibrary.util.UserCanceledException
import com.microsoft.walletlibrary.util.VerifiedIdExceptions
import com.microsoft.walletlibrary.util.VerifiedIdResult
import com.microsoft.walletlibrary.util.getResult
import com.microsoft.walletlibrary.verifiedid.OpenId4VciVerifiedId
import com.microsoft.walletlibrary.verifiedid.VerifiedId
import com.microsoft.walletlibrary.wrapper.IdentifierDocumentResolver

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
            val response = formatAndSendIssuanceRequest()
            mapToVerifiedId(response)
        }
        return result
    }

    override fun isSatisfied(): Boolean {
        val validationResult = requirement.validate()
        return !validationResult.isFailure
    }

    override suspend fun cancel(message: String?): VerifiedIdResult<Unit> {
        return getResult {
            throw UserCanceledException(
                message ?: "User Canceled",
                VerifiedIdExceptions.USER_CANCELED_EXCEPTION.value
            )
        }
    }

    private suspend fun formatAndSendIssuanceRequest(): RawOpenID4VCIResponse {
        val accessToken = when (requirement) {
            is AccessTokenRequirement -> requirement.accessToken
            is OpenId4VCIPinRequirement -> requirement.accessToken
            else -> throw OpenId4VciValidationException(
                "Access token is missing in requirement.",
                VerifiedIdExceptions.REQUEST_CREATION_EXCEPTION.value
            )
        } ?: throw OpenId4VciValidationException(
            "Access token is missing in requirement.",
            VerifiedIdExceptions.REQUEST_CREATION_EXCEPTION.value
        )

        val credentialEndpoint = credentialMetadata.credentialEndpoint
            ?: throw OpenId4VciValidationException(
                "Credential endpoint is missing in credential metadata.",
                VerifiedIdExceptions.MALFORMED_CREDENTIAL_METADATA_EXCEPTION.value
            )

        val rawRequest = requestFormatter.format(credentialOffer, credentialEndpoint, accessToken)
        return sendIssuanceRequest(credentialEndpoint, rawRequest, accessToken)
    }

    private suspend fun sendIssuanceRequest(
        credentialEndpoint: String,
        rawRequest: RawOpenID4VCIRequest,
        accessToken: String
    ): RawOpenID4VCIResponse {
        PostOpenID4VCINetworkOperation(
            credentialEndpoint,
            libraryConfiguration.serializer.encodeToString(
                RawOpenID4VCIRequest.serializer(),
                rawRequest
            ),
            accessToken,
            libraryConfiguration.httpAgentApiProvider,
            libraryConfiguration.serializer
        ).fire()
            .onSuccess { response -> return response }
            .onFailure {
                throw OpenId4VciRequestException(
                    "Failed to send issuance request. ${it.message}",
                    VerifiedIdExceptions.REQUEST_SEND_EXCEPTION.value
                )
            }
        throw OpenId4VciRequestException(
            "Failed to send issuance request.",
            VerifiedIdExceptions.UNSPECIFIED_EXCEPTION.value
        )
    }

    private suspend fun mapToVerifiedId(rawResponse: RawOpenID4VCIResponse): VerifiedId {
        val issuerName =
            credentialMetadata.transformLocalizedIssuerDisplayDefinitionToRequesterStyle().name
        val credential = rawResponse.credential ?: throw OpenId4VciValidationException(
            "Credential is missing in response.",
            VerifiedIdExceptions.INVALID_PROPERTY_EXCEPTION.value
        )
        val raw = verifyAndUnWrapIssuanceResponse(credential)
        return OpenId4VciVerifiedId(
            raw = raw,
            issuerName = issuerName,
            credentialConfiguration = credentialConfiguration
        )
    }

    private suspend fun verifyAndUnWrapIssuanceResponse(jwsTokenString: String): VerifiableCredential {
        val jwsToken = JwsToken.deserialize(jwsTokenString)
        if (!verifySignature(jwsToken))
            throw InvalidSignatureException("Signature is not Valid on Issuance Response.")
        val verifiableCredentialContent = libraryConfiguration.serializer.decodeFromString(
            VerifiableCredentialContent.serializer(), jwsToken.content()
        )
        return VerifiableCredential(
            verifiableCredentialContent.jti,
            jwsTokenString,
            verifiableCredentialContent
        )
    }

    private suspend fun verifySignature(jwsToken: JwsToken): Boolean {
        val kid = jwsToken.keyId ?: throw ValidatorException("JWS contains no key id")
        val (didInHeader: String?, keyIdInHeader: String) = getDidAndKeyIdFromHeader(kid)
        if (didInHeader == null) throw ValidatorException("JWS contains no DID")
        val identifierDocument = IdentifierDocumentResolver.resolveIdentifierDocument(didInHeader)
        val publicKeys = identifierDocument.verificationMethod
        if (publicKeys.isNullOrEmpty()) throw ValidatorException("No public key found in identifier document")
        val publicKeysJwk =
            publicKeys.filter { publicKey -> getDidAndKeyIdFromHeader(publicKey.id).second == keyIdInHeader }
                .map { it.publicKeyJwk }
        return jwsToken.verify(publicKeysJwk)
    }

    private fun getDidAndKeyIdFromHeader(kid: String): Pair<String?, String> {
        return JwaCryptoHelper.extractDidAndKeyId(kid)
    }
}