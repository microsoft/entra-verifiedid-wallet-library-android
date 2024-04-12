/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests.requirements

import kotlinx.serialization.Serializable
import com.microsoft.walletlibrary.requests.handlers.RequestProcessorSerializer
import com.microsoft.walletlibrary.util.RequirementNotMetException
import com.microsoft.walletlibrary.util.VerifiedIdExceptions
import com.microsoft.walletlibrary.util.VerifiedIdResult
import com.microsoft.walletlibrary.verifiedid.VerifiedIdSerializer

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

    @Throws
    override suspend fun <T> serialize(
        protocolSerializer: RequestProcessorSerializer<T>,
        verifiedIdSerializer: VerifiedIdSerializer<T>
    ): T? {
        when (this.requirementOperator) {
            GroupRequirementOperator.ANY -> {
                for (requirement in this.requirements) {
                    try {
                        requirement.validate().getOrThrow()
                        protocolSerializer.serialize(requirement, verifiedIdSerializer)
                    } finally {
                        // nothing needs to be done, this requirement won't be serialized
                    }
                }
            }
            GroupRequirementOperator.ALL -> {
                for (requirement in this.requirements) {
                    protocolSerializer.serialize(requirement, verifiedIdSerializer)
                }
            }
        }
        // this requirement has no serialization
        return null
    }
}
