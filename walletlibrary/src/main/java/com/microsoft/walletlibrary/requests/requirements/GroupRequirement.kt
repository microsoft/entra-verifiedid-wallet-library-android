/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests.requirements

enum class GroupRequirementOperator {
    ANY,
    ALL
}

/**
 * Represents a group of requirements required to complete the request.
 */
class GroupRequirement(
    override val required: Boolean,
    var requirements: List<Requirement>,
    val requirementOperator: GroupRequirementOperator
): Requirement {

    override fun validate() {
        //TODO("Not fully implemented yet")
        for (requirement in requirements) {
            requirement.validate()
        }
    }
}