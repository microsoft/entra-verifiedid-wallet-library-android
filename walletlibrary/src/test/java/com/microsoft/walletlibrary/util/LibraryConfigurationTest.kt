package com.microsoft.walletlibrary.util

import com.microsoft.walletlibrary.BooleanProvider
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

internal class LibraryConfigurationTest {

    @Test
    fun testIsPreviewFeatureEnabled_WithPreviewFeatureFlagInList_ReturnsTrue() {
        val libraryConfiguration = LibraryConfiguration(
            PreviewFeatureFlags(listOf("OpenID4VCIAccessToken")),
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            mockk()
        )
        assertThat(libraryConfiguration.isPreviewFeatureEnabled("OpenID4VCIAccessToken")).isEqualTo(
            true
        )
    }

    @Test
    fun testIsPreviewFeatureEnabled_WithPreviewFeatureFlagNotInList_ReturnsFalse() {
        val libraryConfiguration =
            LibraryConfiguration(
                PreviewFeatureFlags(),
                mockk(),
                mockk(),
                mockk(),
                mockk(),
                mockk()
            )
        assertThat(libraryConfiguration.isPreviewFeatureEnabled("OpenID4VCIPreAuth")).isEqualTo(
            false
        )
    }

    @Test
    fun testLegacyResolverIsDisabledByDefault() {
        val libraryConfiguration = LibraryConfiguration(
            PreviewFeatureFlags(),
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            mockk()
        )

        assertThat(
            libraryConfiguration.isPreviewFeatureEnabled(
                PreviewFeatureFlags.FEATURE_FLAG_ENABLE_LEGACY_RESOLVER
            )
        ).isFalse()
        assertThat(libraryConfiguration.isDidResolverHardeningEnabled).isTrue()
    }

    @Test
    fun testLegacyResolverDisablesDidResolverHardening() {
        val libraryConfiguration = LibraryConfiguration(
            PreviewFeatureFlags(
                listOf(PreviewFeatureFlags.FEATURE_FLAG_ENABLE_LEGACY_RESOLVER)
            ),
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            mockk()
        )

        assertThat(libraryConfiguration.isDidResolverHardeningEnabled).isFalse()
    }

    @Test
    fun testDynamicProviderChangesAfterConfigurationConstruction() {
        var hardeningEnabled = true
        val libraryConfiguration = LibraryConfiguration(
            PreviewFeatureFlags(),
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            BooleanProvider { hardeningEnabled }
        )

        assertThat(libraryConfiguration.isDidResolverHardeningEnabled).isTrue()

        hardeningEnabled = false

        assertThat(libraryConfiguration.isDidResolverHardeningEnabled).isFalse()
    }
}