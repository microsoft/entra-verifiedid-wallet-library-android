package com.microsoft.walletlibrary.util

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
<<<<<<< HEAD
=======
            mockk(),
            mockk(),
            mockk(),
>>>>>>> dev
            mockk()
        )
        assertThat(libraryConfiguration.isPreviewFeatureEnabled("OpenID4VCIAccessToken")).isEqualTo(
            true
        )
    }

    @Test
    fun testIsPreviewFeatureEnabled_WithPreviewFeatureFlagNotInList_ReturnsFalse() {
        val libraryConfiguration =
<<<<<<< HEAD
            LibraryConfiguration(PreviewFeatureFlags(), mockk(), mockk(), mockk())
=======
            LibraryConfiguration(PreviewFeatureFlags(), mockk(), mockk(), mockk(), mockk(), mockk(), mockk())
>>>>>>> dev
        assertThat(libraryConfiguration.isPreviewFeatureEnabled("OpenID4VCIPreAuth")).isEqualTo(
            false
        )
    }
}