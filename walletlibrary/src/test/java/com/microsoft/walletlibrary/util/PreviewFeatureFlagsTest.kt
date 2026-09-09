package com.microsoft.walletlibrary.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

internal class PreviewFeatureFlagsTest {

    @Test
    fun legacyResolverIsDisabledByDefault() {
        val previewFeatureFlags = PreviewFeatureFlags()

        assertThat(
            previewFeatureFlags.isPreviewFeatureSupported(
                PreviewFeatureFlags.FEATURE_FLAG_ENABLE_LEGACY_RESOLVER
            )
        ).isFalse()
    }

    @Test
    fun defaultDisabledFeatureIsEnabledWhenIncludedInEnabledList() {
        val previewFeatureFlags = PreviewFeatureFlags(
            listOf(PreviewFeatureFlags.FEATURE_FLAG_OPENID4VCI_ACCESS_TOKEN)
        )

        assertThat(
            previewFeatureFlags.isPreviewFeatureSupported(
                PreviewFeatureFlags.FEATURE_FLAG_OPENID4VCI_ACCESS_TOKEN
            )
        ).isTrue()
    }

    @Test
    fun legacyResolverIsEnabledWhenIncludedInEnabledList() {
        val previewFeatureFlags = PreviewFeatureFlags(
            listOf(PreviewFeatureFlags.FEATURE_FLAG_ENABLE_LEGACY_RESOLVER)
        )

        assertThat(
            previewFeatureFlags.isPreviewFeatureSupported(
                PreviewFeatureFlags.FEATURE_FLAG_ENABLE_LEGACY_RESOLVER
            )
        ).isTrue()
    }
}
