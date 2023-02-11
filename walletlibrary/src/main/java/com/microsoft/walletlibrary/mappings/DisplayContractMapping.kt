/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.mappings

import com.microsoft.did.sdk.credential.service.models.contracts.display.DisplayContract
import com.microsoft.walletlibrary.requests.styles.VerifiedIDStyle

fun DisplayContract.toVerifiedIdStyle(): VerifiedIDStyle {
    val cardDescriptor = this.card
    return VerifiedIDStyle(
        this.locale,
        cardDescriptor.title,
        cardDescriptor.issuedBy,
        cardDescriptor.backgroundColor,
        cardDescriptor.textColor,
        cardDescriptor.description,
        this.claims.values.map { it.toClaimAttributes() },
        cardDescriptor.logo?.toLogo()
    )
}