/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests.requirements

import com.microsoft.walletlibrary.requests.input.VerifiedIdRequestInput
import com.microsoft.walletlibrary.requests.requirements.constraints.VerifiedIdConstraint
import com.microsoft.walletlibrary.util.RequirementNotMetException
import com.microsoft.walletlibrary.util.RequirementValidationException
import com.microsoft.walletlibrary.util.VerifiedIdExceptions
import com.microsoft.walletlibrary.util.VerifiedIdResult
import com.microsoft.walletlibrary.verifiedid.VerifiedId

/**
 * Represents information that describes Verified IDs required in order to complete a VerifiedID request.
 */
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
) : Requirement {
    // Validates the requirement and throws an exception if the requirement is invalid or not fulfilled.
    override fun validate(): VerifiedIdResult<Unit> {
        if (verifiedId == null)
            return RequirementNotMetException(
                "Verified ID has not been set.",
                VerifiedIdExceptions.REQUIREMENT_NOT_MET_EXCEPTION.value
            ).toVerifiedIdResult()
        verifiedId?.let {
            try {
                constraint.matches(it)
            } catch (constraintException: RequirementValidationException) {
                return RequirementNotMetException(
                    "Verified ID constraint do not match.",
                    VerifiedIdExceptions.REQUIREMENT_NOT_MET_EXCEPTION.value,
                    listOf(constraintException)
                ).toVerifiedIdResult()
            }
        }
        return VerifiedIdResult.success(Unit)
    }

    // Fulfills the requirement in the request with specified value.
    fun fulfill(selectedVerifiedId: VerifiedId): VerifiedIdResult<Unit> {
        try {
            constraint.matches(selectedVerifiedId)
        } catch (constraintException: RequirementValidationException) {
            return RequirementNotMetException(
                "Verified ID constraint do not match.",
                VerifiedIdExceptions.REQUIREMENT_NOT_MET_EXCEPTION.value,
                listOf(constraintException)
            ).toVerifiedIdResult()
        }
        verifiedId = selectedVerifiedId
        return VerifiedIdResult.success(Unit)
    }

    // Retrieves list of Verified IDs from the provided list that matches this requirement.
    fun getMatches(verifiedIds: List<VerifiedId>): List<VerifiedId> {
        return verifiedIds.filter { constraint.doesMatch(it) }
    }
}