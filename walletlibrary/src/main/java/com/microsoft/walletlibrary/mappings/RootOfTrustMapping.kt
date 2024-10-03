package com.microsoft.walletlibrary.mappings

import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainMissing
import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainResult
import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainUnVerified
import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainVerified
import com.microsoft.walletlibrary.requests.RootOfTrust

/**
 * Maps LinkedDomainResult object from VC SDK to RootOfTrust object in library.
 */
internal fun RootOfTrust.toLinkedDomainResult(): LinkedDomainResult {
    return if (verified)
        LinkedDomainVerified(source)
    else if (source.isNotEmpty())
        LinkedDomainUnVerified(source)
    else
        LinkedDomainMissing
}

