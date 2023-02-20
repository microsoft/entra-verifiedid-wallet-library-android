/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests.requirements

import com.microsoft.walletlibrary.requests.input.VerifiedIdRequestInput
import com.microsoft.walletlibrary.util.VerifiedIdRequirementNotFulfilledException
import com.microsoft.walletlibrary.verifiedid.VerifiedId

/**
 * Represents information that describes Verified IDs required in order to complete a VerifiedID request.
 */
class VerifiedIdRequirement(
    internal val id: String,

    // The types of Verified ID required.
    val types: List<String>,

    // Indicates if the requirement must be encrypted.
    internal val encrypted: Boolean = false,

    // Indicates if the requirement is required or optional.
    override val required: Boolean = false,

    // Purpose of the requested Verified ID which could be displayed to user if needed.
    var purpose: String = "",

    // Information needed for issuance from presentation.
    val issuanceOptions: List<VerifiedIdRequestInput> = mutableListOf(),

    private var verifiedId: VerifiedId? = null
): Requirement {
    // Validates the requirement and throws an exception if the requirement is invalid or not fulfilled.
    override fun validate() {
        if (verifiedId == null)
            throw VerifiedIdRequirementNotFulfilledException("VerifiedIdRequirement has not been fulfilled.")
    }

    // Fulfills the requirement in the request with specified value.
    fun fulfill(verifiedIdValue: VerifiedId) {
        verifiedId = verifiedIdValue
    }

    // Retrieves list of Verified IDs from the provided list that matches this requirement.
    fun getMatches(verifiedIds: List<VerifiedId>): List<VerifiedId> {
        TODO("Not yet implemented")
    }
}