/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary

import android.content.Context
import com.microsoft.walletlibrary.did.sdk.VerifiableCredentialSdk
import android.content.pm.PackageManager
import com.microsoft.walletlibrary.did.sdk.identifier.resolvers.RootOfTrustResolver
import com.microsoft.walletlibrary.requests.ManifestIssuanceRequest
import com.microsoft.walletlibrary.requests.OpenIdPresentationRequest
import com.microsoft.walletlibrary.interceptor.HttpInterceptor
import com.microsoft.walletlibrary.requests.RequestHandlerFactory
import com.microsoft.walletlibrary.requests.RequestResolverFactory
import com.microsoft.walletlibrary.requests.VerifiedIdIssuanceRequest
import com.microsoft.walletlibrary.requests.VerifiedIdPresentationRequest
import com.microsoft.walletlibrary.requests.VerifiedIdRequest
import com.microsoft.walletlibrary.requests.handlers.OpenIdRequestHandler
import com.microsoft.walletlibrary.requests.handlers.RequestHandler
import com.microsoft.walletlibrary.requests.requirements.AccessTokenRequirement
import com.microsoft.walletlibrary.requests.requirements.GroupRequirement
import com.microsoft.walletlibrary.requests.requirements.IdTokenRequirement
import com.microsoft.walletlibrary.requests.requirements.PinRequirement
import com.microsoft.walletlibrary.requests.requirements.Requirement
import com.microsoft.walletlibrary.requests.requirements.SelfAttestedClaimRequirement
import com.microsoft.walletlibrary.requests.requirements.VerifiedIdRequirement
import com.microsoft.walletlibrary.requests.resolvers.OpenIdURLRequestResolver
import com.microsoft.walletlibrary.requests.resolvers.RequestResolver
import com.microsoft.walletlibrary.requests.styles.BasicVerifiedIdStyle
import com.microsoft.walletlibrary.requests.styles.OpenIdVerifierStyle
import com.microsoft.walletlibrary.requests.styles.RequesterStyle
import com.microsoft.walletlibrary.requests.styles.VerifiedIdManifestIssuerStyle
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
    private val requestHandlers = mutableListOf<RequestHandler>()
    private val httpInterceptors = mutableListOf<HttpInterceptor>()
    private val jsonSerializer = Json {
        serializersModule = SerializersModule {
            polymorphic(VerifiedId::class) {
                subclass(VerifiableCredential::class)
            }
            polymorphic(VerifiedIdStyle::class) {
                subclass(BasicVerifiedIdStyle::class)
            }
            polymorphic(VerifiedIdRequest::class) {
                subclass(ManifestIssuanceRequest::class)
                subclass(OpenIdPresentationRequest::class)
            }
            polymorphic(VerifiedIdIssuanceRequest::class) {
                subclass(ManifestIssuanceRequest::class)
            }
            polymorphic(VerifiedIdPresentationRequest::class) {
                subclass(OpenIdPresentationRequest::class)
            }
            polymorphic(RequesterStyle::class) {
                subclass(VerifiedIdManifestIssuerStyle::class)
                subclass(OpenIdVerifierStyle::class)
            }
            polymorphic(Requirement::class) {
                subclass(AccessTokenRequirement::class)
                subclass(IdTokenRequirement::class)
                subclass(PinRequirement::class)
                subclass(SelfAttestedClaimRequirement::class)
                subclass(GroupRequirement::class)
                subclass(VerifiedIdRequirement::class)
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

    fun with(httpInterceptor: HttpInterceptor): VerifiedIdClientBuilder {
        httpInterceptors.add(httpInterceptor)
        return this
    }

    // Configures and returns VerifiedIdClient with the configurations provided in builder class.
    fun build(): VerifiedIdClient {
        val requestResolverFactory = RequestResolverFactory()
        registerRequestResolver(OpenIdURLRequestResolver())
        requestResolverFactory.requestResolvers.addAll(requestResolvers)

        val requestHandlerFactory = RequestHandlerFactory()
        registerRequestHandler(OpenIdRequestHandler(context, json = jsonSerializer))
        requestHandlerFactory.requestHandlers.addAll(requestHandlers)

        val vcSdkLogConsumer = WalletLibraryVCSDKLogConsumer(logger)
        VerifiableCredentialSdk.init(
            context,
            logConsumer = vcSdkLogConsumer,
            userAgentInfo = getUserAgent(context),
            walletLibraryVersionInfo = getWalletLibraryVersionInfo(),
            interceptors = httpInterceptors
        )
        return VerifiedIdClient(
            requestResolverFactory,
            requestHandlerFactory,
            logger,
            jsonSerializer,
            rootOfTrustResolver
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