/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests

import com.microsoft.walletlibrary.requests.handlers.RequestHandler
import com.microsoft.walletlibrary.requests.resolvers.RequestResolver

/**
 * RequestHandlerFactory holds a list of RequestHandler objects and returns a handler which is compatible with the provided request resolver.
 */
class RequestHandlerFactory {
    private val requestHandlers: List<RequestHandler> = mutableListOf()

    // Returns the first request handler in the list that is compatible with the provided request resolver.
    fun getHandler(requestResolver: RequestResolver): RequestHandler {
        return requestHandlers.first { requestResolver.canResolve(it) }
    }
}