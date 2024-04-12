/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary

import android.content.Context
import android.content.pm.PackageManager
import com.microsoft.walletlibrary.did.sdk.VerifiableCredentialSdk
import com.microsoft.walletlibrary.did.sdk.datasource.network.apis.HttpAgentApiProvider
import com.microsoft.walletlibrary.did.sdk.identifier.resolvers.RootOfTrustResolver
import com.microsoft.walletlibrary.did.sdk.util.HttpAgentUtils
import com.microsoft.walletlibrary.identifier.IdentifierManager
import com.microsoft.walletlibrary.requests.ManifestIssuanceRequest
import com.microsoft.walletlibrary.requests.OpenIdPresentationRequest
import com.microsoft.walletlibrary.requests.RequestProcessorFactory
import com.microsoft.walletlibrary.requests.RequestResolverFactory
import com.microsoft.walletlibrary.requests.VerifiedIdExtension
import com.microsoft.walletlibrary.requests.VerifiedIdIssuanceRequest
import com.microsoft.walletlibrary.requests.VerifiedIdPresentationRequest
import com.microsoft.walletlibrary.requests.VerifiedIdRequest
import com.microsoft.walletlibrary.requests.handlers.OpenId4VCIRequestHandler
import com.microsoft.walletlibrary.requests.handlers.OpenIdRequestProcessor
import com.microsoft.walletlibrary.requests.handlers.RequestProcessor
import com.microsoft.walletlibrary.requests.handlers.SignedMetadataProcessor
import com.microsoft.walletlibrary.requests.requestProcessorExtensions.RequestProcessorExtension
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
import com.microsoft.walletlibrary.util.LibraryConfiguration
import com.microsoft.walletlibrary.util.PreviewFeatureFlags
import com.microsoft.walletlibrary.util.WalletLibraryLogger
import com.microsoft.walletlibrary.util.WalletLibraryVCSDKLogConsumer
import com.microsoft.walletlibrary.util.http.httpagent.IHttpAgent
import com.microsoft.walletlibrary.util.http.httpagent.OkHttpAgent
import com.microsoft.walletlibrary.verifiedid.VerifiableCredential
import com.microsoft.walletlibrary.verifiedid.VerifiedId
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.jetbrains.annotations.TestOnly

/**
 * Entry point to Wallet Library - VerifiedIdClientBuilder configures the builder with required and optional configurations.
 */
class VerifiedIdClientBuilder(private val context: Context) {

    private var logger: WalletLibraryLogger = WalletLibraryLogger
    private var httpAgent: IHttpAgent = OkHttpAgent()
    private val requestResolvers = mutableListOf<RequestResolver>()
    private val requestProcessors = mutableListOf<RequestProcessor<*>>()
    private val previewFeatureFlagsSupported = mutableListOf<String>()
    private var preferHeaders = mutableListOf<String>()
    private val extensionBuilders = mutableListOf<VerifiedIdExtension>()
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

    fun with(extension: VerifiedIdExtension): VerifiedIdClientBuilder {
        preferHeaders.addAll(extension.prefer)
        extensionBuilders.add(extension)
        return this
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

    // Build the extension configuration for use in extension testing
    @TestOnly
    fun buildExtensionConfiguration(): ExtensionConfiguration {
        val libraryConfiguration = buildLibraryConfiguration()
        return ExtensionConfiguration(libraryConfiguration)
    }

    // Configures and returns VerifiedIdClient with the configurations provided in builder class.
    fun build(): VerifiedIdClient {
        val libraryConfiguration = buildLibraryConfiguration()
        val requestResolverFactory = RequestResolverFactory()
        registerRequestResolver(OpenIdURLRequestResolver(libraryConfiguration, preferHeaders))
        requestResolverFactory.requestResolvers.addAll(requestResolvers)

        val config = ExtensionConfiguration(libraryConfiguration)
        val extensions: List<RequestProcessorExtension<*>> = extensionBuilders.mapNotNull { it.createRequestProcessorExtensions(config) }.flatMap { it }

        val requestProcessorFactory = RequestProcessorFactory()
        registerRequestHandler(OpenIdRequestProcessor(libraryConfiguration), extensions)
        registerRequestHandler(OpenId4VCIRequestHandler(libraryConfiguration, SignedMetadataProcessor(libraryConfiguration)), extensions)
        requestProcessorFactory.requestProcessors.addAll(requestProcessors)

        return VerifiedIdClient(
            requestResolverFactory,
            requestProcessorFactory,
            logger,
            jsonSerializer,
            rootOfTrustResolver
        )
    }

    private fun buildLibraryConfiguration(): LibraryConfiguration {
        val vcSdkLogConsumer = WalletLibraryVCSDKLogConsumer(logger)
        val userAgentInfo = getUserAgent(context)
        val walletLibraryVersionInfo = getWalletLibraryVersionInfo()
        VerifiableCredentialSdk.init(
            context,
            logConsumer = vcSdkLogConsumer,
            userAgentInfo = userAgentInfo,
            walletLibraryVersionInfo = walletLibraryVersionInfo,
            httpAgent = httpAgent
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

        val identifierManager = IdentifierManager(VerifiableCredentialSdk.identifierService)
        val previewFeatureFlags = PreviewFeatureFlags(previewFeatureFlagsSupported)
        return LibraryConfiguration(previewFeatureFlags,
                apiProvider,
                jsonSerializer,
                identifierManager,
                identifierManager.getTokenSigner(),
                logger
            )
    }

    private inline fun <reified T> registerRequestHandler(requestProcessor: RequestProcessor<T>, extensions: List<RequestProcessorExtension<*>>) {
        for (extension in extensions) {
            if (extension.associatedRequestProcessor.isInstance(requestProcessor)) {
                // associatedType has the same <T> parameter for this cast
                @Suppress("UNCHECKED_CAST")
                requestProcessor.requestProcessors.add(extension as RequestProcessorExtension<T>)
            }
        }
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
            val applicationInfo =
                packageManager.getApplicationInfo(applicationContext.packageName, 0)
            val appName = packageManager.getApplicationLabel(applicationInfo).toString()
            val packageInfo = packageManager.getPackageInfo(applicationContext.packageName, 0)
            "Microsoft-Authenticator" + "/" + packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            WalletLibraryLogger.e("Error getting version name.", e)
            ""
        }
    }
}