/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.mappings.issuance

import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.display.DisplayContract
import com.microsoft.walletlibrary.requests.styles.BasicVerifiedIdStyle
import com.microsoft.walletlibrary.requests.styles.VerifiedIdStyle

internal fun DisplayContract.toVerifiedIdStyle(): VerifiedIdStyle {
    val cardDescriptor = this.card
    return BasicVerifiedIdStyle(
        cardDescriptor.title,
        cardDescriptor.issuedBy,
        cardDescriptor.backgroundColor,
        cardDescriptor.textColor,
        cardDescriptor.description,
        cardDescriptor.logo?.toLogo()
    )
}