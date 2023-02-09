/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests.resolvers

import com.microsoft.did.sdk.VerifiableCredentialSdk
import com.microsoft.walletlibrary.mappings.getRawRequest
import com.microsoft.walletlibrary.requests.handlers.OpenIdRequestHandler
import com.microsoft.walletlibrary.requests.handlers.RequestHandler
import com.microsoft.walletlibrary.requests.input.VerifiedIdRequestInput
import com.microsoft.walletlibrary.requests.input.VerifiedIdRequestURL
import com.microsoft.walletlibrary.requests.rawrequests.OpenIdRawRequest
import com.microsoft.walletlibrary.util.UnSupportedVerifiedIdRequestInputException

/**
 * Implementation of RequestResolver specific to OIDCRequestHandler and VerifiedIdRequestURL as RequestInput.
 * It can resolve a VerifiedIdRequestInput and return a OIDC raw request.
 */
class OpenIdURLRequestResolver : RequestResolver<OpenIdRawRequest> {
    companion object {
        private const val OPENID_SCHEME = "openid-vc"
    }

    // Indicates whether the raw request returned by this resolver can be handled by provided handler.
    override fun canResolve(requestHandler: RequestHandler<OpenIdRawRequest>): Boolean {
        if (requestHandler is OpenIdRequestHandler) return true
        return false
    }

    // Indicates whether this resolver can resolve the provided input.
    override fun canResolve(verifiedIdRequestInput: VerifiedIdRequestInput): Boolean {
        if (verifiedIdRequestInput !is VerifiedIdRequestURL) return false
        if (verifiedIdRequestInput.url.scheme == OPENID_SCHEME) return true
        return false
    }

    // Resolves the provided input and returns a raw request.
    override suspend fun resolve(verifiedIdRequestInput: VerifiedIdRequestInput): OpenIdRawRequest {
        if (verifiedIdRequestInput !is VerifiedIdRequestURL) throw UnSupportedVerifiedIdRequestInputException(
            "Provided VerifiedIdRequestInput is not supported."
        )
        return VerifiableCredentialSdk.presentationService.getRawRequest(verifiedIdRequestInput.url.toString())
    }
}