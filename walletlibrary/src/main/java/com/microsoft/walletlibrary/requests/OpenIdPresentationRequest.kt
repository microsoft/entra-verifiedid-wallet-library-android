/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests

import com.microsoft.did.sdk.credential.service.PresentationResponse
import com.microsoft.walletlibrary.requests.rawrequests.OpenIdRawRequest
import com.microsoft.walletlibrary.requests.requirements.Requirement
import com.microsoft.walletlibrary.requests.styles.RequesterStyle
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
) : VerifiedIdPresentationRequest {
    override fun isSatisfied(): Boolean {
        try {
            requirement.validate()
        } catch (exception: RequirementValidationException) {
            //TODO("log exception message")
            return false
        }
        return true
    }

    // Completes the request and returns nothing if successful.
    override suspend fun complete(): Result<Unit> {
        val response = PresentationResponse(request.rawRequest)
        return try {
            val result = OpenIdResponder.sendPresentationResponse(response)
            Result.success(result)
        } catch (exception: WalletLibraryException) {
            Result.failure(exception)
        }
    }

    override fun cancel(message: String?): Result<Void> {
        TODO("Not yet implemented")
    }
}