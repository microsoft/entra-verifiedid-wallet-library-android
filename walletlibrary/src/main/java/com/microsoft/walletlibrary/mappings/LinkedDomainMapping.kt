/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.mappings

import com.microsoft.did.sdk.credential.service.models.linkedDomains.LinkedDomainMissing
import com.microsoft.did.sdk.credential.service.models.linkedDomains.LinkedDomainResult
import com.microsoft.did.sdk.credential.service.models.linkedDomains.LinkedDomainUnVerified
import com.microsoft.did.sdk.credential.service.models.linkedDomains.LinkedDomainVerified
import com.microsoft.walletlibrary.requests.RootOfTrust

internal fun LinkedDomainResult.toRootOfTrust(): RootOfTrust {
    return when (this) {
        is LinkedDomainVerified -> RootOfTrust(domainUrl, true)
        is LinkedDomainUnVerified -> RootOfTrust(domainUrl, false)
        is LinkedDomainMissing -> RootOfTrust("", false)
    }
}