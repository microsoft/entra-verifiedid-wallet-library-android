/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests.resolvers

import com.microsoft.walletlibrary.requests.handlers.RequestHandler
import com.microsoft.walletlibrary.requests.input.VerifiedIdRequestInput

/**
 * An implementation RequestResolver is specific to a certain type of RequestHandler and VerifiedIdClientInput.
 * It can resolve a VerifiedIdClientInput and return a raw request.
 */
interface RequestResolver<out T> {

    // Indicates whether the raw request returned by this resolver can be handled by provided handler.
    fun canResolve(requestHandler: RequestHandler<T>): Boolean

    // Indicates whether this resolver can resolve the provided input.
    fun canResolve(verifiedIdRequestInput: VerifiedIdRequestInput): Boolean

    // Resolves the provided input and returns a raw request.
    suspend fun resolve(verifiedIdRequestInput: VerifiedIdRequestInput): T
}