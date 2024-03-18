package com.microsoft.walletlibrary.mappings

import com.microsoft.walletlibrary.did.sdk.LinkedDomainsService
import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainResult
import com.microsoft.walletlibrary.did.sdk.identifier.models.identifierdocument.IdentifierDocument
import com.microsoft.walletlibrary.did.sdk.util.controlflow.SdkException

internal suspend fun LinkedDomainsService.fetchAndVerifyLinkedDomains(identifierDocument: IdentifierDocument): Result<LinkedDomainResult> {
    val linkedDomains = getLinkedDomainsFromDidDocument(identifierDocument)
    verifyLinkedDomains(linkedDomains, identifierDocument.id)
        .onSuccess { return Result.success(it) }
        .onFailure { return Result.failure(it) }
    return Result.failure(SdkException("Failed while verifying linked domains"))
}