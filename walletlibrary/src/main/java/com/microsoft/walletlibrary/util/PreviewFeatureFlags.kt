package com.microsoft.walletlibrary.util

data class PreviewFeatureFlags(val previewFeatureFlags: List<String> = emptyList()) {

    private var supportedPreviewFeatureFlags =
        mutableMapOf(Constants.OPENID4VCI_ACCESS_TOKEN to false, Constants.OPENID4VCI_PRE_AUTH to false)

    init {
        previewFeatureFlags.forEach {
            supportedPreviewFeatureFlags[it] = true
        }
    }

    fun isPreviewFeatureSupported(featureFlag: String): Boolean {
        return supportedPreviewFeatureFlags[featureFlag] ?: false
    }
}