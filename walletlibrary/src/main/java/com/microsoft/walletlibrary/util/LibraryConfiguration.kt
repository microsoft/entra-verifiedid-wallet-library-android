package com.microsoft.walletlibrary.util

/**
 * Configuration such as logger, preview feature flags etc. used by the library.
 */
internal class LibraryConfiguration(private val previewFeatureFlags: PreviewFeatureFlags) {

    // Determine if a preview feature is enabled.
    fun isPreviewFeatureEnabled(previewFeatureFlag: String): Boolean {
        return previewFeatureFlags.isPreviewFeatureSupported(previewFeatureFlag)
    }
}