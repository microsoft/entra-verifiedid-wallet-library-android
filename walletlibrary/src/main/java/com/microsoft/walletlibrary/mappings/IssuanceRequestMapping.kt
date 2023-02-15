/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.mappings

import com.microsoft.did.sdk.credential.service.IssuanceRequest
import com.microsoft.walletlibrary.requests.OpenIdIssuanceRequest
import com.microsoft.walletlibrary.requests.styles.RequesterStyle

internal fun IssuanceRequest.toRequesterStyle(): RequesterStyle {
    return RequesterStyle(
        this.entityName,
        "",
        null
    )
}

internal fun IssuanceRequest.toOpenIdIssuanceRequest(): OpenIdIssuanceRequest {
    return OpenIdIssuanceRequest(
        this.toRequesterStyle(),
        this.getAttestations().toRequirement(),
        this.linkedDomainResult.toRootOfTrust(),
        this.contract.display.toVerifiedIdStyle()
    )
}
