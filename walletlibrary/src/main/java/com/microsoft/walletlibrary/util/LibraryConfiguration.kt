package com.microsoft.walletlibrary.util

import com.microsoft.walletlibrary.did.sdk.datasource.db.SdkDatabase
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
    val database: SdkDatabase
) {

    // Determine if a preview feature is enabled.
    fun isPreviewFeatureEnabled(previewFeatureFlag: String): Boolean {
        return previewFeatureFlags.isPreviewFeatureSupported(previewFeatureFlag)
    }
}