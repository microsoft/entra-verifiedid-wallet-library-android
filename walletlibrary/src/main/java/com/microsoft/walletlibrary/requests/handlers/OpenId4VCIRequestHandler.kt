package com.microsoft.walletlibrary.requests.handlers

import com.microsoft.walletlibrary.networking.entities.openid4vci.credentialmetadata.CredentialMetadata
import com.microsoft.walletlibrary.networking.entities.openid4vci.credentialoffer.CredentialOffer
import com.microsoft.walletlibrary.networking.operations.FetchCredentialMetadataNetworkOperation
import com.microsoft.walletlibrary.requests.VerifiedIdRequest
import com.microsoft.walletlibrary.util.LibraryConfiguration
import com.microsoft.walletlibrary.util.OpenId4VciException
import com.microsoft.walletlibrary.util.VerifiedIdExceptions

internal class OpenId4VCIRequestHandler(private val libraryConfiguration: LibraryConfiguration) :
    RequestHandler {
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
            credentialOffer = libraryConfiguration.serializer.decodeFromString(
                CredentialOffer.serializer(),
                rawRequest as String
            )
        } catch (exception: Exception) {
            throw OpenId4VciException(
                "Failed to decode CredentialOffer ${exception.message}",
                VerifiedIdExceptions.MALFORMED_CREDENTIAL_OFFER_EXCEPTION.value,
                exception
            )
        }
        fetchCredentialMetadata(credentialOffer.credential_issuer)
            .onSuccess { }
            .onFailure { }
        TODO(
            "Validate credential metadata, gather more inform, map data models and " +
                    "finally return VerifiedIdRequest."
        )
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
}