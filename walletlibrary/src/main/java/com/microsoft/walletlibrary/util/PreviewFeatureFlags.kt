package com.microsoft.walletlibrary.util

data class PreviewFeatureFlags(val previewFeatureFlags: List<String> = emptyList()) {
    private val openID4VCIAccessToken = "OpenID4VCIAccessToken"
    private val openID4VCIPreAuth = "OpenID4VCIPreAuth"

    private var supportedPreviewFeatureFlags =
        mutableMapOf(openID4VCIAccessToken to false, openID4VCIPreAuth to false)

    init {
        previewFeatureFlags.forEach {
            supportedPreviewFeatureFlags[it] = true
        }
    }

    fun isPreviewFeatureSupported(featureFlag: String): Boolean {
        return supportedPreviewFeatureFlags[featureFlag] ?: false
    }
}