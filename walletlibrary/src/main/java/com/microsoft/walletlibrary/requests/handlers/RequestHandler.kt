/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests.handlers

import com.microsoft.walletlibrary.did.sdk.identifier.resolvers.RootOfTrustResolver
import com.microsoft.walletlibrary.requests.VerifiedIdRequest

/**
 * An implementation of RequestHandler is protocol specific. It can handle and process the raw request and returns a VerifiedIdRequest.
 */
internal interface RequestHandler {

    // Indicates whether the provided raw request can be handled by this handler.
    fun canHandle(rawRequest: Any): Boolean

    // Handle and process the provided raw request and returns a VerifiedIdRequest.
    suspend fun handleRequest(rawRequest: Any, rootOfTrustResolver: RootOfTrustResolver? = null): VerifiedIdRequest<*>
}