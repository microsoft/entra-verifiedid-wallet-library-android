/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary

import android.content.Context
import android.content.pm.PackageManager
import com.microsoft.walletlibrary.did.sdk.VerifiableCredentialSdk
import com.microsoft.walletlibrary.did.sdk.credential.service.protectors.TokenSigner
import com.microsoft.walletlibrary.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.walletlibrary.did.sdk.datasource.network.apis.HttpAgentApiProvider
import com.microsoft.walletlibrary.did.sdk.identifier.resolvers.RootOfTrustResolver
import com.microsoft.walletlibrary.did.sdk.util.HttpAgentUtils
import com.microsoft.walletlibrary.requests.RequestHandlerFactory
import com.microsoft.walletlibrary.requests.RequestResolverFactory
import com.microsoft.walletlibrary.requests.handlers.OpenId4VCIRequestHandler
import com.microsoft.walletlibrary.requests.handlers.OpenIdRequestHandler
import com.microsoft.walletlibrary.requests.handlers.RequestHandler
import com.microsoft.walletlibrary.requests.resolvers.OpenIdURLRequestResolver
import com.microsoft.walletlibrary.requests.resolvers.RequestResolver
import com.microsoft.walletlibrary.requests.styles.BasicVerifiedIdStyle
import com.microsoft.walletlibrary.requests.styles.VerifiedIdStyle
import com.microsoft.walletlibrary.util.LibraryConfiguration
import com.microsoft.walletlibrary.util.PreviewFeatureFlags
import com.microsoft.walletlibrary.util.WalletLibraryLogger
import com.microsoft.walletlibrary.util.WalletLibraryVCSDKLogConsumer
import com.microsoft.walletlibrary.util.http.httpagent.IHttpAgent
import com.microsoft.walletlibrary.util.http.httpagent.OkHttpAgent
import com.microsoft.walletlibrary.verifiedid.OpenId4VciVerifiedId
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
    private var httpAgent: IHttpAgent = OkHttpAgent()
    private val requestResolvers = mutableListOf<RequestResolver>()
    private val requestHandlers = mutableListOf<RequestHandler>()
    private val previewFeatureFlagsSupported = mutableListOf<String>()
    private val jsonSerializer = Json {
        serializersModule = SerializersModule {
            polymorphic(VerifiedId::class) {
                subclass(VerifiableCredential::class)
                subclass(OpenId4VciVerifiedId::class)
            }
            polymorphic(VerifiedIdStyle::class) {
                subclass(BasicVerifiedIdStyle::class)
            }
        }
        ignoreUnknownKeys = true
        isLenient = true
    }
    private var rootOfTrustResolver: RootOfTrustResolver? = null

    // An optional custom log consumer can be passed to be used by VerifiedIdClient.
    fun with(logConsumer: WalletLibraryLogger.Consumer) {
        logger.addConsumer(logConsumer)
    }

    fun with(rootOfTrustResolver: RootOfTrustResolver) {
        this.rootOfTrustResolver = rootOfTrustResolver
    }

    fun with(httpAgent: IHttpAgent): VerifiedIdClientBuilder {
        this.httpAgent = httpAgent
        return this
    }

    // An optional method to provide a list of preview features to be supported by the client.
    fun with(previewFeatureFlagsToSupport: List<String>): VerifiedIdClientBuilder {
        previewFeatureFlagsSupported.addAll(previewFeatureFlagsToSupport)
        return this
    }

    // Configures and returns VerifiedIdClient with the configurations provided in builder class.
    fun build(): VerifiedIdClient {
        val vcSdkLogConsumer = WalletLibraryVCSDKLogConsumer(logger)
        val userAgentInfo = getUserAgent(context)
        val walletLibraryVersionInfo = getWalletLibraryVersionInfo()
        VerifiableCredentialSdk.init(
            context,
            logConsumer = vcSdkLogConsumer,
            userAgentInfo = userAgentInfo,
            walletLibraryVersionInfo = walletLibraryVersionInfo,
            httpAgent = httpAgent,
            rootOfTrustResolver = rootOfTrustResolver
        )

        val apiProvider = HttpAgentApiProvider(
            this.httpAgent,
            HttpAgentUtils(
                userAgentInfo,
                walletLibraryVersionInfo,
                VerifiableCredentialSdk.correlationVectorService
            ),
            jsonSerializer
        )
        val previewFeatureFlags = PreviewFeatureFlags(previewFeatureFlagsSupported)
        val keyStore = EncryptedKeyStore(context)
        val tokenSigner = TokenSigner(keyStore)
        val libraryConfiguration =
            LibraryConfiguration(previewFeatureFlags, apiProvider, jsonSerializer, tokenSigner, rootOfTrustResolver)

        val requestResolverFactory = RequestResolverFactory()
        registerRequestResolver(OpenIdURLRequestResolver(libraryConfiguration))
        requestResolverFactory.requestResolvers.addAll(requestResolvers)

        val requestHandlerFactory = RequestHandlerFactory()
        registerRequestHandler(OpenIdRequestHandler())
        registerRequestHandler(OpenId4VCIRequestHandler(libraryConfiguration))
        requestHandlerFactory.requestHandlers.addAll(requestHandlers)

        return VerifiedIdClient(
            requestResolverFactory,
            requestHandlerFactory,
            logger,
            jsonSerializer
        )
    }

    private fun registerRequestHandler(requestHandler: RequestHandler) {
        requestHandlers.add(requestHandler)
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
            val applicationInfo =
                packageManager.getApplicationInfo(applicationContext.packageName, 0)
            val appName = packageManager.getApplicationLabel(applicationInfo).toString()
            val packageInfo = packageManager.getPackageInfo(applicationContext.packageName, 0)
            appName + "/" + packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            WalletLibraryLogger.e("Error getting version name.", e)
            ""
        }
    }
}