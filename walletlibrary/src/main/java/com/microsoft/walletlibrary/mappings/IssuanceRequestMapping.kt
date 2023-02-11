/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.mappings

import com.microsoft.did.sdk.credential.service.IssuanceRequest
import com.microsoft.walletlibrary.requests.styles.RequesterStyle

fun IssuanceRequest.toRequesterStyle(): RequesterStyle {
    val logo = this.contract.display.card.logo
    return RequesterStyle(
        this.entityName,
        "",
        logo?.toLogo()
    )
}