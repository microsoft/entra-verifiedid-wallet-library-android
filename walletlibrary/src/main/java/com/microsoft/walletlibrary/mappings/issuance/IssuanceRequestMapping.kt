/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.mappings.issuance

import com.microsoft.did.sdk.credential.service.IssuanceRequest
import com.microsoft.walletlibrary.mappings.toRootOfTrust
import com.microsoft.walletlibrary.requests.ManifestIssuanceRequest
import com.microsoft.walletlibrary.requests.rawrequests.RawManifest
import com.microsoft.walletlibrary.requests.rawrequests.RequestType
import com.microsoft.walletlibrary.requests.styles.OpenIdRequesterStyle

/**
 * Maps IssuanceRequest object from VC SDK to RequesterStyle and ManifestIssuanceRequest in library.
 */
private fun IssuanceRequest.getRequesterStyle(): OpenIdRequesterStyle {
    return OpenIdRequesterStyle(
        entityName,
        "",
        null
    )
}

internal fun IssuanceRequest.toManifestIssuanceRequest(): ManifestIssuanceRequest {
    return ManifestIssuanceRequest(
        getRequesterStyle(),
        getAttestations().toRequirement(),
        linkedDomainResult.toRootOfTrust(),
        contract.display.toVerifiedIdStyle(),
        RawManifest(this, RequestType.ISSUANCE)
    )
}
