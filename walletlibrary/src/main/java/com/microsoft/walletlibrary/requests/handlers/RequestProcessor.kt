/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests.handlers

import com.microsoft.walletlibrary.did.sdk.identifier.resolvers.RootOfTrustResolver
import com.microsoft.walletlibrary.requests.VerifiedIdRequest
import com.microsoft.walletlibrary.requests.requestProcessorExtensions.RequestProcessorExtension

/**
 * An implementation of RequestProcessor is protocol specific. It can handle and process the raw request and returns a VerifiedIdRequest.
 */
interface RequestProcessor <T> {
    /**
     * Extensions to this RequestProcessor. All extensions should be called after initial request
     * processing to mutate the request with additional input.
     */
    var requestProcessors: MutableList<RequestProcessorExtension<T>>

    /**
     * Handle and process the provided raw request and returns a VerifiedIdRequest.
     * @param rawRequest A primitive form of the request
     * @return A parsed VerifiedIdRequest
     */
    suspend fun handleRequest(rawRequest: Any, rootOfTrustResolver: RootOfTrustResolver? = null): VerifiedIdRequest<*>

    /**
     * Checks if the input can be processed
     * @param rawRequest A primitive form of the request
     * @return if the input is understood by the processor and can be processed
     */
    suspend fun canHandleRequest(rawRequest: Any): Boolean
}