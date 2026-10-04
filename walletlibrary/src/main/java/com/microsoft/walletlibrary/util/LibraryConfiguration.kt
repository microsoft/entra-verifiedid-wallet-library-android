package com.microsoft.walletlibrary.util

import com.microsoft.walletlibrary.BooleanProvider
import com.microsoft.walletlibrary.did.sdk.datasource.network.apis.HttpAgentApiProvider
import com.microsoft.walletlibrary.did.sdk.identifier.resolvers.RootOfTrustResolver
import com.microsoft.walletlibrary.identifier.IdentifierFactory
import kotlinx.serialization.json.Json

/**
 * Configuration such as preview feature flags used by the library.
 */
internal class LibraryConfiguration(
    private val previewFeatureFlags: PreviewFeatureFlags,
    val httpAgentApiProvider: HttpAgentApiProvider,
    val serializer: Json,
    val rootOfTrustResolver: RootOfTrustResolver? = null,
    val logger: WalletLibraryLogger,
    val identifierFactory: IdentifierFactory,
    private val didResolverHardeningEnabledProvider: BooleanProvider = BooleanProvider {
        !previewFeatureFlags.isPreviewFeatureSupported(
            PreviewFeatureFlags.FEATURE_FLAG_ENABLE_LEGACY_RESOLVER
        )
    }
) {
    val isDidResolverHardeningEnabled: Boolean
        get() = didResolverHardeningEnabledProvider.get()

    // Determine if a preview feature is enabled.
    fun isPreviewFeatureEnabled(previewFeatureFlag: String): Boolean {
        return previewFeatureFlags.isPreviewFeatureSupported(previewFeatureFlag)
    }
}