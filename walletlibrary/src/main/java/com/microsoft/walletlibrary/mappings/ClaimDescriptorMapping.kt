/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.mappings

import com.microsoft.did.sdk.credential.service.models.contracts.display.ClaimDescriptor
import com.microsoft.walletlibrary.requests.styles.ClaimAttributes

fun ClaimDescriptor.toClaimAttributes(): ClaimAttributes {
    return ClaimAttributes(type = this.type, label = this.label)
}