/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests.requirements

import com.microsoft.walletlibrary.util.AccessTokenRequirementNotFulfilledException
import kotlinx.serialization.Serializable

/**
 * Represents information that describes Access Token required in order to complete a VerifiedID request.
 */
@Serializable
class AccessTokenRequirement(
    internal val id: String,

    // Properties used by developers to get access token (configuration, clientId, resourceId, scope).
    val configuration: String,
    val redirectUri: String,
    val resourceId: String,
    val scope: String,

    // Specific claims requested from access token.
    internal val claims: List<RequestedClaim>,

    // Indicates whether the requirement must be encrypted.
    internal val encrypted: Boolean = false,

    // Indicates whether the requirement is required or optional.
    override val required: Boolean = false,

    internal var accessToken: String? = null
): Requirement {
    // Validates the requirement and throws an exception if the requirement is invalid or not fulfilled.
    override fun validate(): Result<Unit> {
        if (accessToken == null)
            return Result.failure(AccessTokenRequirementNotFulfilledException("AccessTokenRequirement has not been fulfilled."))
        return Result.success(Unit)
    }

    // Fulfills the requirement in the request with specified value.
    fun fulfill(accessTokenValue: String) {
        accessToken = accessTokenValue
    }
}