/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.verifiedid

import com.microsoft.walletlibrary.requests.styles.VerifiedIdStyle
import java.util.*

/**
 * Data model for VerifiedId
 */
interface VerifiedId {
    val id: String

    // Time when Verified ID was issued.
    val issuedOn: Date

    // Time when Verified ID expires.
    val expiresOn: Date?

    // Information needed to style a Verified ID.
    val style: VerifiedIdStyle?

    // List of types of Verified ID.
    val types: List<String>

    // Return list of claims in the Verified ID.
    fun getClaims(): ArrayList<VerifiedIdClaim>
}