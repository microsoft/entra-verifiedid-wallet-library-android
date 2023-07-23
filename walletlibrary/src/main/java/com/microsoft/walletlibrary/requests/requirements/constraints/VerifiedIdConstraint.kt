/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests.requirements.constraints

import com.microsoft.walletlibrary.verifiedid.VerifiedId

/**
 * Represents a constraint on the properties of VerifiedId to determine if it matches the constraint.
 */
interface VerifiedIdConstraint {
    fun doesMatch(verifiedId: VerifiedId): Boolean

    fun matches(verifiedId: VerifiedId)
}