/*---------------------------------------------------------------------------------------------
*  Copyright (c) Microsoft Corporation. All rights reserved.
*  Licensed under the MIT License. See License.txt in the project root for license information.
*--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests

import com.microsoft.walletlibrary.requests.requirements.GroupRequirement
import com.microsoft.walletlibrary.requests.requirements.Requirement
import com.microsoft.walletlibrary.requests.requirements.VerifiedIdRequirement
import com.microsoft.walletlibrary.requests.styles.RequesterStyle
import com.microsoft.walletlibrary.requests.styles.VerifiedIdStyle

/**
 * Represents an incomplete mutable VerifiedID Request for RequestProcessorExtensions to modify.
 */
data class VerifiedIdPartialRequest(
    /**
     * Display information for the requester
     */
    var requesterStyle: RequesterStyle,

    /**
     * Potential display information for the Verified ID being issued (if this is an issuance request)
     */
    var verifiedIdStyle: VerifiedIdStyle?,

    /**
     * Requirement for this request
     */
    var requirement: Requirement,

    /**
     * Root of trust resolved for this request
     */
    var rootOfTrust: RootOfTrust
) {
    fun replaceRequirement(id: String, transformer: (VerifiedIdRequirement) -> Requirement): Boolean {
        return replaceRequirement(id, this.requirement, transformer)
    }

    fun removeRequirement(id: String): Boolean {
        return removeRequirement(id, this.requirement)
    }

    private fun replaceRequirement(
        id: String,
        requirement: Requirement,
        transformer: (VerifiedIdRequirement) -> Requirement
    ): Boolean {
        if (requirement is GroupRequirement) {
            for (i in 0 until requirement.requirements.size) {
                val childRequirement = requirement.requirements[i]
                if (childRequirement is VerifiedIdRequirement && childRequirement.id == id) {
                    requirement.requirements[i] = transformer(childRequirement)
                    return true
                }
                return this.replaceRequirement(id, childRequirement, transformer)
            }
        }
        return false
    }

    private fun removeRequirement(
        id: String,
        requirement: Requirement
    ): Boolean {
        return when (requirement) {
            is GroupRequirement -> {
                val sizeBeforeOp = requirement.requirements.size
                requirement.requirements.removeIf { it is VerifiedIdRequirement && it.id == id }
                sizeBeforeOp > requirement.requirements.size
            }
            else -> {
                false
            }
        }
    }
}
