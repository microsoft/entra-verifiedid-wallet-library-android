/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests.handlers

import com.microsoft.walletlibrary.mappings.toVerifiedIdIssuanceRequest
import com.microsoft.walletlibrary.requests.VerifiedIdIssuanceRequest
import com.microsoft.walletlibrary.requests.VerifiedIdPresentationRequest
import com.microsoft.walletlibrary.requests.VerifiedIdRequest
import com.microsoft.walletlibrary.requests.rawrequests.OpenIdRawRequest
import com.microsoft.walletlibrary.requests.rawrequests.RawRequest
import com.microsoft.walletlibrary.requests.rawrequests.RequestType
import com.microsoft.walletlibrary.util.UnSupportedProtocolException

/**
 * OIDC protocol specific implementation of RequestHandler. It can handle OpenID raw request and returns a VerifiedIdRequest.
 */
class OpenIdRequestHandler : RequestHandler {

    override fun handleRequest(rawRequest: RawRequest): VerifiedIdRequest {
        if (rawRequest !is OpenIdRawRequest) throw UnSupportedProtocolException("Received a raw request of unsupported protocol")
        return when (rawRequest.requestType) {
            RequestType.ISSUANCE -> handleIssuanceRequest(rawRequest)
            RequestType.PRESENTATION -> handlePresentationRequest(rawRequest)
        }
    }

    private fun handleIssuanceRequest(rawRequest: RawRequest): VerifiedIdIssuanceRequest {
        return (rawRequest as OpenIdRawRequest).toVerifiedIdIssuanceRequest()
    }

    private fun handlePresentationRequest(rawRequest: RawRequest): VerifiedIdPresentationRequest {
        TODO("Not yet implemented")
    }
}