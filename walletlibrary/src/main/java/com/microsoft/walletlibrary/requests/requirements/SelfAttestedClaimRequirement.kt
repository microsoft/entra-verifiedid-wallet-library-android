/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests.requirements

import com.microsoft.walletlibrary.util.SelfAttestedClaimRequirementNotFulfilledException

/**
 * Represents information that describes self-attested claims required in order to complete a VerifiedID request.
 */
class SelfAttestedClaimRequirement(
    internal val id: String,

    // Claim requested.
    val claim: String,

    // Indicates if the requirement must be encrypted.
    internal val encrypted: Boolean = false,

    // Indicates if the requirement is required or optional.
    override val required: Boolean = false,

    internal var value: String? = null
): Requirement {
    // Validates the requirement and throws an exception if the requirement is invalid or not fulfilled.
    override fun validate() {
        //TODO("should required field be checked?")
        if (value == null)
            throw SelfAttestedClaimRequirementNotFulfilledException("SelfAttestedClaimRequirement has not been fulfilled.")
    }

    // Fulfills the requirement in the request with specified value.
    fun fulfill(selfAttestedClaimValue: String) {
        value = selfAttestedClaimValue
    }
}