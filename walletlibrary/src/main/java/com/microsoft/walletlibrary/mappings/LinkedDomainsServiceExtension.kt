package com.microsoft.walletlibrary.mappings

import com.microsoft.walletlibrary.did.sdk.LinkedDomainsService
import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainResult
import com.microsoft.walletlibrary.did.sdk.identifier.models.identifierdocument.IdentifierDocument
import com.microsoft.walletlibrary.did.sdk.identifier.resolvers.RootOfTrustResolver
import com.microsoft.walletlibrary.did.sdk.util.controlflow.SdkException

internal suspend fun LinkedDomainsService.fetchAndVerifyLinkedDomains(
    identifierDocument: IdentifierDocument,
    rootOfTrustResolver: RootOfTrustResolver? = null
): Result<LinkedDomainResult> {
    fetchAndVerifyLinkedDomains(identifierDocument.id, rootOfTrustResolver)
        .onSuccess { return Result.success(it) }
        .onFailure { return Result.failure(it) }
    return Result.failure(SdkException("Failed while verifying linked domains"))
}