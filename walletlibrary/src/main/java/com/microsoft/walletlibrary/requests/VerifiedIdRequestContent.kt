/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests

import com.microsoft.walletlibrary.requests.requirements.GroupRequirement
import com.microsoft.walletlibrary.requests.requirements.GroupRequirementOperator
import com.microsoft.walletlibrary.requests.requirements.IdTokenRequirement
import com.microsoft.walletlibrary.requests.requirements.Requirement
import com.microsoft.walletlibrary.requests.styles.RequesterStyle

/**
 * Contents in a Verified Id Request.
 * It is used to map protocol specific requests in SDK to abstract request objects in library.
 */
internal class VerifiedIdRequestContent(
    // Attributes describing the requester (eg. name, logo).
    internal val requesterStyle: RequesterStyle,

    // Information describing the requirements needed to complete the flow.
    internal var requirement: Requirement,

    // Root of trust of the requester (eg. linked domains).
    internal val rootOfTrust: RootOfTrust,

    internal val injectedIdToken: InjectedIdToken? = null
) {
    internal fun addRequirementsForIdTokenHint(idToken: InjectedIdToken) {
        val pinRequirement = idToken.pinRequirement
        when (requirement) {
            is IdTokenRequirement -> {
                (requirement as IdTokenRequirement).fulfill(idToken.rawToken)
                val requirements = mutableListOf(requirement)
                pinRequirement?.let { requirements.add(it) }
                val groupRequirement =
                    GroupRequirement(false, requirements, GroupRequirementOperator.ALL)
                requirement = groupRequirement
            }
            is GroupRequirement -> addRequirementsForIdTokenHintToGroupRequirement(idToken)
        }
    }

    private fun addRequirementsForIdTokenHintToGroupRequirement(idToken: InjectedIdToken) {
        val pinRequirement = idToken.pinRequirement
        val groupRequirements = (requirement as GroupRequirement).requirements
        for (req in groupRequirements) {
            if (req is IdTokenRequirement) {
                req.fulfill(idToken.rawToken)
                pinRequirement?.let { (requirement as GroupRequirement).requirements.add(it) }
            }
        }
    }
}