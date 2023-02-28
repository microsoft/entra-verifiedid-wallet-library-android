package com.microsoft.walletlibrary.mappings.issuance

import com.microsoft.did.sdk.credential.service.models.contracts.display.Logo
import com.microsoft.walletlibrary.mappings.issuance.toLogo
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class LogoMappingTest {
    private val expectedLogoUri = "testLogoUri"
    private val expectedLogoImage = "testLogoImage"
    private val expectedLogoDescription = "testLogoDescription"
    private lateinit var logo: Logo

    init {
        setupInput(expectedLogoUri, expectedLogoImage, expectedLogoDescription)
    }

    private fun setupInput(logoUri: String?, logoImage: String?, logoDescription: String) {
        logo = Logo(logoUri, logoImage, logoDescription)
    }

    @Test
    fun logoMapping_mapLogoFromSdk_ReturnsLogoInLibrary() {
        // Act
        val actualLogo = logo.toLogo()

        // Assert
        assertThat(actualLogo.uri).isEqualTo(expectedLogoUri)
        assertThat(actualLogo.image).isEqualTo(expectedLogoImage)
        assertThat(actualLogo.description).isEqualTo(expectedLogoDescription)
    }

    @Test
    fun logoMapping_mapLogoWithNullUriFromSdk_ReturnsLogoWithNullUriInLibrary() {
        // Arrange
        setupInput(null, expectedLogoImage, expectedLogoDescription)

        // Act
        val actualLogo = logo.toLogo()

        // Assert
        assertThat(actualLogo.uri).isNull()
        assertThat(actualLogo.image).isEqualTo(expectedLogoImage)
        assertThat(actualLogo.description).isEqualTo(expectedLogoDescription)
    }

    @Test
    fun logoMapping_mapLogoWithNullImageFromSdk_ReturnsLogoWithNullImageInLibrary() {
        // Arrange
        setupInput(expectedLogoUri, null, expectedLogoDescription)

        // Act
        val actualLogo = logo.toLogo()

        // Assert
        assertThat(actualLogo.uri).isEqualTo(expectedLogoUri)
        assertThat(actualLogo.image).isNull()
        assertThat(actualLogo.description).isEqualTo(expectedLogoDescription)
    }
}