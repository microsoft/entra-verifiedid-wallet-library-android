package com.microsoft.walletlibrary.networking.entities.openid4vci.credentialmetadata

import android.content.res.Configuration
import android.content.res.Resources
import androidx.core.os.ConfigurationCompat
import androidx.core.os.LocaleListCompat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CredentialSubjectDefinitionTest {
    private val mockLocaleListCompat = mockk<LocaleListCompat>()

    @Test
    fun getPreferredLocalizedDisplayDefinition_WithMatchingLocale_ReturnsDisplayDefinition() {
        // Arrange
        setupLocaleInput()
        val mockLocalizedDisplayDefinition = mockk<LocalizedDisplayDefinition>()
        val credentialSubjectDefinition =
            CredentialSubjectDefinition(listOf(mockLocalizedDisplayDefinition))
        every { mockLocaleListCompat.get(0) } returns mockk {
            every { language } returns "en"
        }
        every { mockLocaleListCompat.size() } returns 1
        every { mockLocalizedDisplayDefinition.locale } returns "en"

        // Act
        val actualDisplayDefinition =
            credentialSubjectDefinition.getPreferredLocalizedDisplayDefinition()

        // Assert
        assertThat(actualDisplayDefinition).isInstanceOf(LocalizedDisplayDefinition::class.java)
        assertThat(actualDisplayDefinition).isNotNull
        assertThat(actualDisplayDefinition).isEqualTo(mockLocalizedDisplayDefinition)
    }

    @Test
    fun getPreferredLocalizedDisplayDefinition_WithNoMatchingLocaleButHasDisplayDefinition_ReturnsDisplayDefinition() {
        // Arrange
        setupLocaleInput()
        val mockLocalizedDisplayDefinition = mockk<LocalizedDisplayDefinition>()
        val credentialSubjectDefinition = CredentialSubjectDefinition(listOf(mockLocalizedDisplayDefinition))
        every { mockLocaleListCompat.get(0) } returns mockk {
            every { language } returns "en"
        }
        every { mockLocaleListCompat.size() } returns 1
        every { mockLocalizedDisplayDefinition.locale } returns "fr"

        // Act
        val actualDisplayDefinition =
            credentialSubjectDefinition.getPreferredLocalizedDisplayDefinition()

        // Assert
        assertThat(actualDisplayDefinition).isInstanceOf(LocalizedDisplayDefinition::class.java)
        assertThat(actualDisplayDefinition).isNotNull
        assertThat(actualDisplayDefinition).isEqualTo(mockLocalizedDisplayDefinition)
    }

    @Test
    fun getPreferredLocalizedDisplayDefinition_WithMatchingLocaleFirstInSettings_ReturnsDisplayDefinition() {
        // Arrange
        setupLocaleInput()
        val mockLocalizedDisplayDefinition = mockk<LocalizedDisplayDefinition>()
        val credentialSubjectDefinition = CredentialSubjectDefinition(listOf(mockLocalizedDisplayDefinition))
        every { mockLocaleListCompat.get(0) } returns mockk {
            every { language } returns "en"
        }
        every { mockLocaleListCompat.get(1) } returns mockk {
            every { language } returns "fr"
        }
        every { mockLocaleListCompat.size() } returns 2
        every { mockLocalizedDisplayDefinition.locale } returns "en"

        // Act
        val actualDisplayDefinition =
            credentialSubjectDefinition.getPreferredLocalizedDisplayDefinition()

        // Assert
        assertThat(actualDisplayDefinition).isInstanceOf(LocalizedDisplayDefinition::class.java)
        assertThat(actualDisplayDefinition).isNotNull
        assertThat(actualDisplayDefinition).isEqualTo(mockLocalizedDisplayDefinition)
    }

    @Test
    fun getPreferredLocalizedDisplayDefinition_WithMatchingLocaleLastInSettings_ReturnsDisplayDefinition() {
        // Arrange
        setupLocaleInput()
        val mockLocalizedDisplayDefinition = mockk<LocalizedDisplayDefinition>()
        val credentialSubjectDefinition = CredentialSubjectDefinition(listOf(mockLocalizedDisplayDefinition))
        every { mockLocaleListCompat.get(0) } returns mockk {
            every { language } returns "fr"
        }
        every { mockLocaleListCompat.get(1) } returns mockk {
            every { language } returns "en"
        }
        every { mockLocaleListCompat.size() } returns 2
        every { mockLocalizedDisplayDefinition.locale } returns "en"

        // Act
        val actualDisplayDefinition =
            credentialSubjectDefinition.getPreferredLocalizedDisplayDefinition()

        // Assert
        assertThat(actualDisplayDefinition).isInstanceOf(LocalizedDisplayDefinition::class.java)
        assertThat(actualDisplayDefinition).isNotNull
        assertThat(actualDisplayDefinition).isEqualTo(mockLocalizedDisplayDefinition)
    }

    @Test
    fun getPreferredLocalizedDisplayDefinition_WithNoMatchingLocaleAndEmptyDisplayDefinition_ReturnsDisplayDefinition() {
        // Arrange
        setupLocaleInput()
        val credentialSubjectDefinition = CredentialSubjectDefinition(emptyList())
        every { mockLocaleListCompat.get(0) } returns mockk {
            every { language } returns "en"
        }
        every { mockLocaleListCompat.size() } returns 1

        // Act
        val actualDisplayDefinition =
            credentialSubjectDefinition.getPreferredLocalizedDisplayDefinition()

        // Assert
        assertThat(actualDisplayDefinition).isNull()
    }

    private fun setupLocaleInput() {
        mockkStatic(Resources::class)
        val mockResources = mockk<Resources>()
        val mockConfiguration = mockk<Configuration>()
        every { Resources.getSystem() } returns mockResources.also { every { it.configuration } returns mockConfiguration }
        mockkStatic(ConfigurationCompat::class)
        every { ConfigurationCompat.getLocales(mockConfiguration) } returns mockLocaleListCompat
    }
}