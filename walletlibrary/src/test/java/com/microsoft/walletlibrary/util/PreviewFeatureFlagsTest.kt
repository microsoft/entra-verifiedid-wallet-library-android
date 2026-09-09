package com.microsoft.walletlibrary.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

internal class PreviewFeatureFlagsTest {

    @Test
    fun didResolverHardeningIsEnabledByDefault() {
        val previewFeatureFlags = PreviewFeatureFlags()

        assertThat(
            previewFeatureFlags.isPreviewFeatureSupported(
                PreviewFeatureFlags.FEATURE_FLAG_DID_RESOLVER_HARDENING
            )
        ).isTrue()
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
    fun didResolverHardeningIsDisabledWhenIncludedInDisabledList() {
        val previewFeatureFlags = PreviewFeatureFlags(
            emptyList(),
            listOf(PreviewFeatureFlags.FEATURE_FLAG_DID_RESOLVER_HARDENING)
        )

        assertThat(
            previewFeatureFlags.isPreviewFeatureSupported(
                PreviewFeatureFlags.FEATURE_FLAG_DID_RESOLVER_HARDENING
            )
        ).isFalse()
    }

    @Test
    fun disabledFeatureTakesPrecedenceOverEnabledFeature() {
        val previewFeatureFlags = PreviewFeatureFlags(
            listOf(PreviewFeatureFlags.FEATURE_FLAG_DID_RESOLVER_HARDENING),
            listOf(PreviewFeatureFlags.FEATURE_FLAG_DID_RESOLVER_HARDENING)
        )

        assertThat(
            previewFeatureFlags.isPreviewFeatureSupported(
                PreviewFeatureFlags.FEATURE_FLAG_DID_RESOLVER_HARDENING
            )
        ).isFalse()
    }
}
