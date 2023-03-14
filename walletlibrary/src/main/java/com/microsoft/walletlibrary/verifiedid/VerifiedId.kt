/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.verifiedid

/**
 * Data model for VerifiedId
 */
interface VerifiedId {
    val id: String

    // Time when Verified ID was issued.
    val issuedOn: Long

    // Time when Verified ID expires.
    val expiresOn: Long?

    // Return list of claims in the Verified ID.
    fun getClaims(): List<VerifiedIdClaim>
}