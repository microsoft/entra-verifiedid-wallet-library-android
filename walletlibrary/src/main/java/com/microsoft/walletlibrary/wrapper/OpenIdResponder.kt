/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.wrapper

import com.microsoft.did.sdk.VerifiableCredentialSdk
import com.microsoft.did.sdk.credential.service.PresentationRequest
import com.microsoft.did.sdk.credential.service.PresentationResponse
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.walletlibrary.mappings.presentation.addRequirements
import com.microsoft.walletlibrary.requests.requirements.Requirement
import com.microsoft.walletlibrary.util.OpenIdResponseCompletionException


/**
 * Wrapper class to wrap the send presentation response to VC SDK.
 */
object OpenIdResponder {

    internal suspend fun sendPresentationResponse(
        presentationRequest: PresentationRequest,
        requirement: Requirement
    ) {
        val presentationResponse = PresentationResponse(presentationRequest)
        presentationResponse.addRequirements(requirement)
        val presentationResponseResult =
            VerifiableCredentialSdk.presentationService.sendResponse(presentationResponse)
        if (presentationResponseResult is Result.Failure) {
            throw OpenIdResponseCompletionException(
                "Unable to send presentation response",
                presentationResponseResult.payload
            )
        }

    }
}