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
import com.microsoft.walletlibrary.requests.styles.VerifiedIdStyle
import com.microsoft.walletlibrary.util.Constants.IDTOKENHINT_CONFIGURATION

/**
 * Contents in a Verified Id Request.
 * It is used to map protocol specific requests in SDK to abstract request objects in library.
 */
internal class IssuanceRequestContent(
    // Attributes describing the requester (eg. name, logo).
    internal val requesterStyle: RequesterStyle,

    // Information describing the requirements needed to complete the flow.
    internal var requirement: Requirement,

    // Root of trust of the requester (eg. linked domains).
    internal val rootOfTrust: RootOfTrust,

    internal val verifiedIDStyle: VerifiedIdStyle
) {
    internal fun addRequirementsForIdTokenHint(idToken: InjectedIdToken) {
        when (requirement) {
            is IdTokenRequirement -> {
                val groupRequirement =
                    GroupRequirement(
                        false,
                        mutableListOf(requirement),
                        GroupRequirementOperator.ALL
                    )
                fulfillIdTokenRequirement(
                    requirement as IdTokenRequirement,
                    idToken,
                    groupRequirement
                )
                requirement = groupRequirement
            }
            is GroupRequirement -> addRequirementsForIdTokenHintToGroupRequirement(idToken)
        }
    }

    private fun addRequirementsForIdTokenHintToGroupRequirement(idToken: InjectedIdToken) {
        if (requirement is GroupRequirement) {
            val requirementsInGroup = (requirement as GroupRequirement).requirements
            for (requirementInGroup in requirementsInGroup) {
                if ((requirementInGroup is IdTokenRequirement) && (requirementInGroup.configuration == IDTOKENHINT_CONFIGURATION)) {
                    fulfillIdTokenRequirement(
                        requirementInGroup,
                        idToken,
                        requirement as GroupRequirement
                    )
                }
            }
        }
    }

    private fun fulfillIdTokenRequirement(
        idTokenRequirement: IdTokenRequirement,
        idToken: InjectedIdToken,
        groupRequirement: GroupRequirement
    ) {
        idTokenRequirement.fulfill(idToken.rawToken)
        idToken.pinRequirement?.let {
            groupRequirement.requirements.add(it)
        }
    }
}