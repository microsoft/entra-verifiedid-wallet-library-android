/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests.requirements.constraints

import com.microsoft.walletlibrary.verifiedid.VerifiableCredential
import com.microsoft.walletlibrary.verifiedid.VerifiedId

/**
 * A Constraint that determines if a Verifiable Credential's type matches the requested value.
 */
class VcTypeConstraint(internal val vcType: String): VerifiedIdConstraint {

    override fun doesMatch(verifiedId: VerifiedId): Boolean {
        if (verifiedId !is VerifiableCredential)
            return false
        return verifiedId.types.contains(vcType)
    }
}