/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary

import android.content.Context
import com.microsoft.did.sdk.VerifiableCredentialSdk
import com.microsoft.walletlibrary.requests.RequestHandlerFactory
import com.microsoft.walletlibrary.requests.RequestResolverFactory
import com.microsoft.walletlibrary.requests.handlers.OpenIdRequestHandler
import com.microsoft.walletlibrary.requests.handlers.RequestHandler
import com.microsoft.walletlibrary.requests.resolvers.OpenIdURLRequestResolver
import com.microsoft.walletlibrary.requests.resolvers.RequestResolver
import com.microsoft.walletlibrary.util.WalletLibraryLogger

/**
 * Entry point to Wallet Library - VerifiedIdClientBuilder configures the builder with required and optional configurations.
 */
class VerifiedIdClientBuilder(private val context: Context) {

    private var logger: WalletLibraryLogger = WalletLibraryLogger
    private val requestResolvers = mutableListOf<RequestResolver>()
    private val requestHandlers = mutableListOf<RequestHandler>()

    // An optional custom log consumer can be passed to be used by VerifiedIdClient.
    fun with(logConsumer: WalletLibraryLogger.Consumer) {
        logger.addConsumer(logConsumer)
    }

    // Configures and returns VerifiedIdClient with the configurations provided in builder class.
    fun build(): VerifiedIdClient {
        val requestResolverFactory = RequestResolverFactory()
        registerRequestResolver(OpenIdURLRequestResolver())
        requestResolverFactory.requestResolvers.addAll(requestResolvers)

        val requestHandlerFactory = RequestHandlerFactory()
        registerRequestHandler(OpenIdRequestHandler())
        requestHandlerFactory.requestHandlers.addAll(requestHandlers)
        
        VerifiableCredentialSdk.init(context)
        return VerifiedIdClient(requestResolverFactory, requestHandlerFactory)
    }

    private fun registerRequestHandler(requestHandler: RequestHandler) {
        requestHandlers.add(requestHandler)
    }

    private fun registerRequestResolver(requestResolver: RequestResolver) {
        requestResolvers.add(requestResolver)
    }
}