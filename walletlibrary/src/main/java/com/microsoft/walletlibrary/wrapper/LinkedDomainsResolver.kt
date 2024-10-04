package com.microsoft.walletlibrary.wrapper

import com.microsoft.walletlibrary.did.sdk.VerifiableCredentialSdk
import com.microsoft.walletlibrary.did.sdk.identifier.models.identifierdocument.DidMetadata
import com.microsoft.walletlibrary.did.sdk.identifier.models.identifierdocument.IdentifierDocument
import com.microsoft.walletlibrary.did.sdk.identifier.resolvers.RootOfTrustResolver
import com.microsoft.walletlibrary.mappings.toRootOfTrust
import com.microsoft.walletlibrary.requests.RootOfTrust
import com.microsoft.walletlibrary.util.MalformedInputException
import com.microsoft.walletlibrary.util.VerifiedIdExceptions

/**
 * Wrapper class to wrap the fetch linked domains from VC SDK and return RootOfTrust.
 */
internal object LinkedDomainsResolver : RootOfTrustResolver {

/*    internal suspend fun resolveRootOfTrust(identifierDocument: IdentifierDocument): RootOfTrust {
        VerifiableCredentialSdk.linkedDomainsService.fetchAndVerifyLinkedDomains(identifierDocument)
            .onSuccess { return it.toRootOfTrust() }
            .onFailure { return RootOfTrust("", false) }
        return RootOfTrust("", false)
    }*/

    override suspend fun resolve(didMetadata: DidMetadata): RootOfTrust {
        if (didMetadata !is IdentifierDocument) {
            throw MalformedInputException(
                "Expected Identifier Document to resolve Root of Trust",
                VerifiedIdExceptions.MALFORMED_INPUT_EXCEPTION.value
            )
        }
        VerifiableCredentialSdk.linkedDomainsService.validateLinkedDomains(didMetadata)
            .map { it.toRootOfTrust() }
            .onSuccess { return it }
            .onFailure { return RootOfTrust("", false) }
        return RootOfTrust("", false)
    }
}