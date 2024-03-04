package com.microsoft.walletlibrary.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

internal class LibraryConfigurationTest {

    @Test
    fun testIsPreviewFeatureEnabled_WithPreviewFeatureFlagInList_ReturnsTrue() {
        val libraryConfiguration = LibraryConfiguration(PreviewFeatureFlags(listOf("OpenID4VCIAccessToken")))
        assertThat(libraryConfiguration.isPreviewFeatureEnabled("OpenID4VCIAccessToken")).isEqualTo(true)
    }

    @Test
    fun testIsPreviewFeatureEnabled_WithPreviewFeatureFlagNotInList_ReturnsFalse() {
        val libraryConfiguration = LibraryConfiguration(PreviewFeatureFlags())
        assertThat(libraryConfiguration.isPreviewFeatureEnabled("OpenID4VCIPreAuth")).isEqualTo(false)
    }
}