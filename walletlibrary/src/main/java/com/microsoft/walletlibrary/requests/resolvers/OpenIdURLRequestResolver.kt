/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests.resolvers

import com.microsoft.walletlibrary.did.sdk.identifier.resolvers.RootOfTrustResolver
import com.microsoft.walletlibrary.requests.handlers.OpenIdRequestProcessor
import com.microsoft.walletlibrary.requests.handlers.RequestProcessor
import com.microsoft.walletlibrary.requests.input.VerifiedIdRequestInput
import com.microsoft.walletlibrary.requests.input.VerifiedIdRequestURL
import com.microsoft.walletlibrary.requests.rawrequests.OpenIdRawRequest
import com.microsoft.walletlibrary.util.Constants
import com.microsoft.walletlibrary.util.UnSupportedVerifiedIdRequestInputException
import com.microsoft.walletlibrary.wrapper.OpenIdResolver

/**
 * Implementation of RequestResolver specific to OIDCRequestHandler and VerifiedIdRequestURL as RequestInput.
 * It can resolve a VerifiedIdRequestInput and return a OIDC raw request.
 */
internal class OpenIdURLRequestResolver : RequestResolver {

    // Indicates whether the raw request returned by this resolver can be handled by provided handler.
    override fun canResolve(requestProcessor: RequestProcessor): Boolean {
        if (requestProcessor is OpenIdRequestProcessor) return true
        return false
    }

    // Indicates whether this resolver can resolve the provided input.
    override fun canResolve(verifiedIdRequestInput: VerifiedIdRequestInput): Boolean {
        if (verifiedIdRequestInput !is VerifiedIdRequestURL) return false
        if (verifiedIdRequestInput.url.scheme == Constants.OPENID_SCHEME) return true
        return false
    }

    // Resolves the provided input and returns a raw request.
    override suspend fun resolve(
        verifiedIdRequestInput: VerifiedIdRequestInput,
        rootOfTrustResolver: RootOfTrustResolver?
    ): OpenIdRawRequest {
        if (verifiedIdRequestInput !is VerifiedIdRequestURL) throw UnSupportedVerifiedIdRequestInputException(
            "Provided VerifiedIdRequestInput is not supported."
        )
        return OpenIdResolver.getRequest(verifiedIdRequestInput.url.toString(), rootOfTrustResolver)
    }
}