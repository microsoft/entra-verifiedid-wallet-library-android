/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests.requirements

import com.microsoft.walletlibrary.util.PinRequirementNotFulfilledException

/**
 * Represents information that describes pin required in order to complete a VerifiedID request.
 */
class PinRequirement(
    // Length of the pin.
    val length: Int,

    // Type of the pin (eg. alphanumeric, numeric).
    val type: String,

    // Indicates if pin is required or optional.
    override val required: Boolean = false,

    internal val salt: String? = null,

    internal var pin: String? = null
): Requirement {
    // Validates the requirement and throws an exception if the requirement is invalid or not fulfilled.
    override fun validate() {
        if (pin == null)
            throw PinRequirementNotFulfilledException("PinRequirement has not been fulfilled.")
    }

    // Fulfills the requirement in the request with specified value.
    fun fulfill(pinValue: String) {
        pin = pinValue
    }
}