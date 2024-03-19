package com.microsoft.walletlibrary.networking.entities.openid4vci.credentialmetadata

import android.content.res.Configuration
import android.content.res.Resources
import androidx.core.os.ConfigurationCompat
import androidx.core.os.LocaleListCompat
import com.microsoft.walletlibrary.requests.styles.BasicVerifiedIdStyle
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CredentialConfigurationTest {

    private val mockLocaleListCompat = mockk<LocaleListCompat>()
    private val credentialConfiguration = CredentialConfiguration(
        display = listOf(
            LocalizedDisplayDefinition(
                locale = "en",
                name = "English Name",
                description = "English Description",
                logo = LogoDisplayDefinition(
                    uri = "English Logo",
                    alt_text = "English Alt Text"
                )
            ),
            LocalizedDisplayDefinition(
                locale = "fr",
                name = "French Name",
                description = "French Description",
                logo = LogoDisplayDefinition(
                    uri = "French Logo",
                    alt_text = "French Alt Text"
                )
            )
        )
    )

    init {
        setupInput()
    }

    @Test
    fun getVerifiedIdStyleInPreferredLocale_MatchingLocaleOccursFirstInDisplay_ReturnsMatchingStyle() {
        //Arrange
        every { mockLocaleListCompat.get(0) } returns mockk {
            every { language } returns "en"
        }
        every { mockLocaleListCompat.size() } returns 1

        //Act
        val result = credentialConfiguration.getVerifiedIdStyleInPreferredLocale("Issuer Name")

        //Assert
        assertThat(result).isInstanceOf(BasicVerifiedIdStyle::class.java)
        assertThat((result as BasicVerifiedIdStyle).issuer).isEqualTo("Issuer Name")
        assertThat(result.name).isEqualTo("English Name")
        assertThat(result.description).isEqualTo("English Description")
    }

    @Test
    fun getVerifiedIdStyleInPreferredLocale_MatchingLocaleOccursLastInDisplay_ReturnsMatchingStyle() {
        //Arrange
        every { mockLocaleListCompat.get(0) } returns mockk {
            every { language } returns "en"
        }
        every { mockLocaleListCompat.size() } returns 1

        //Act
        val result = credentialConfiguration.getVerifiedIdStyleInPreferredLocale("Issuer Name")

        //Assert
        assertThat(result).isInstanceOf(BasicVerifiedIdStyle::class.java)
        assertThat((result as BasicVerifiedIdStyle).issuer).isEqualTo("Issuer Name")
        assertThat(result.name).isEqualTo("English Name")
        assertThat(result.description).isEqualTo("English Description")
    }

    @Test
    fun getVerifiedIdStyleInPreferredLocale_MatchingLocaleOccursFirstInSettings_ReturnsMatchingStyle() {
        //Arrange
        every { mockLocaleListCompat.get(0) } returns mockk {
            every { language } returns "en"
        }
        every { mockLocaleListCompat.get(1) } returns mockk {
            every { language } returns "fr"
        }
        every { mockLocaleListCompat.size() } returns 2

        //Act
        val result = credentialConfiguration.getVerifiedIdStyleInPreferredLocale("Issuer Name")

        //Assert
        assertThat(result).isInstanceOf(BasicVerifiedIdStyle::class.java)
        assertThat((result as BasicVerifiedIdStyle).issuer).isEqualTo("Issuer Name")
        assertThat(result.name).isEqualTo("English Name")
        assertThat(result.description).isEqualTo("English Description")
    }

    @Test
    fun getVerifiedIdStyleInPreferredLocale_MatchingLocaleOccursLastInSettings_ReturnsMatchingStyle() {
        //Arrange
        every { mockLocaleListCompat.get(0) } returns mockk {
            every { language } returns "fr"
        }
        every { mockLocaleListCompat.get(1) } returns mockk {
            every { language } returns "en"
        }
        every { mockLocaleListCompat.size() } returns 2

        //Act
        val result = credentialConfiguration.getVerifiedIdStyleInPreferredLocale("Issuer Name")

        //Assert
        assertThat(result).isInstanceOf(BasicVerifiedIdStyle::class.java)
        assertThat((result as BasicVerifiedIdStyle).issuer).isEqualTo("Issuer Name")
        assertThat(result.name).isEqualTo("French Name")
        assertThat(result.description).isEqualTo("French Description")
    }

    @Test
    fun getVerifiedIdStyleInPreferredLocale_NoMatchingLocale_ReturnsFirstDisplay() {
        //Arrange
        val credentialConfiguration = CredentialConfiguration(
            display = listOf(
                LocalizedDisplayDefinition(
                    locale = "fr",
                    name = "French Name",
                    description = "French Description",
                    logo = LogoDisplayDefinition(
                        uri = "French Logo",
                        alt_text = "French Alt Text"
                    )
                ),
                LocalizedDisplayDefinition(
                    locale = "en",
                    name = "English Name",
                    description = "English Description",
                    logo = LogoDisplayDefinition(
                        uri = "English Logo",
                        alt_text = "English Alt Text"
                    )
                )
            )
        )
        every { mockLocaleListCompat.get(0) } returns mockk {
            every { language } returns "testLocale"
        }
        every { mockLocaleListCompat.size() } returns 1

        //Act
        val result = credentialConfiguration.getVerifiedIdStyleInPreferredLocale("Issuer Name")

        //Assert
        assertThat(result).isInstanceOf(BasicVerifiedIdStyle::class.java)
        assertThat((result as BasicVerifiedIdStyle).issuer).isEqualTo("Issuer Name")
        assertThat(result.name).isEqualTo("French Name")
        assertThat(result.description).isEqualTo("French Description")
    }

    private fun setupInput() {
        mockkStatic(Resources::class)
        val mockResources = mockk<Resources>()
        val mockConfiguration = mockk<Configuration>()
        every { Resources.getSystem() } returns mockResources.also { every { it.configuration } returns mockConfiguration }
        mockkStatic(ConfigurationCompat::class)
        every { ConfigurationCompat.getLocales(mockConfiguration) } returns mockLocaleListCompat
    }
}