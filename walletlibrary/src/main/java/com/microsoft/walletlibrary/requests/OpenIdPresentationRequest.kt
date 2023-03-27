/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests

import com.microsoft.walletlibrary.requests.rawrequests.OpenIdRawRequest
import com.microsoft.walletlibrary.requests.requirements.Requirement
import com.microsoft.walletlibrary.requests.styles.RequesterStyle
import com.microsoft.walletlibrary.util.PresentationRequestCancelIsNotSupported
import com.microsoft.walletlibrary.util.RequirementValidationException
import com.microsoft.walletlibrary.util.WalletLibraryException
import com.microsoft.walletlibrary.wrapper.OpenIdResponder

/**
 * Presentation request specific to OpenId protocol.
 */
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
        try {
            requirement.validate()
        } catch (exception: RequirementValidationException) {
            //TODO("log exception message")
            return false
        }
        return true
    }

    // Completes the presentation request and returns Result with success status if successful.
    override suspend fun complete(): Result<Unit> {
        return try {
            val result = OpenIdResponder.sendPresentationResponse(request.rawRequest, requirement)
            Result.success(result)
        } catch (exception: WalletLibraryException) {
            Result.failure(exception)
        }
    }

    override suspend fun cancel(message: String?): Result<Unit> {
        return Result.failure(PresentationRequestCancelIsNotSupported("Cancelling presentation request is not supported."))
    }
}