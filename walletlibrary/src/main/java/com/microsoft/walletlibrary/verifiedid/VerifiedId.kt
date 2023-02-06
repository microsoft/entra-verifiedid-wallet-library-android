/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.verifiedid

/**
 * Holds the information related to a VerifiedID like the claims, type, issued and expiry dates.
 * */
data class VerifiedId(
    internal val id: String,

    // Type of Verified ID.
    val type: VerifiedIdType,

    // List of claims in the Verified ID.
    val claims: List<VerifiedIdClaim>,

    // Time when Verified ID was issued.
    val issuedOn: Long,

    // Time when Verified ID expires.
    val expiresOn: Long? = null,

    // Raw representation of Verified ID.
    internal var raw: String = ""
)