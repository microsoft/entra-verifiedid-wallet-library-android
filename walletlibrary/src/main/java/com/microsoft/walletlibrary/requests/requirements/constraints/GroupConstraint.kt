/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests.requirements.constraints

import com.microsoft.walletlibrary.util.NoMatchForAnyConstraintsException
import com.microsoft.walletlibrary.util.NoMatchForAtLeastOneConstraintException
import com.microsoft.walletlibrary.util.WalletLibraryException
import com.microsoft.walletlibrary.verifiedid.VerifiedId

enum class GroupConstraintOperator {
    ANY,
    ALL
}

/**
 * Represents a group of constraints to fulfill a requirement.
 */
internal class GroupConstraint(
    internal val constraints: List<VerifiedIdConstraint>,
    internal val constraintOperator: GroupConstraintOperator
): VerifiedIdConstraint {
    override fun doesMatch(verifiedId: VerifiedId): Boolean {
        when (constraintOperator) {
            GroupConstraintOperator.ANY -> {
                constraints.forEach { if (it.doesMatch(verifiedId)) return true }
                return false
            }
            GroupConstraintOperator.ALL -> {
                constraints.forEach { if (!it.doesMatch(verifiedId)) return false }
                return true
            }
        }
    }

    override fun matches(verifiedId: VerifiedId) {
        val validationExceptions = mutableListOf<Throwable>()
        constraints.forEach { constraint ->
            try {
                constraint.matches(verifiedId)
            } catch (exception: WalletLibraryException) {
                validationExceptions.add(exception)
            }
        }

        when (constraintOperator) {
            GroupConstraintOperator.ANY -> {
                if (constraints.size == validationExceptions.size)
                    throw NoMatchForAnyConstraintsException("None of the constraints match.", validationExceptions)
            }
            GroupConstraintOperator.ALL -> {
                if (validationExceptions.isNotEmpty())
                    throw NoMatchForAtLeastOneConstraintException("At least one of the constraints doesn't match.", validationExceptions)
            }
        }
    }
}