package com.microsoft.walletlibrary.wrapper

import com.microsoft.walletlibrary.did.sdk.VerifiableCredentialSdk
import com.microsoft.walletlibrary.did.sdk.identifier.models.identifierdocument.IdentifierDocument
import com.microsoft.walletlibrary.mappings.fetchAndVerifyLinkedDomains
import com.microsoft.walletlibrary.mappings.toRootOfTrust
import com.microsoft.walletlibrary.requests.RootOfTrust

internal object RootOfTrustResolver {

    internal suspend fun resolveRootOfTrust(identifierDocument: IdentifierDocument): RootOfTrust {
        VerifiableCredentialSdk.linkedDomainsService.fetchAndVerifyLinkedDomains(identifierDocument)
            .onSuccess { return it.toRootOfTrust() }
            .onFailure { return RootOfTrust("", false) }
        return RootOfTrust("", false)
    }
}