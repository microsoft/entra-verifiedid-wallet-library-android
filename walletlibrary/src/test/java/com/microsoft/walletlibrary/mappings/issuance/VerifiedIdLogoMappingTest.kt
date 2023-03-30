package com.microsoft.walletlibrary.mappings.issuance

import com.microsoft.did.sdk.credential.service.models.contracts.display.Logo
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class VerifiedIdLogoMappingTest {
    private val expectedLogoUri = "testLogoUri"
    private val expectedLogoImage = "testLogoImage"
    private val expectedLogoDescription = "testLogoDescription"
    private lateinit var logo: Logo

    init {
        setupInput(expectedLogoUri)
    }

    private fun setupInput(logoUri: String?) {
        logo = Logo(logoUri, expectedLogoImage, expectedLogoDescription)
    }

    @Test
    fun logoMapping_mapLogoFromSdk_ReturnsLogoInLibrary() {
        // Act
        val actualLogo = logo.toLogo()

        // Assert
        assertThat(actualLogo.uri).isEqualTo(expectedLogoUri)
        assertThat(actualLogo.description).isEqualTo(expectedLogoDescription)
    }

    @Test
    fun logoMapping_mapLogoWithNullUriFromSdk_ReturnsLogoWithNullUriInLibrary() {
        // Arrange
        setupInput(null)

        // Act
        val actualLogo = logo.toLogo()

        // Assert
        assertThat(actualLogo.uri).isNull()
        assertThat(actualLogo.description).isEqualTo(expectedLogoDescription)
    }
}