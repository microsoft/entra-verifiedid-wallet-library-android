/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests

import com.microsoft.walletlibrary.requests.input.VerifiedIdRequestInput
import com.microsoft.walletlibrary.requests.resolvers.RequestResolver
import com.microsoft.walletlibrary.util.ResolverMissingException
import com.microsoft.walletlibrary.util.UnSupportedInputException

/**
 * RequestResolverFactory holds a list of RequestResolver objects and returns a resolver that can resolve the provided VerifiedIdClientInput.
 */
class RequestResolverFactory {
    internal val requestResolvers = mutableListOf<RequestResolver>()

    // Returns the first resolver in the list that can resolve the provided VerifiedIdClientInput.
    internal fun getResolver(verifiedIdRequestInput: VerifiedIdRequestInput): RequestResolver {
        if (requestResolvers.isEmpty()) throw ResolverMissingException("No request resolver is registered")
        val compatibleRequestResolvers = requestResolvers.filter { it.canResolve(verifiedIdRequestInput) }
        if (compatibleRequestResolvers.isEmpty()) throw UnSupportedInputException("No compatible request resolver is registered for this input")
        return compatibleRequestResolvers.first()
    }
}