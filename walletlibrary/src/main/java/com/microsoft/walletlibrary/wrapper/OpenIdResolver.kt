/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.wrapper

import com.microsoft.walletlibrary.did.sdk.VerifiableCredentialSdk
import com.microsoft.walletlibrary.did.sdk.credential.service.PresentationRequest
import com.microsoft.walletlibrary.did.sdk.identifier.resolvers.RootOfTrustResolver
import com.microsoft.walletlibrary.did.sdk.credential.service.models.oidc.PresentationRequestContent
import com.microsoft.walletlibrary.did.sdk.util.controlflow.Result
import com.microsoft.walletlibrary.requests.rawrequests.OpenIdRawRequest
import com.microsoft.walletlibrary.requests.rawrequests.RequestType
import com.microsoft.walletlibrary.requests.rawrequests.VerifiedIdOpenIdJwtRawRequest
import com.microsoft.walletlibrary.util.VerifiedIdRequestFetchException

/**
 * Wrapper class to wrap the get Presentation Request from VC SDK and return a raw request.
 */
object OpenIdResolver {

    // Fetches the presentation request from VC SDK using the url and converts it to raw request.
    internal suspend fun getRequest(uri: String, rootOfTrustResolver: RootOfTrustResolver? = null): OpenIdRawRequest {
        val presentationRequestResult = VerifiableCredentialSdk.presentationService.getRequest(uri, rootOfTrustResolver)
        return handleRequestResult(presentationRequestResult)
    }

    internal suspend fun validateRequest(requestContent: PresentationRequestContent, rootOfTrustResolver: RootOfTrustResolver?): OpenIdRawRequest {
        val presentationRequestResult = VerifiableCredentialSdk.presentationService.validateRequest(requestContent, rootOfTrustResolver)
        return handleRequestResult(presentationRequestResult)
    }

    private fun handleRequestResult(presentationRequestResult: Result<PresentationRequest>): OpenIdRawRequest {
        when (presentationRequestResult) {
            is Result.Success -> {
                val request = presentationRequestResult.payload
                val requestType = getRequestType(request)
                return VerifiedIdOpenIdJwtRawRequest(request, requestType)
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