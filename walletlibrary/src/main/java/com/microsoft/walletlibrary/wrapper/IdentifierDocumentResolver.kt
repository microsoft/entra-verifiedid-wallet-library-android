package com.microsoft.walletlibrary.wrapper

import com.microsoft.walletlibrary.did.sdk.VerifiableCredentialSdk
import com.microsoft.walletlibrary.did.sdk.identifier.models.identifierdocument.IdentifierDocument
import com.microsoft.walletlibrary.util.IdentifierDocumentResolutionException

/**
 * Wrapper class to wrap the resolve identifier from VC SDK and return identifier document.
 */
internal object IdentifierDocumentResolver {

    internal suspend fun resolveIdentifierDocument(did: String): IdentifierDocument {
        val identifierDocumentResult =
            VerifiableCredentialSdk.linkedDomainsService.resolveIdentifierDocument(did)
        return handleResolutionResult(identifierDocumentResult)
    }

    private fun handleResolutionResult(identifierDocumentResult: Result<IdentifierDocument>): IdentifierDocument {
        identifierDocumentResult
            .onSuccess {
                return it
            }
            .onFailure {
                throw IdentifierDocumentResolutionException(
                    "Unable to fetch identifier document",
                    it
                )
            }
        throw IdentifierDocumentResolutionException(
            "Unable to fetch identifier document"
        )
    }
}