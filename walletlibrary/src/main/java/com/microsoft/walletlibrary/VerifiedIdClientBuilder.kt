/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary

import android.content.Context
import com.microsoft.walletlibrary.did.sdk.VerifiableCredentialSdk
import android.content.pm.PackageManager
import com.microsoft.walletlibrary.requests.RequestProcessorFactory
import com.microsoft.walletlibrary.requests.RequestResolverFactory
import com.microsoft.walletlibrary.requests.VerifiedIdExtension
import com.microsoft.walletlibrary.requests.handlers.OpenIdRequestProcessor
import com.microsoft.walletlibrary.requests.handlers.RequestProcessor
import com.microsoft.walletlibrary.requests.resolvers.OpenIdURLRequestResolver
import com.microsoft.walletlibrary.requests.resolvers.RequestResolver
import com.microsoft.walletlibrary.requests.styles.BasicVerifiedIdStyle
import com.microsoft.walletlibrary.requests.styles.VerifiedIdStyle
import com.microsoft.walletlibrary.util.WalletLibraryLogger
import com.microsoft.walletlibrary.util.WalletLibraryVCSDKLogConsumer
import com.microsoft.walletlibrary.verifiedid.VerifiableCredential
import com.microsoft.walletlibrary.verifiedid.VerifiedId
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/**
 * Entry point to Wallet Library - VerifiedIdClientBuilder configures the builder with required and optional configurations.
 */
class VerifiedIdClientBuilder(private val context: Context) {

    private var logger: WalletLibraryLogger = WalletLibraryLogger
    private val requestResolvers = mutableListOf<RequestResolver>()
    private val requestProcessors = mutableListOf<RequestProcessor>()
    private val jsonSerializer = Json {
        serializersModule = SerializersModule {
            polymorphic(VerifiedId::class) {
                subclass(VerifiableCredential::class)
            }
            polymorphic(VerifiedIdStyle::class) {
                subclass(BasicVerifiedIdStyle::class)
            }
        }
        ignoreUnknownKeys = true
        isLenient = true
    }

    // An optional custom log consumer can be passed to be used by VerifiedIdClient.
    fun with(logConsumer: WalletLibraryLogger.Consumer) {
        logger.addConsumer(logConsumer)
    }

    fun with(extension: VerifiedIdExtension): VerifiedIdClientBuilder {
        // TODO: Add prefer headers to RequestResolverFactory
        // TODO: lookup RequestProcessors by extension associated types and inject extensions
        return this
    }

    // Configures and returns VerifiedIdClient with the configurations provided in builder class.
    fun build(): VerifiedIdClient {
        val requestResolverFactory = RequestResolverFactory()
        registerRequestResolver(OpenIdURLRequestResolver())
        requestResolverFactory.requestResolvers.addAll(requestResolvers)

        val requestProcessorFactory = RequestProcessorFactory()
        registerRequestHandler(OpenIdRequestProcessor())
        requestProcessorFactory.requestProcessors.addAll(requestProcessors)

        val vcSdkLogConsumer = WalletLibraryVCSDKLogConsumer(logger)
        VerifiableCredentialSdk.init(
            context,
            logConsumer = vcSdkLogConsumer,
            userAgentInfo = getUserAgent(context),
            walletLibraryVersionInfo = getWalletLibraryVersionInfo()
        )
        return VerifiedIdClient(
            requestResolverFactory,
            requestProcessorFactory,
            logger,
            jsonSerializer
        )
    }

    private fun registerRequestHandler(requestProcessor: RequestProcessor) {
        requestProcessors.add(requestProcessor)
    }

    private fun registerRequestResolver(requestResolver: RequestResolver) {
        requestResolvers.add(requestResolver)
    }

    private fun getWalletLibraryVersionInfo(): String {
        return "Android/${BuildConfig.WalletLibraryVersion}"
    }

    private fun getUserAgent(applicationContext: Context): String {
        return try {
            val packageManager = applicationContext.packageManager
            val applicationInfo = packageManager.getApplicationInfo(applicationContext.packageName, 0)
            val appName = packageManager.getApplicationLabel(applicationInfo).toString()
            val packageInfo = packageManager.getPackageInfo(applicationContext.packageName, 0)
            appName + "/" + packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            WalletLibraryLogger.e("Error getting version name.", e)
            ""
        }
    }
}