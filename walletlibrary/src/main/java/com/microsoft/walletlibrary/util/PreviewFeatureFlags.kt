package com.microsoft.walletlibrary.util

/**
 * Preview Features that are supported using feature flags.
 */
data class PreviewFeatureFlags(val previewFeatureFlags: List<String> = emptyList()) {

    companion object {
        // Feature flags for Access Token flow preview feature.
        const val FEATURE_FLAG_OPENID4VCI_ACCESS_TOKEN = "OpenID4VCIAccessToken"

        // Feature flags for Pre-Auth Token flow preview feature.
        const val FEATURE_FLAG_OPENID4VCI_PRE_AUTH = "OpenID4VCIPreAuth"
    }

    private var supportedPreviewFeatureFlags =
        mutableMapOf(FEATURE_FLAG_OPENID4VCI_ACCESS_TOKEN to false, FEATURE_FLAG_OPENID4VCI_PRE_AUTH to false)

    init {
        previewFeatureFlags.forEach {
            supportedPreviewFeatureFlags[it] = true
        }
    }

    // Determine if requested preview feature is supported.
    internal fun isPreviewFeatureSupported(featureFlag: String): Boolean {
        return supportedPreviewFeatureFlags[featureFlag] ?: false
    }
}