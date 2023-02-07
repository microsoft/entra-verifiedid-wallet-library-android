/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.verifiedid

/**
 * Represents each claim that is embedded in a VerifiedID.
 */
data class VerifiedIdClaim(
    // Represents name of the claim.
    val id: String,

    // Represents value of the claim.
    val value: Any
)