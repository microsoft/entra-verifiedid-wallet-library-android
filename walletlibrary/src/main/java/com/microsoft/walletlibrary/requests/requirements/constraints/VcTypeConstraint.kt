/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests.requirements.constraints

import com.microsoft.walletlibrary.util.VerifiedIdTypeIsNotRequestedTypeException
import com.microsoft.walletlibrary.verifiedid.VerifiedId

/**
 * A Constraint that determines if a Verifiable Credential's type matches the requested value.
 */
internal class VcTypeConstraint(internal val vcType: String): VerifiedIdConstraint {

    override fun doesMatch(verifiedId: VerifiedId): Boolean {
        return verifiedId.types?.contains(vcType) ?: false
    }

    override fun matches(verifiedId: VerifiedId) {
        if (!doesMatch(verifiedId))
            throw VerifiedIdTypeIsNotRequestedTypeException("Provided Verified Id type does not match requested type $vcType.")
    }
}