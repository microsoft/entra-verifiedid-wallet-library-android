package com.microsoft.walletlibrary.mappings

import com.microsoft.did.sdk.credential.service.models.linkedDomains.LinkedDomainResult
import com.microsoft.did.sdk.credential.service.models.linkedDomains.LinkedDomainUnVerified
import com.microsoft.did.sdk.credential.service.models.linkedDomains.LinkedDomainVerified
import com.microsoft.walletlibrary.requests.RootOfTrust

fun LinkedDomainResult.toRootOfTrust(): RootOfTrust {
    var source = ""
    var verificationStatus = false
    if (this is LinkedDomainVerified) {
        source = domainUrl
        verificationStatus = true
    } else if (this is LinkedDomainUnVerified)
        source = domainUrl
    return RootOfTrust(source, verificationStatus)
}