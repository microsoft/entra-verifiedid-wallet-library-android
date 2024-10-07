package com.microsoft.walletlibrary.util

import com.microsoft.walletlibrary.did.sdk.credential.service.protectors.TokenSigner
import com.microsoft.walletlibrary.did.sdk.datasource.network.apis.HttpAgentApiProvider
import com.microsoft.walletlibrary.did.sdk.identifier.resolvers.RootOfTrustResolver
import kotlinx.serialization.json.Json

/**
 * Configuration such as preview feature flags used by the library.
 */
internal class LibraryConfiguration(
    private val previewFeatureFlags: PreviewFeatureFlags,
    val httpAgentApiProvider: HttpAgentApiProvider,
    val serializer: Json,
    val signer: TokenSigner,
    val rootOfTrustResolver: RootOfTrustResolver? = null
) {

    // Determine if a preview feature is enabled.
    fun isPreviewFeatureEnabled(previewFeatureFlag: String): Boolean {
        return previewFeatureFlags.isPreviewFeatureSupported(previewFeatureFlag)
    }
}