/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary

import com.microsoft.walletlibrary.requests.RequestHandlerFactory
import com.microsoft.walletlibrary.requests.RequestResolverFactory
import com.microsoft.walletlibrary.requests.VerifiedIdRequest
import com.microsoft.walletlibrary.requests.input.VerifiedIdRequestInput

/**
 * VerifiedIdClient is configured by builder and is used to create requests.
 */
class VerifiedIdClient(
    private val requestResolverFactory: RequestResolverFactory,
    private val requestHandlerFactory: RequestHandlerFactory
) {

    // Creates an issuance or presentation request based on the provided input.
    suspend fun createRequest(verifiedIdRequestInput: VerifiedIdRequestInput): VerifiedIdRequest<*> {
        val requestResolver = requestResolverFactory.getResolver(verifiedIdRequestInput)
        val rawRequest = requestResolver.resolve(verifiedIdRequestInput)
        val requestHandler = requestHandlerFactory.getHandler(requestResolver)
        return requestHandler.handleRequest(rawRequest)
    }
}