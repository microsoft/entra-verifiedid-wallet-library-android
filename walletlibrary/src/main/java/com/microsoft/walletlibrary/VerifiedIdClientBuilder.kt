/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary

import android.content.Context
import com.microsoft.did.sdk.VerifiableCredentialSdk
import com.microsoft.did.sdk.util.log.SdkLog
import com.microsoft.walletlibrary.requests.RequestHandlerFactory
import com.microsoft.walletlibrary.requests.RequestResolverFactory
import com.microsoft.walletlibrary.requests.handlers.OpenIdRequestHandler
import com.microsoft.walletlibrary.requests.resolvers.OpenIdURLRequestResolver

/**
 * Entry point to Wallet Library - VerifiedIdClientBuilder configures the builder with required and optional configurations.
 */
class VerifiedIdClientBuilder(private val context: Context) {

    private var walletLogConsumer: SdkLog.Consumer? = null

    // An optional custom log consumer can be passed to be used by VerifiedIdClient.
    fun with(logConsumer: SdkLog.Consumer) {
        walletLogConsumer = logConsumer
    }

    // Configures and returns VerifiedIdClient with the configurations provided in builder class.
    fun build(): VerifiedIdClient {
        val requestResolverFactory = RequestResolverFactory()
        requestResolverFactory.requestResolvers.add(OpenIdURLRequestResolver())
        val requestHandlerFactory = RequestHandlerFactory()
        requestHandlerFactory.requestHandlers.add(OpenIdRequestHandler())
        VerifiableCredentialSdk.init(context)
        return VerifiedIdClient(requestResolverFactory, requestHandlerFactory)
    }
}