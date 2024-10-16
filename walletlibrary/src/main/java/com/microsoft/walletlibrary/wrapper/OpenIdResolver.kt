/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.wrapper

import com.microsoft.walletlibrary.did.sdk.VerifiableCredentialSdk
import com.microsoft.walletlibrary.did.sdk.credential.service.PresentationRequest
import com.microsoft.walletlibrary.did.sdk.credential.service.models.oidc.PresentationRequestContent
import com.microsoft.walletlibrary.did.sdk.util.controlflow.Result
import com.microsoft.walletlibrary.requests.rawrequests.OpenIdProcessedRequest
import com.microsoft.walletlibrary.requests.rawrequests.RequestType
import com.microsoft.walletlibrary.requests.rawrequests.VerifiedIdOpenIdJwtRawRequest
import com.microsoft.walletlibrary.util.VerifiedIdRequestFetchException

/**
 * Wrapper class to wrap the get Presentation Request from VC SDK and return a raw request.
 */
object OpenIdResolver {

    // Fetches the presentation request from VC SDK using the url and converts it to raw request.
    internal suspend fun getRequest(uri: String, preferHeaders: List<String>): OpenIdProcessedRequest {
        val presentationRequestResult = VerifiableCredentialSdk.presentationService.getRequest(uri, preferHeaders)
        return handleRequestResult(presentationRequestResult, emptyMap())
    }

    internal suspend fun validateRequest(requestContent: PresentationRequestContent, rawRequest: Map<String, Any>): OpenIdProcessedRequest {
        val presentationRequestResult = VerifiableCredentialSdk.presentationService.validateRequest(requestContent)
        return handleRequestResult(presentationRequestResult, rawRequest)
    }

    private fun handleRequestResult(presentationRequestResult: Result<PresentationRequest>, rawRequest: Map<String, Any>): OpenIdProcessedRequest {
        when (presentationRequestResult) {
            is Result.Success -> {
                val request = presentationRequestResult.payload
                val requestType = getRequestType(request)
                return VerifiedIdOpenIdJwtRawRequest(request, requestType, rawRequest)
            }
            is Result.Failure -> {
                throw VerifiedIdRequestFetchException(
                    "Unable to fetch presentation request",
                    presentationRequestResult.payload
                )
            }
        }
    }

    private fun getRequestType(request: PresentationRequest): RequestType {
        return if (request.content.prompt == com.microsoft.walletlibrary.util.Constants.PURE_ISSUANCE_FLOW_VALUE)
            RequestType.ISSUANCE
        else
            RequestType.PRESENTATION
    }
}