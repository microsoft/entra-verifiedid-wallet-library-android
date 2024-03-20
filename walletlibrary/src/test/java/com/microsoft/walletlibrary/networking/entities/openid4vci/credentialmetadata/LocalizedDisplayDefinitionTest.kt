package com.microsoft.walletlibrary.networking.entities.openid4vci.credentialmetadata

import com.microsoft.walletlibrary.requests.styles.BasicVerifiedIdStyle
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class LocalizedDisplayDefinitionTest {

    @Test
    fun transformToVerifiedIdLogo_ValidLogo_ReturnsVerifiedIdLogoWithValidValues() {
        // Arrange
        val localizedDisplayDefinition = LocalizedDisplayDefinition(
            "name",
            "locale",
            LogoDisplayDefinition("uri", "alt_text"),
            "description",
            "background_color",
            "text_color"
        )

        // Act
        val result = localizedDisplayDefinition.transformToVerifiedIdLogo()

        // Assert
        assertThat(result.url).isEqualTo("uri")
        assertThat(result.altText).isEqualTo("alt_text")
    }

    @Test
    fun transformToVerifiedIdLogo_NullLogo_ReturnsVerifiedIdLogoWithEmptyValues() {
        // Arrange
        val localizedDisplayDefinition = LocalizedDisplayDefinition(
            "name",
            "locale",
            null,
            "description",
            "background_color",
            "text_color"
        )
        // Act
        val result = localizedDisplayDefinition.transformToVerifiedIdLogo()

        // Assert
        assertThat(result.url).isEqualTo("")
        assertThat(result.altText).isEqualTo("")
    }

    @Test
    fun transformToVerifiedIdLogo_NullUriForLogo_ReturnsVerifiedIdLogoWithEmptyUri() {
        // Arrange
        val localizedDisplayDefinition = LocalizedDisplayDefinition(
            "name",
            "locale",
            LogoDisplayDefinition(null, "alt_text"),
            "description",
            "background_color",
            "text_color"
        )

        // Act
        val result = localizedDisplayDefinition.transformToVerifiedIdLogo()

        // Assert
        assertThat(result.url).isEqualTo("")
        assertThat(result.altText).isEqualTo("alt_text")
    }

    @Test
    fun transformToVerifiedIdLogo_NullAltTestForLogo_ReturnsVerifiedIdLogoWithEmptyAltText() {
        // Arrange
        val localizedDisplayDefinition = LocalizedDisplayDefinition(
            "name",
            "locale",
            LogoDisplayDefinition("uri", null),
            "description",
            "background_color",
            "text_color"
        )

        // Act
        val result = localizedDisplayDefinition.transformToVerifiedIdLogo()

        // Assert
        assertThat(result.url).isEqualTo("uri")
        assertThat(result.altText).isEqualTo("")
    }

    @Test
    fun transformToVerifiedIdStyle_ValidValues_ReturnsBasicVerifiedIdStyleWithValidValues() {
        // Arrange
        val localizedDisplayDefinition = LocalizedDisplayDefinition(
            "name",
            "locale",
            LogoDisplayDefinition("uri", "alt_text"),
            "description",
            "background_color",
            "text_color"
        )

        // Act
        val result = localizedDisplayDefinition.transformToVerifiedIdStyle("issuerName")

        // Assert
        assertThat(result).isInstanceOf(BasicVerifiedIdStyle::class.java)
        assertThat((result as BasicVerifiedIdStyle).name).isEqualTo("name")
        assertThat(result.issuer).isEqualTo("issuerName")
        assertThat(result.backgroundColor).isEqualTo("background_color")
        assertThat(result.textColor).isEqualTo("text_color")
        assertThat(result.description).isEqualTo("description")
        assertThat(result.logo).isNotNull
        assertThat(result.logo?.url).isEqualTo("uri")
        assertThat(result.logo?.altText).isEqualTo("alt_text")
    }

    @Test
    fun transformToVerifiedIdStyle_NullLogo_ReturnsBasicVerifiedIdStyleWithEmptyLogoValues() {
        // Arrange
        val localizedDisplayDefinition = LocalizedDisplayDefinition(
            "name",
            "locale",
            null,
            "description",
            "background_color",
            "text_color"
        )

        // Act
        val result = localizedDisplayDefinition.transformToVerifiedIdStyle("issuerName")

        // Assert
        assertThat(result).isInstanceOf(BasicVerifiedIdStyle::class.java)
        assertThat((result as BasicVerifiedIdStyle).name).isEqualTo("name")
        assertThat(result.issuer).isEqualTo("issuerName")
        assertThat(result.backgroundColor).isEqualTo("background_color")
        assertThat(result.textColor).isEqualTo("text_color")
        assertThat(result.description).isEqualTo("description")
        assertThat(result.logo).isNotNull
        assertThat(result.logo?.url).isEqualTo("")
        assertThat(result.logo?.altText).isEqualTo("")
    }

    @Test
    fun transformToVerifiedIdStyle_NullValues_ReturnsBasicVerifiedIdStyleWithEmptyValues() {
        // Arrange
        val localizedDisplayDefinition = LocalizedDisplayDefinition()

        // Act
        val result = localizedDisplayDefinition.transformToVerifiedIdStyle("issuerName")

        // Assert
        assertThat(result).isInstanceOf(BasicVerifiedIdStyle::class.java)
        assertThat((result as BasicVerifiedIdStyle).name).isEqualTo("")
        assertThat(result.issuer).isEqualTo("issuerName")
        assertThat(result.backgroundColor).isEqualTo("")
        assertThat(result.textColor).isEqualTo("")
        assertThat(result.description).isEqualTo("")
        assertThat(result.logo).isNotNull
        assertThat(result.logo?.url).isEqualTo("")
        assertThat(result.logo?.altText).isEqualTo("")
    }
}