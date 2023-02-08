/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests

import com.microsoft.walletlibrary.VerifiedIdClientInput
import com.microsoft.walletlibrary.requests.resolvers.RequestResolver

/**
 * RequestResolverFactory holds a list of RequestResolver objects and returns a resolver that can resolve the provided VerifiedIdClientInput.
 */
class RequestResolverFactory {
    private val requestResolvers: List<RequestResolver> = mutableListOf()

    // Returns the first resolver in the list that can resolve the provided VerifiedIdClientInput.
    fun getResolver(verifiedIdClientInput: VerifiedIdClientInput): RequestResolver {
        return requestResolvers.first { it.canResolve(verifiedIdClientInput) }
    }
}