package com.microsoft.walletlibrary.wrapper

import com.microsoft.walletlibrary.did.sdk.VerifiableCredentialSdk
import com.microsoft.walletlibrary.did.sdk.identifier.models.identifierdocument.IdentifierDocument

internal object IdentifierDocumentResolver {

    internal suspend fun resolveIdentifierDocument(did: String): Result<IdentifierDocument> {
        return VerifiableCredentialSdk.linkedDomainsService.resolveIdentifierDocument(did)
    }
}