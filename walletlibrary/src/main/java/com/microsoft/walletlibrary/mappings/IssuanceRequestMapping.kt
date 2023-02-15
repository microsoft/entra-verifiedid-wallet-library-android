/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.mappings

import com.microsoft.did.sdk.credential.service.IssuanceRequest
import com.microsoft.walletlibrary.requests.ContractIssuanceRequest
import com.microsoft.walletlibrary.requests.styles.OpenIdRequesterStyle

internal fun IssuanceRequest.toRequesterStyle(): OpenIdRequesterStyle {
    return OpenIdRequesterStyle(
        this.entityName,
        "",
        null
    )
}

internal fun IssuanceRequest.toOpenIdIssuanceRequest(): ContractIssuanceRequest {
    return ContractIssuanceRequest(
        this.toRequesterStyle(),
        this.getAttestations().toRequirement(),
        this.linkedDomainResult.toRootOfTrust(),
        this.contract.display.toVerifiedIdStyle()
    )
}
