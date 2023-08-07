/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests.requirements

import com.microsoft.walletlibrary.util.RequirementNotMetException
import com.microsoft.walletlibrary.util.VerifiedIdExceptions
import com.microsoft.walletlibrary.util.VerifiedIdResult
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
) : Requirement {

    override fun validate(): VerifiedIdResult<Unit> {
        val validationExceptions = mutableListOf<String>()
        for (requirement in requirements) {
            if (requirement.validate().isFailure)
                validationExceptions.add("${requirement.validate().exceptionOrNull()}")
        }
        if (validationExceptions.isNotEmpty())
            return RequirementNotMetException(
                "Group Requirement validation failed with following exceptions: $validationExceptions",
                VerifiedIdExceptions.REQUIREMENT_NOT_MET_EXCEPTION.value
            ).toVerifiedIdResult()
        return VerifiedIdResult.success(Unit)
    }
}