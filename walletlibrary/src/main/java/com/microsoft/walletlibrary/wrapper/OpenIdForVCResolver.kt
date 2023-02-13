/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.wrapper

import com.microsoft.did.sdk.VerifiableCredentialSdk
import com.microsoft.did.sdk.credential.service.PresentationRequest
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.walletlibrary.requests.rawrequests.OpenIdRawRequest
import com.microsoft.walletlibrary.requests.rawrequests.RequestType
import com.microsoft.walletlibrary.requests.rawrequests.VerifiedIdOpenIdJwtRawRequest
import com.microsoft.walletlibrary.util.VerifiedIdRequestFetchException

/**
 * Wrapper class to wrap the getRequest call from VC SDK and return a raw request.
 */
object OpenIdForVCResolver {

    // Fetches the request from VC SDK using the url and converts it to raw request.
    suspend fun getRequest(uri: String): OpenIdRawRequest {
        when (val presentationRequestResult =
            VerifiableCredentialSdk.presentationService.getRequest(uri)) {
            is Result.Success -> {
                val request = presentationRequestResult.payload
                val requestType = getRequestType(request)
                return VerifiedIdOpenIdJwtRawRequest(requestType, request)
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

    private suspend fun getIssuanceRequest(uri: String): OpenIdRawRequest {
        return when (val issuanceRequestResult =
            VerifiableCredentialSdk.issuanceService.getRequest(uri)) {
            is Result.Success -> {
                val request = issuanceRequestResult.payload
                VerifiedIdOpenIdJwtRawRequest(RequestType.ISSUANCE, request)
            }
            is Result.Failure -> {
                throw VerifiedIdRequestFetchException(
                    "Unable to fetch issuance request",
                    issuanceRequestResult.payload
                )
            }
        }
    }
}