/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests

import com.microsoft.walletlibrary.requests.rawrequests.OpenIdRawRequest
import com.microsoft.walletlibrary.requests.requirements.Requirement
import com.microsoft.walletlibrary.requests.styles.RequesterStyle
import com.microsoft.walletlibrary.util.UserCanceledException
import com.microsoft.walletlibrary.util.VerifiedIdExceptions
import com.microsoft.walletlibrary.util.VerifiedIdResult
import com.microsoft.walletlibrary.util.getResult
import com.microsoft.walletlibrary.wrapper.OpenIdResponder
import kotlinx.serialization.Serializable

/**
 * Presentation request specific to OpenId protocol.
 */
@Serializable
internal class OpenIdPresentationRequest(
    // Attributes describing the requester (eg. name, logo).
    override val requesterStyle: RequesterStyle,

    // Information describing the requirements needed to complete the flow.
    override val requirement: Requirement,

    // Root of trust of the requester (eg. linked domains).
    override val rootOfTrust: RootOfTrust,

    val request: OpenIdRawRequest
): VerifiedIdPresentationRequest {
    // Indicates whether presentation request is satisfied on client side.
    override fun isSatisfied(): Boolean {
        val validationResult = requirement.validate()
        //TODO("Add logging")
        return !validationResult.isFailure
    }

    // Completes the presentation request and returns Result with success status if successful.
    override suspend fun complete(): VerifiedIdResult<Unit> {
        return getResult {
            OpenIdResponder.sendPresentationResponse(request.rawRequest, requirement)
        }
    }

    override suspend fun cancel(message: String?): VerifiedIdResult<Unit> {
        return getResult {
            throw UserCanceledException(
                message ?: "User Canceled",
                VerifiedIdExceptions.USER_CANCELED_EXCEPTION.value
            )
        }
    }
}