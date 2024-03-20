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
interface VerifiedIdPartialRequest {
    // Attributes describing the requester (eg. name, logo).
    val requesterStyle: RequesterStyle

    // Information describing the requirements needed to complete the flow.
    val requirement: Requirement

    // Root of trust of the requester (eg. linked domains).
    val rootOfTrust: RootOfTrust

    fun replaceRequirement(id: String, transformer: (VerifiedIdRequirement) -> Requirement): Boolean {
        return replaceRequirement(id, this.requirement, transformer)
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
}
