/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests.requirements

import com.microsoft.walletlibrary.util.RequirementValidationException

enum class GroupRequirementOperator {
    ANY,
    ALL
}

/**
 * Represents a group of requirements required to complete the request.
 */
class GroupRequirement(
    override val required: Boolean,
    val requirements: MutableList<Requirement>,
    val requirementOperator: GroupRequirementOperator
): Requirement {

    override fun validate(): Result<Unit> {
        //TODO("Not fully implemented yet")
        val validationFailureRequirementTypes = mutableListOf<String>()
        for (requirement in requirements) {
            if (requirement.validate().isFailure)
                validationFailureRequirementTypes.add("${requirement::class.simpleName}")
        }
        if (validationFailureRequirementTypes.isNotEmpty())
            return Result.failure(RequirementValidationException("Validation failed for the requirements $validationFailureRequirementTypes"))
        return Result.success(Unit)
    }
}