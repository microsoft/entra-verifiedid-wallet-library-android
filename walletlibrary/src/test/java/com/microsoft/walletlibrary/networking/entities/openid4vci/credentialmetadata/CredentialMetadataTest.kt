package com.microsoft.walletlibrary.networking.entities.openid4vci.credentialmetadata

import android.content.res.Configuration
import android.content.res.Resources
import androidx.core.os.ConfigurationCompat
import androidx.core.os.LocaleListCompat
import com.microsoft.walletlibrary.networking.entities.openid4vci.credentialoffer.CredentialOffer
import com.microsoft.walletlibrary.networking.entities.openid4vci.credentialoffer.CredentialOfferGrant
import com.microsoft.walletlibrary.requests.styles.VerifiedIdManifestIssuerStyle
import com.microsoft.walletlibrary.util.OpenId4VciValidationException
import com.microsoft.walletlibrary.util.VerifiedIdExceptions
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.Test

class CredentialMetadataTest {
    private val mockCredentialConfiguration1 = mockk<CredentialConfiguration>()
    private val credentialConfigurationId1 = "credential_id1"
    private val mockCredentialConfiguration2 = mockk<CredentialConfiguration>()
    private val credentialConfigurationId2 = "credential_id2"
    private val authorizationServer1 = "https://authorization_server1.com/qwerty/v2.0"
    private val credentialMetadata = CredentialMetadata(
        authorization_servers = listOf(authorizationServer1),
        credential_configurations_supported = mapOf(
            credentialConfigurationId1 to mockCredentialConfiguration1,
            credentialConfigurationId2 to mockCredentialConfiguration2
        )
    )
    private val credentialOffer = CredentialOffer(
        credential_issuer = "metadata_url",
        issuer_session = "request_state",
        credential_configuration_ids = listOf(credentialConfigurationId1),
        grants = mapOf(
            "authorization_code" to CredentialOfferGrant(
                authorizationServer1
            )
        )
    )
    private val mockLocaleListCompat = mockk<LocaleListCompat>()

    @Test
    fun getSupportedCredentialConfigurations_HasMatchingConfig_ReturnsMatchingConfig() {
        // Act
        val actualSupportedCredentialConfigurations =
            credentialMetadata.getSupportedCredentialConfigurations(
                listOf(credentialConfigurationId1)
            )

        // Assert
        assertThat(actualSupportedCredentialConfigurations).containsExactly(
            mockCredentialConfiguration1
        )
    }

    @Test
    fun getSupportedCredentialConfigurations_HasNoMatchingConfig_ReturnsEmptyList() {
        // Act
        val actualSupportedCredentialConfigurations =
            credentialMetadata.getSupportedCredentialConfigurations(
                listOf("non_matching_credential_id")
            )

        // Assert
        assertThat(actualSupportedCredentialConfigurations).isEmpty()
    }

    @Test
    fun getSupportedCredentialConfigurations_ConfigInMetadataIsNull_ReturnsEmptyList() {
        // Arrange
        val credentialMetadata = CredentialMetadata()

        // Act
        val actualSupportedCredentialConfigurations =
            credentialMetadata.getSupportedCredentialConfigurations(
                listOf(credentialConfigurationId1)
            )

        // Assert
        assertThat(actualSupportedCredentialConfigurations).isEmpty()
    }

    @Test
    fun validateAuthorizationServers_AuthorizationServerInConfigAndMetadata_ValidationPasses() {
        // Arrange
        val credentialMetadata = CredentialMetadata(
            authorization_servers = listOf("http://authorization_server1.com/qwerty/oauth2/v2.0/token"),
            credential_configurations_supported = mapOf(
                credentialConfigurationId1 to mockCredentialConfiguration1,
                credentialConfigurationId2 to mockCredentialConfiguration2
            )
        )
        val credentialOffer = CredentialOffer(
            credential_issuer = "metadata_url",
            issuer_session = "request_state",
            credential_configuration_ids = listOf(credentialConfigurationId1),
            grants = mapOf(
                "authorization_code" to CredentialOfferGrant(
                    authorizationServer1
                )
            )
        )

        // Act
        assertThatCode { credentialMetadata.validateAuthorizationServers(credentialOffer) }.doesNotThrowAnyException()
    }

    @Test
    fun validateAuthorizationServers_AuthorizationServerInConfigNotInMetadata_ThrowsException() {
        // Arrange
        val credentialMetadata = CredentialMetadata(
            authorization_servers = listOf("http://authorization_server1.com/abcdefg/oauth2/v2.0/token"),
            credential_configurations_supported = mapOf(
                credentialConfigurationId1 to mockCredentialConfiguration1,
                credentialConfigurationId2 to mockCredentialConfiguration2
            )
        )

        // Act
        val actualResult = runCatching {
            credentialMetadata.validateAuthorizationServers(credentialOffer)
        }

        // Assert
        assertThat(actualResult.isSuccess).isFalse
        val actualException = actualResult.exceptionOrNull()
        assertThat(actualException).isInstanceOf(OpenId4VciValidationException::class.java)
        assertThat(actualException?.message).isEqualTo("Authorization server $authorizationServer1 not found in Credential Metadata.")
        assertThat((actualException as OpenId4VciValidationException).code).isEqualTo(
            VerifiedIdExceptions.MALFORMED_CREDENTIAL_METADATA_EXCEPTION.value
        )
    }

    @Test
    fun validateAuthorizationServers_NoAuthorizationServerInMetadata_ThrowsException() {
        // Arrange
        val credentialMetadata = CredentialMetadata()

        // Act
        val actualResult = runCatching {
            credentialMetadata.validateAuthorizationServers(credentialOffer)
        }

        // Assert
        assertThat(actualResult.isSuccess).isFalse
        val actualException = actualResult.exceptionOrNull()
        assertThat(actualException).isInstanceOf(OpenId4VciValidationException::class.java)
        assertThat(actualException?.message).isEqualTo("Authorization servers property missing in credential metadata.")
        assertThat((actualException as OpenId4VciValidationException).code).isEqualTo(
            VerifiedIdExceptions.INVALID_PROPERTY_EXCEPTION.value
        )
    }

    @Test
    fun verifyIfCredentialIssuerExist_NoCredentialIssuer_ThrowsException() {
        // Arrange
        val credentialMetadata = CredentialMetadata()

        // Act
        val actualResult = runCatching {
            credentialMetadata.verifyIfCredentialIssuerExist()
        }

        // Assert
        assertThat(actualResult.isSuccess).isFalse
        val actualException = actualResult.exceptionOrNull()
        assertThat(actualException).isInstanceOf(OpenId4VciValidationException::class.java)
        assertThat(actualException?.message).isEqualTo("Credential metadata does not contain credential_issuer.")
        assertThat((actualException as OpenId4VciValidationException).code).isEqualTo(
            VerifiedIdExceptions.MALFORMED_CREDENTIAL_METADATA_EXCEPTION.value
        )
    }

    @Test
    fun verifyIfSignedMetadataExist_NoSignedMetadata_ThrowsException() {
        // Arrange
        val credentialMetadata = CredentialMetadata(credential_issuer = "credential_issuer")

        // Act
        val actualResult = runCatching {
            credentialMetadata.verifyIfSignedMetadataExist()
        }

        // Assert
        assertThat(actualResult.isSuccess).isFalse
        val actualException = actualResult.exceptionOrNull()
        assertThat(actualException).isInstanceOf(OpenId4VciValidationException::class.java)
        assertThat(actualException?.message).isEqualTo("Credential metadata does not contain signed_metadata.")
        assertThat((actualException as OpenId4VciValidationException).code).isEqualTo(
            VerifiedIdExceptions.MALFORMED_CREDENTIAL_METADATA_EXCEPTION.value
        )
    }

    @Test
    fun transformLocalizedIssuerDisplayDefinition_WithMatchingLocale_ReturnsRequesterStyle() {
        // Arrange
        setupLocaleInput()
        every { mockLocaleListCompat.get(0) } returns mockk {
            every { language } returns "en"
        }
        every { mockLocaleListCompat.size() } returns 1
        val localizedIssuerDisplayDefinition = LocalizedIssuerDisplayDefinition(
            "issuer_name",
            "en"
        )
        val credentialMetadata = CredentialMetadata(
            authorization_servers = listOf(authorizationServer1),
            credential_configurations_supported = mapOf(
                credentialConfigurationId1 to mockCredentialConfiguration1,
                credentialConfigurationId2 to mockCredentialConfiguration2
            ),
            display = listOf(localizedIssuerDisplayDefinition)
        )

        // Act
        val actualRequesterStyle =
            credentialMetadata.transformLocalizedIssuerDisplayDefinitionToRequesterStyle()

        // Assert
        assertThat(actualRequesterStyle).isInstanceOf(VerifiedIdManifestIssuerStyle::class.java)
        assertThat((actualRequesterStyle as VerifiedIdManifestIssuerStyle).name).isEqualTo("issuer_name")
    }

    @Test
    fun transformLocalizedIssuerDisplayDefinition_WithMatchingLocaleFirstInSettings_ReturnsRequesterStyle() {
        // Arrange
        setupLocaleInput()
        every { mockLocaleListCompat.get(0) } returns mockk {
            every { language } returns "en"
        }
        every { mockLocaleListCompat.get(1) } returns mockk {
            every { language } returns "fr"
        }
        every { mockLocaleListCompat.size() } returns 2
        val localizedIssuerDisplayDefinition = LocalizedIssuerDisplayDefinition(
            "issuer_name",
            "en"
        )
        val credentialMetadata = CredentialMetadata(
            authorization_servers = listOf(authorizationServer1),
            credential_configurations_supported = mapOf(
                credentialConfigurationId1 to mockCredentialConfiguration1,
                credentialConfigurationId2 to mockCredentialConfiguration2
            ),
            display = listOf(localizedIssuerDisplayDefinition)
        )

        // Act
        val actualRequesterStyle =
            credentialMetadata.transformLocalizedIssuerDisplayDefinitionToRequesterStyle()

        // Assert
        assertThat(actualRequesterStyle).isInstanceOf(VerifiedIdManifestIssuerStyle::class.java)
        assertThat((actualRequesterStyle as VerifiedIdManifestIssuerStyle).name).isEqualTo("issuer_name")
    }

    @Test
    fun transformLocalizedIssuerDisplayDefinition_WithMatchingLocaleLastInSettings_ReturnsRequesterStyle() {
        // Arrange
        setupLocaleInput()
        every { mockLocaleListCompat.get(0) } returns mockk {
            every { language } returns "fr"
        }
        every { mockLocaleListCompat.get(1) } returns mockk {
            every { language } returns "en"
        }
        every { mockLocaleListCompat.size() } returns 2
        val localizedIssuerDisplayDefinition = LocalizedIssuerDisplayDefinition(
            "issuer_name",
            "en"
        )
        val credentialMetadata = CredentialMetadata(
            authorization_servers = listOf(authorizationServer1),
            credential_configurations_supported = mapOf(
                credentialConfigurationId1 to mockCredentialConfiguration1,
                credentialConfigurationId2 to mockCredentialConfiguration2
            ),
            display = listOf(localizedIssuerDisplayDefinition)
        )

        // Act
        val actualRequesterStyle =
            credentialMetadata.transformLocalizedIssuerDisplayDefinitionToRequesterStyle()

        // Assert
        assertThat(actualRequesterStyle).isInstanceOf(VerifiedIdManifestIssuerStyle::class.java)
        assertThat((actualRequesterStyle as VerifiedIdManifestIssuerStyle).name).isEqualTo("issuer_name")
    }

    @Test
    fun transformLocalizedIssuerDisplayDefinition_WithNoMatchingLocaleButValidNameInDisplay_ReturnsRequesterStyle() {
        // Arrange
        setupLocaleInput()
        every { mockLocaleListCompat.get(0) } returns mockk {
            every { language } returns "en"
        }
        every { mockLocaleListCompat.size() } returns 1
        val localizedIssuerDisplayDefinition = LocalizedIssuerDisplayDefinition(
            "issuer_name",
            "fr"
        )
        val credentialMetadata = CredentialMetadata(
            authorization_servers = listOf(authorizationServer1),
            credential_configurations_supported = mapOf(
                credentialConfigurationId1 to mockCredentialConfiguration1,
                credentialConfigurationId2 to mockCredentialConfiguration2
            ),
            display = listOf(localizedIssuerDisplayDefinition)
        )

        // Act
        val actualRequesterStyle =
            credentialMetadata.transformLocalizedIssuerDisplayDefinitionToRequesterStyle()

        // Assert
        assertThat(actualRequesterStyle).isInstanceOf(VerifiedIdManifestIssuerStyle::class.java)
        assertThat((actualRequesterStyle as VerifiedIdManifestIssuerStyle).name).isEqualTo("issuer_name")
    }

    @Test
    fun transformLocalizedIssuerDisplayDefinition_WithNoMatchingLocaleAndEmptyNameInDisplay_ReturnsRequesterStyleWithEmptyName() {
        // Arrange
        setupLocaleInput()
        every { mockLocaleListCompat.get(0) } returns mockk {
            every { language } returns "en"
        }
        every { mockLocaleListCompat.size() } returns 1
        val localizedIssuerDisplayDefinition = LocalizedIssuerDisplayDefinition(
            "",
            "fr"
        )
        val credentialMetadata = CredentialMetadata(
            authorization_servers = listOf(authorizationServer1),
            credential_configurations_supported = mapOf(
                credentialConfigurationId1 to mockCredentialConfiguration1,
                credentialConfigurationId2 to mockCredentialConfiguration2
            ),
            display = listOf(localizedIssuerDisplayDefinition)
        )

        // Act
        val actualRequesterStyle =
            credentialMetadata.transformLocalizedIssuerDisplayDefinitionToRequesterStyle()

        // Assert
        assertThat(actualRequesterStyle).isInstanceOf(VerifiedIdManifestIssuerStyle::class.java)
        assertThat((actualRequesterStyle as VerifiedIdManifestIssuerStyle).name).isEqualTo("")
    }

    @Test
    fun transformLocalizedIssuerDisplayDefinition_WithNoMatchingLocaleAndNullDisplay_ReturnsRequesterStyleWithEmptyName() {
        // Arrange
        setupLocaleInput()
        every { mockLocaleListCompat.get(0) } returns mockk {
            every { language } returns "en"
        }
        every { mockLocaleListCompat.size() } returns 1
        val credentialMetadata = CredentialMetadata(
            authorization_servers = listOf(authorizationServer1),
            credential_configurations_supported = mapOf(
                credentialConfigurationId1 to mockCredentialConfiguration1,
                credentialConfigurationId2 to mockCredentialConfiguration2
            )
        )

        // Act
        val actualRequesterStyle =
            credentialMetadata.transformLocalizedIssuerDisplayDefinitionToRequesterStyle()

        // Assert
        assertThat(actualRequesterStyle).isInstanceOf(VerifiedIdManifestIssuerStyle::class.java)
        assertThat((actualRequesterStyle as VerifiedIdManifestIssuerStyle).name).isEqualTo("")
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