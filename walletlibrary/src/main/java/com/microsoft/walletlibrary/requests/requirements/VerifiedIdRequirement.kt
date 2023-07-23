/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests.requirements

import com.microsoft.walletlibrary.requests.input.VerifiedIdRequestInput
import com.microsoft.walletlibrary.requests.requirements.constraints.VerifiedIdConstraint
import com.microsoft.walletlibrary.util.RequirementValidationException
import com.microsoft.walletlibrary.util.VerifiedIdRequirementNotFulfilledException
import com.microsoft.walletlibrary.verifiedid.VerifiedId
import kotlinx.serialization.Serializable

/**
 * Represents information that describes Verified IDs required in order to complete a VerifiedID request.
 */
@Serializable
class VerifiedIdRequirement(
    internal val id: String?,

    // The types of Verified ID required.
    val types: List<String>,

    // Constraint that represents how the requirement is fulfilled
    private val constraint: VerifiedIdConstraint,

    // Indicates if the requirement must be encrypted.
    internal val encrypted: Boolean = false,

    // Indicates if the requirement is required or optional.
    override val required: Boolean = false,

    // Purpose of the requested Verified ID which could be displayed to user if needed.
    var purpose: String = "",

    // Information needed for issuance from presentation.
    val issuanceOptions: List<VerifiedIdRequestInput> = mutableListOf(),

    internal var verifiedId: VerifiedId? = null
): Requirement {
    // Validates the requirement and throws an exception if the requirement is invalid or not fulfilled.
    override fun validate(): Result<Unit> {
        if (verifiedId == null)
            return Result.failure(VerifiedIdRequirementNotFulfilledException("VerifiedIdRequirement has not been fulfilled."))
        verifiedId?.let {
            try {
                constraint.matches(it)
            } catch (constraintException: RequirementValidationException) {
                return Result.failure(constraintException)
            }
        }
        return Result.success(Unit)
    }

    // Fulfills the requirement in the request with specified value.
    fun fulfill(selectedVerifiedId: VerifiedId): Result<Unit> {
        try {
            constraint.matches(selectedVerifiedId)
        } catch (constraintException: RequirementValidationException) {
            return Result.failure(constraintException)
        }
        verifiedId = selectedVerifiedId
        return Result.success(Unit)
    }

    // Retrieves list of Verified IDs from the provided list that matches this requirement.
    fun getMatches(verifiedIds: List<VerifiedId>): List<VerifiedId> {
        return verifiedIds.filter { constraint.doesMatch(it) }
    }
}