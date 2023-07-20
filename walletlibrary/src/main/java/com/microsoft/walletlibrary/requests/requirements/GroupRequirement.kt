/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests.requirements

import com.microsoft.walletlibrary.util.RequirementValidationException
import kotlinx.serialization.Serializable

enum class GroupRequirementOperator {
    ANY,
    ALL
}

/**
 * Represents a group of requirements required to complete the request.
 */
@Serializable
class GroupRequirement(
    override val required: Boolean,
    val requirements: MutableList<Requirement>,
    val requirementOperator: GroupRequirementOperator
): Requirement {

    override fun validate(): Result<Unit> {
        val validationExceptions = mutableListOf<String>()
        for (requirement in requirements) {
            if (requirement.validate().isFailure)
                validationExceptions.add("${requirement.validate().exceptionOrNull()}")
        }
        if (validationExceptions.isNotEmpty())
            return Result.failure(RequirementValidationException("Validation failed with following exceptions: $validationExceptions"))
        return Result.success(Unit)
    }
}