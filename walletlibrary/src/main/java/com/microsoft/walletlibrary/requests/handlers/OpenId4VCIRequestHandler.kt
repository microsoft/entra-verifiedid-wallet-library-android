package com.microsoft.walletlibrary.requests.handlers

import com.microsoft.walletlibrary.networking.entities.openid4vci.credentialoffer.CredentialOffer
import com.microsoft.walletlibrary.networking.operations.FetchCredentialMetadataNetworkOperation
import com.microsoft.walletlibrary.requests.VerifiedIdRequest
import com.microsoft.walletlibrary.util.LibraryConfiguration

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
        val credentialOffer = libraryConfiguration.serializer.decodeFromString(
            CredentialOffer.serializer(),
            rawRequest as String
        )
        fetchCredentialMetadata(credentialOffer.credential_issuer)
            .onSuccess { }
            .onFailure { }
        TODO("Not yet implemented")
    }

    private suspend fun fetchCredentialMetadata(metadataUrl: String) =
        FetchCredentialMetadataNetworkOperation(
            metadataUrl,
            libraryConfiguration.httpAgentApiProvider,
            libraryConfiguration.serializer
        ).fire()
}