/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.wrapper

import com.microsoft.did.sdk.VerifiableCredentialSdk
import com.microsoft.did.sdk.credential.service.PresentationResponse
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.walletlibrary.util.OpenIdResponseCompletionException


/**
 * Wrapper class to wrap the send presentation response to VC SDK.
 */
object OpenIdResponder {

    internal suspend fun sendPresentationResponse(response: PresentationResponse) {
        when (val presentationResponseResult =
            VerifiableCredentialSdk.presentationService.sendResponse(response)) {
            is Result.Success -> {}
            is Result.Failure -> {
                throw OpenIdResponseCompletionException(
                    "Unable to send presentation response",
                    presentationResponseResult.payload
                )
            }
        }
    }
}