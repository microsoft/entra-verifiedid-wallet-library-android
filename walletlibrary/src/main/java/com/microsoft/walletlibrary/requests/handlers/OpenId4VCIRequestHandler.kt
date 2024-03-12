package com.microsoft.walletlibrary.requests.handlers

import com.microsoft.walletlibrary.networking.entities.openid4vci.credentialmetadata.CredentialMetadata
import com.microsoft.walletlibrary.networking.entities.openid4vci.credentialoffer.CredentialOffer
import com.microsoft.walletlibrary.networking.operations.FetchCredentialMetadataNetworkOperation
import com.microsoft.walletlibrary.requests.RootOfTrust
import com.microsoft.walletlibrary.requests.VerifiedIdRequest
import com.microsoft.walletlibrary.util.LibraryConfiguration
import com.microsoft.walletlibrary.util.OpenId4VciRequestException
import com.microsoft.walletlibrary.util.OpenId4VciValidationException
import com.microsoft.walletlibrary.util.VerifiedIdExceptions

internal class OpenId4VCIRequestHandler(private val libraryConfiguration: LibraryConfiguration) :
    RequestHandler {
    private val signedMetadataProcessor = SignedMetadataProcessor(libraryConfiguration)

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
    override suspend fun handleRequest(rawRequest: Any): VerifiedIdRequest<*> {
        val credentialOffer: CredentialOffer
        try {
            // Deserialize the raw request to a CredentialOffer object.
            credentialOffer = libraryConfiguration.serializer.decodeFromString(
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

        // Fetch the credential metadata from the credential issuer in credential offer object.
        fetchCredentialMetadata(credentialOffer.credential_issuer)
            .onSuccess { credentialMetadata ->
                // Validate Credential Metadata and Signed Metadata.
                credentialMetadata.validateCredentialMetadataAndSignedMetadata(credentialMetadata)

                // Get only the supported credential configuration ids from the credential metadata from the list in credential offer.
                val configIds = credentialOffer.credential_configuration_ids
                val supportedCredentialConfigurationId =
                    credentialMetadata.getSupportedCredentialConfigurations(configIds).first()

                // Validate the authorization servers in the credential metadata.
                credentialMetadata.validateAuthorizationServers(credentialOffer)

                // Get the root of trust from the signed metadata.
                val rootOfTrust = credentialMetadata.signed_metadata?.let {
                    getRootOfTrust(
                        it,
                        credentialOffer.credential_issuer
                    )
                }
                val requesterStyle = credentialMetadata.transformLocalizedIssuerDisplayDefinitionToRequesterStyle()
                val verifiedIdStyle = supportedCredentialConfigurationId.transformDisplayToVerifiedIdStyle(requesterStyle.name)
            }
            .onFailure {
                throw OpenId4VciRequestException(
                    "Failed to fetch credential metadata ${it.message}",
                    VerifiedIdExceptions.CREDENTIAL_METADATA_FETCH_EXCEPTION.value,
                    it as Exception
                )
            }
        TODO("Map data models and finally return VerifiedIdRequest.")
    }

    private suspend fun fetchCredentialMetadata(metadataUrl: String): Result<CredentialMetadata> {
        val credentialMetadataUrl = buildCredentialMetadataUrl(metadataUrl)
        return FetchCredentialMetadataNetworkOperation(
            credentialMetadataUrl,
            libraryConfiguration.httpAgentApiProvider,
            libraryConfiguration.serializer
        ).fire()
    }

    private fun buildCredentialMetadataUrl(credentialIssuer: String): String {
        val suffix = "/.well-known/openid-credential-issuer"
        if (!credentialIssuer.endsWith(suffix))
            return credentialIssuer + suffix
        return credentialIssuer
    }

    private suspend fun getRootOfTrust(
        signedMetadata: String,
        credentialIssuer: String
    ): RootOfTrust {
        return signedMetadataProcessor.process(signedMetadata, credentialIssuer)
    }
}