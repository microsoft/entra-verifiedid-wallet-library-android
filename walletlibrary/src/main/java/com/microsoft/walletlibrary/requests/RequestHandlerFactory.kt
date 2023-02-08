/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests

import com.microsoft.walletlibrary.requests.handlers.RequestHandler
import com.microsoft.walletlibrary.requests.resolvers.RequestResolver
import com.microsoft.walletlibrary.util.HandlerMissingException
import com.microsoft.walletlibrary.util.UnSupportedResolverException

/**
 * RequestHandlerFactory holds a list of RequestHandler objects and returns a handler which is compatible with the provided request resolver.
 */
class RequestHandlerFactory {
    internal val requestHandlers = mutableListOf<RequestHandler>()

    // Returns the first request handler in the list that is compatible with the provided request resolver.
    fun getHandler(requestResolver: RequestResolver): RequestHandler {
        if (requestHandlers.isEmpty()) throw HandlerMissingException("No request handler is registered")
        val compatibleRequestHandlers = requestHandlers.filter { requestResolver.canResolve(it) }
        if (compatibleRequestHandlers.isEmpty()) throw UnSupportedResolverException("No compatible request resolver is registered")
        return compatibleRequestHandlers.first()
    }
}