package com.microsoft.walletlibrary.requests.handlers

import com.microsoft.walletlibrary.did.sdk.identifier.resolvers.RootOfTrustResolver
import com.microsoft.walletlibrary.networking.entities.openid4vci.credentialmetadata.CredentialConfiguration
import com.microsoft.walletlibrary.networking.entities.openid4vci.credentialmetadata.CredentialMetadata
import com.microsoft.walletlibrary.networking.entities.openid4vci.credentialoffer.CredentialOffer
import com.microsoft.walletlibrary.networking.entities.openid4vci.credentialoffer.CredentialOfferGrant
import com.microsoft.walletlibrary.networking.operations.FetchCredentialMetadataNetworkOperation
import com.microsoft.walletlibrary.networking.operations.FetchOpenIdWellKnownConfigNetworkOperation
import com.microsoft.walletlibrary.requests.RootOfTrust
import com.microsoft.walletlibrary.requests.VerifiedIdRequest
import com.microsoft.walletlibrary.requests.openid4vci.OpenId4VciIssuanceRequest
import com.microsoft.walletlibrary.requests.requirements.AccessTokenRequirement
import com.microsoft.walletlibrary.requests.requirements.GroupRequirement
import com.microsoft.walletlibrary.requests.requirements.GroupRequirementOperator
import com.microsoft.walletlibrary.requests.requirements.OpenId4VCIPinRequirement
import com.microsoft.walletlibrary.requests.requirements.Requirement
import com.microsoft.walletlibrary.requests.resolvers.OpenID4VCIPreAuthAccessTokenResolver
import com.microsoft.walletlibrary.util.LibraryConfiguration
import com.microsoft.walletlibrary.util.OpenId4VciRequestException
import com.microsoft.walletlibrary.util.OpenId4VciValidationException
import com.microsoft.walletlibrary.util.VerifiedIdExceptions

internal class OpenId4VCIRequestHandler(
    private val libraryConfiguration: LibraryConfiguration,
    private val signedMetadataProcessor: SignedMetadataProcessor = SignedMetadataProcessor(
        libraryConfiguration
    )
) : RequestHandler {
    companion object {
        const val WELL_KNOWN_CONFIGURATION_SUFFIX = ".well-known/openid-configuration"
    }

    // Indicates whether the provided raw request can be handled by this handler.
    // This method checks if the raw request can be cast to CredentialOffer successfully, and if it contains the required fields.
    override fun canHandle(rawRequest: Any): Boolean {
        return try {
            libraryConfiguration.serializer.decodeFromString(
                CredentialOffer.serializer(),
                rawRequest as String
            )
            true
        } catch (e: Exception) {
            false
        }
    }

    // Handle and process the provided raw request and returns a VerifiedIdRequest.
    override suspend fun handleRequest(rawRequest: Any, rootOfTrustResolver: RootOfTrustResolver?): VerifiedIdRequest<*> {
        val credentialOffer = decodeCredentialOffer(rawRequest)

        // Fetch the credential metadata from the credential issuer in credential offer object.
        fetchCredentialMetadata(credentialOffer.credential_issuer)
            .onSuccess { credentialMetadata ->
                validateCredentialMetadata(credentialMetadata, credentialOffer)
                val supportedCredentialConfigurationId = getSupportedCredentialConfigurationId(
                    credentialMetadata,
                    credentialOffer
                )

                // Get the root of trust from the signed metadata.
                val rootOfTrust = credentialMetadata.signedMetadata?.let {
                    signedMetadataProcessor.process(it, credentialOffer.credential_issuer, rootOfTrustResolver)
                } ?: RootOfTrust("", false)

                return transformToVerifiedIdRequest(
                    credentialMetadata,
                    supportedCredentialConfigurationId,
                    credentialOffer,
                    rootOfTrust
                )
            }
            .onFailure {
                throw OpenId4VciRequestException(
                    "Failed to fetch credential metadata ${it.message}",
                    VerifiedIdExceptions.CREDENTIAL_METADATA_FETCH_EXCEPTION.value,
                    it as Exception
                )
            }
        throw OpenId4VciValidationException(
            "Failed to validate or transform credential metadata",
            VerifiedIdExceptions.MALFORMED_CREDENTIAL_METADATA_EXCEPTION.value
        )
    }

    private fun decodeCredentialOffer(rawRequest: Any): CredentialOffer {
        try {
            // Deserialize the raw request to a CredentialOffer object.
            return libraryConfiguration.serializer.decodeFromString(
                CredentialOffer.serializer(),
                rawRequest as String
            )
        } catch (exception: Exception) {
            throw OpenId4VciValidationException(
                "Failed to decode CredentialOffer ${exception.message}",
                VerifiedIdExceptions.MALFORMED_CREDENTIAL_OFFER_EXCEPTION.value,
                exception
            )
        }
    }

    private fun getSupportedCredentialConfigurationId(
        credentialMetadata: CredentialMetadata,
        credentialOffer: CredentialOffer
    ): CredentialConfiguration {
        // Get only the supported credential configuration ids from the credential metadata from the list in credential offer.
        val configIds = credentialOffer.credential_configuration_ids
        val supportedCredentialConfigurationIds =
            credentialMetadata.getSupportedCredentialConfigurations(configIds)
        if (supportedCredentialConfigurationIds.isEmpty())
            throw OpenId4VciValidationException(
                "Request does not contain supported credential configuration.",
                VerifiedIdExceptions.MALFORMED_CREDENTIAL_METADATA_EXCEPTION.value
            )
        return supportedCredentialConfigurationIds.first()
    }

    private suspend fun transformToVerifiedIdRequest(
        credentialMetadata: CredentialMetadata,
        credentialConfiguration: CredentialConfiguration,
        credentialOffer: CredentialOffer,
        rootOfTrust: RootOfTrust
    ): VerifiedIdRequest<*> {
        val requesterStyle =
            credentialMetadata.transformLocalizedIssuerDisplayDefinitionToRequesterStyle()
        val verifiedIdStyle =
            credentialConfiguration.getVerifiedIdStyleInPreferredLocale(requesterStyle.name)

        // By this point, we know credential issuer is not null.
        val accessTokenEndpoint = fetchAccessTokenEndpointFromOpenIdWellKnownConfig(
            credentialMetadata.credentialIssuer!!
        )
        val requirement = transformToRequirement(
            credentialConfiguration.scope,
            credentialOffer,
            accessTokenEndpoint
        )
        return OpenId4VciIssuanceRequest(
            requesterStyle,
            requirement,
            rootOfTrust,
            verifiedIdStyle,
            credentialOffer,
            credentialMetadata,
            credentialConfiguration,
            libraryConfiguration
        )
    }

    private fun validateCredentialMetadata(
        credentialMetadata: CredentialMetadata,
        credentialOffer: CredentialOffer
    ) {
        // Validate Credential Metadata to verify if credential issuer and Signed Metadata exist.
        credentialMetadata.verifyIfCredentialIssuerExist()
        credentialMetadata.verifyIfSignedMetadataExist()

        // Validate the authorization servers in the credential metadata.
        credentialMetadata.validateAuthorizationServers(credentialOffer)
    }

    private suspend fun fetchAccessTokenEndpointFromOpenIdWellKnownConfig(credentialIssuer: String): String {
        val openIdWellKnownUrl = "$credentialIssuer/$WELL_KNOWN_CONFIGURATION_SUFFIX"
        FetchOpenIdWellKnownConfigNetworkOperation(
            openIdWellKnownUrl,
            libraryConfiguration.httpAgentApiProvider,
            libraryConfiguration.serializer
        ).fire()
            .onSuccess { return it.token_endpoint }
            .onFailure {
                throw OpenId4VciRequestException(
                    "Failed to fetch OpenId well-known configuration ${it.message}",
                    VerifiedIdExceptions.OPENID_WELL_KNOWN_CONFIG_FETCH_EXCEPTION.value,
                    it as Exception
                )
            }
        throw OpenId4VciRequestException(
            "Failed to fetch OpenId well-known configuration.",
            VerifiedIdExceptions.OPENID_WELL_KNOWN_CONFIG_FETCH_EXCEPTION.value
        )
    }

    private suspend fun transformToRequirement(
        scope: String?,
        credentialOffer: CredentialOffer,
        accessTokenEndpoint: String
    ): Requirement {
        val requirements = mutableListOf<Requirement>()
        var grant = credentialOffer.grants["authorization_code"]
        grant?.let { requirements.add(transformToAccessTokenRequirement(it, scope)) }

        grant = credentialOffer.grants["urn:ietf:params:oauth:grant-type:pre-authorized_code"]
        grant?.let { requirements.add(transformToPreAuthRequirement(it, accessTokenEndpoint)) }

        return if (requirements.isEmpty()) {
            throw OpenId4VciValidationException(
                "No grants defined in credential offer.",
                VerifiedIdExceptions.MALFORMED_CREDENTIAL_OFFER_EXCEPTION.value
            )
        } else if (requirements.size == 1) {
            requirements.first()
        } else {
            GroupRequirement(true, requirements, GroupRequirementOperator.ALL)
        }
    }

    private fun transformToAccessTokenRequirement(
        grant: CredentialOfferGrant,
        scope: String?
    ): AccessTokenRequirement {
        if (scope == null) {
            throw OpenId4VciValidationException(
                "Credential configuration in credential metadata doesn't contain scope value.",
                VerifiedIdExceptions.MALFORMED_CREDENTIAL_METADATA_EXCEPTION.value
            )
        }
        return AccessTokenRequirement(
            "",
            configuration = grant.authorizationServer,
            resourceId = scope,
            scope = "$scope/.default",
            claims = emptyList()
        )
    }

    private suspend fun transformToPreAuthRequirement(
        grant: CredentialOfferGrant,
        accessTokenEndpoint: String
    ): Requirement {
        val pinDetails = grant.txCode
        return if (pinDetails == null) {
            val openId4VCIPinRequirement = OpenId4VCIPinRequirement()
            openId4VCIPinRequirement.libraryConfiguration = libraryConfiguration
            openId4VCIPinRequirement.accessTokenEndpoint = accessTokenEndpoint
            openId4VCIPinRequirement.preAuthorizedCode = grant.preAuthorizedCode
            preValidatePinRequirement(openId4VCIPinRequirement)
            openId4VCIPinRequirement
        } else {
            val openId4VCIPinRequirement =
                OpenId4VCIPinRequirement(length = pinDetails.length, type = pinDetails.inputMode)
            openId4VCIPinRequirement.libraryConfiguration = libraryConfiguration
            openId4VCIPinRequirement.accessTokenEndpoint = accessTokenEndpoint
            openId4VCIPinRequirement.preAuthorizedCode = grant.preAuthorizedCode
            openId4VCIPinRequirement
        }
    }

    private suspend fun fetchCredentialMetadata(metadataUrl: String): Result<CredentialMetadata> {
        val credentialMetadataUrl = buildCredentialMetadataUrl(metadataUrl)
        return FetchCredentialMetadataNetworkOperation(
            credentialMetadataUrl,
            libraryConfiguration.httpAgentApiProvider,
            libraryConfiguration.serializer
        ).fire()
    }

    // Build the credential metadata url from the provided credential issuer.
    private fun buildCredentialMetadataUrl(credentialIssuer: String): String {
        val suffix = "/.well-known/openid-credential-issuer"
        if (!credentialIssuer.endsWith(suffix))
            return credentialIssuer + suffix
        return credentialIssuer
    }

    private suspend fun preValidatePinRequirement(openId4VCIPinRequirement: OpenId4VCIPinRequirement) {
        OpenID4VCIPreAuthAccessTokenResolver(libraryConfiguration).resolve(
            openId4VCIPinRequirement.preAuthorizedCode,
            openId4VCIPinRequirement,
            openId4VCIPinRequirement.accessTokenEndpoint!!
        )
    }
}