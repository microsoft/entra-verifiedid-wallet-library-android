/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests.requirements

import com.microsoft.walletlibrary.util.PinRequirementNotFulfilledException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents information that describes pin required in order to complete a VerifiedID request.
 */
@Serializable
class PinRequirement(
    // Length of the pin.
    val length: Int,

    // Type of the pin (eg. alphanumeric, numeric).
    @SerialName("pinType")
    val type: String,

    // Indicates if pin is required or optional.
    override val required: Boolean = false,

    internal val salt: String? = null,

    internal var pin: String? = null
): Requirement {
    // Validates the requirement and throws an exception if the requirement is invalid or not fulfilled.
    override fun validate(): Result<Unit> {
        if (pin == null)
            return Result.failure(PinRequirementNotFulfilledException("PinRequirement has not been fulfilled."))
        return Result.success(Unit)
    }

    // Fulfills the requirement in the request with specified value.
    fun fulfill(pinValue: String) {
        pin = pinValue
    }
}