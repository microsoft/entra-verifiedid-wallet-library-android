/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests.styles

data class ClaimAttributes(
    // Type of the claim (eg. String or Date).
    val type: String,

    // Label of the claim.
    val label: String
)