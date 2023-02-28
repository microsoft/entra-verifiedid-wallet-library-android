/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests.requirements

import com.microsoft.walletlibrary.util.IdTokenRequirementNotFulfilledException

/**
 * Represents information that describes Id Token required in order to complete a VerifiedID request.
 */
class IdTokenRequirement(
    internal val id: String,

    // Properties used by developers to get access token (configuration, clientId, resourceId, scope).
    val configuration: String,
    val clientId: String,
    val redirectUri: String,
    val scope: String,

    // Nonce is generated using user DID.
    val nonce: String,

    // Specific claims requested from id token.
    internal val claims: List<RequestedClaim>,

    // Indicates whether the requirement must be encrypted.
    internal val encrypted: Boolean = false,

    // Indicates whether the requirement is required or optional.
    override val required: Boolean = false,

    internal var idToken: String? = null
): Requirement {

    // Validates the requirement and throws an exception if the requirement is invalid or not fulfilled.
    override fun validate() {
        if (idToken == null)
            throw IdTokenRequirementNotFulfilledException("IdTokenRequirement has not been fulfilled.")
    }

    // Fulfills the requirement in the request with specified value.
    fun fulfill(idTokenValue: String) {
        idToken = idTokenValue
    }
}