/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*//*


package com.microsoft.walletlibrary.requests

import com.microsoft.walletlibrary.requests.handlers.RequestHandler
import com.microsoft.walletlibrary.util.HandlerMissingException
import com.microsoft.walletlibrary.util.UnSupportedRawRequestException

*/
/**
 * RequestHandlerFactory holds a list of RequestHandler objects and returns a handler which is compatible with the provided request resolver.
 *//*

class RequestHandlerFactory {
    internal val requestHandlers = mutableListOf<RequestHandler>()

    // Returns the first request handler that supports the provided raw request.
    internal fun getHandler(rawRequest: Any): RequestHandler {
        if (requestHandlers.isEmpty()) throw HandlerMissingException("No request handler is registered")
        val compatibleRequestHandlers = requestHandlers.filter { it.canHandle(rawRequest) }
        if (compatibleRequestHandlers.isEmpty()) throw UnSupportedRawRequestException("No registered request handler can handle this raw request")
        return compatibleRequestHandlers.first()
    }
}*/
