/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests.handlers

import com.microsoft.walletlibrary.mappings.toVerifiedIdIssuanceRequest
import com.microsoft.walletlibrary.requests.VerifiedIdRequest
import com.microsoft.walletlibrary.requests.rawrequests.OpenIdRawRequest

/**
 * OIDC protocol specific implementation of RequestHandler. It can handle OpenID raw request and returns a VerifiedIdRequest.
 */
class OpenIdRequestHandler : RequestHandler<OpenIdRawRequest> {

    override fun handleRequest(rawRequest: OpenIdRawRequest): VerifiedIdRequest {
        return rawRequest.toVerifiedIdIssuanceRequest()
    }
}