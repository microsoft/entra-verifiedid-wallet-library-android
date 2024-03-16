package com.microsoft.walletlibrary.networking.entities.openid4vci

import com.microsoft.walletlibrary.networking.entities.openid4vci.credentialmetadata.CredentialConfiguration
import com.microsoft.walletlibrary.networking.entities.openid4vci.credentialmetadata.CredentialMetadata
import com.microsoft.walletlibrary.networking.entities.openid4vci.credentialoffer.CredentialOffer
import com.microsoft.walletlibrary.networking.entities.openid4vci.credentialoffer.CredentialOfferGrants
import com.microsoft.walletlibrary.requests.styles.VerifiedIdManifestIssuerStyle
import com.microsoft.walletlibrary.util.OpenId4VciValidationException
import com.microsoft.walletlibrary.util.VerifiedIdExceptions
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CredentialMetadataTest {
    private val mockCredentialConfiguration1 = mockk<CredentialConfiguration>()
    private val credentialConfigurationId1 = "credential_id1"
    private val mockCredentialConfiguration2 = mockk<CredentialConfiguration>()
    private val credentialConfigurationId2 = "credential_id2"
    private val authorizationServer1 = "authorizationServer1"
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
            "authorization_code" to CredentialOfferGrants(
                authorizationServer1
            )
        )
    )

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
        val credentialOffer = CredentialOffer(
            credential_issuer = "metadata_url",
            issuer_session = "request_state",
            credential_configuration_ids = listOf(credentialConfigurationId1),
            grants = mapOf(
                "authorization_code" to CredentialOfferGrants(
                    authorizationServer1
                )
            )
        )

        // Act
        credentialMetadata.validateAuthorizationServers(credentialOffer)
    }

    @Test
    fun validateAuthorizationServers_AuthorizationServerInConfigNotInMetadata_ThrowsException() {
        // Arrange
        val credentialMetadata = CredentialMetadata(
            authorization_servers = listOf("non_matching_authorization_server"),
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
        assertThat((actualException as OpenId4VciValidationException).code).isEqualTo(VerifiedIdExceptions.MALFORMED_CREDENTIAL_METADATA_EXCEPTION.value)
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
        assertThat((actualException as OpenId4VciValidationException).code).isEqualTo(VerifiedIdExceptions.INVALID_PROPERTY_EXCEPTION.value)
    }

    @Test
    fun verifyIfCredentialIssuerAndSignedMetadataExist_NoCredentialIssuer_ThrowsException() {
        // Arrange
        val credentialMetadata = CredentialMetadata()

        // Act
        val actualResult = runCatching {
            credentialMetadata.verifyIfCredentialIssuerAndSignedMetadataExist()
        }

        // Assert
        assertThat(actualResult.isSuccess).isFalse
        val actualException = actualResult.exceptionOrNull()
        assertThat(actualException).isInstanceOf(OpenId4VciValidationException::class.java)
        assertThat(actualException?.message).isEqualTo("Credential metadata does not contain credential_issuer.")
        assertThat((actualException as OpenId4VciValidationException).code).isEqualTo(VerifiedIdExceptions.MALFORMED_CREDENTIAL_METADATA_EXCEPTION.value)
    }

    @Test
    fun verifyIfCredentialIssuerAndSignedMetadataExist_NoSignedMetadata_ThrowsException() {
        // Arrange
        val credentialMetadata = CredentialMetadata(credential_issuer = "credential_issuer")

        // Act
        val actualResult = runCatching {
            credentialMetadata.verifyIfCredentialIssuerAndSignedMetadataExist()
        }

        // Assert
        assertThat(actualResult.isSuccess).isFalse
        val actualException = actualResult.exceptionOrNull()
        assertThat(actualException).isInstanceOf(OpenId4VciValidationException::class.java)
        assertThat(actualException?.message).isEqualTo("Credential metadata does not contain signed_metadata.")
        assertThat((actualException as OpenId4VciValidationException).code).isEqualTo(VerifiedIdExceptions.MALFORMED_CREDENTIAL_METADATA_EXCEPTION.value)
    }

    @Test
    fun transformIssuerStyle_NoDisplayDefinition_ReturnsRequesterStyleWithNoIssuerName() {
        // Arrange
        val credentialMetadata = CredentialMetadata()

        // Act
        val actualResult = credentialMetadata.transformLocalizedIssuerDisplayDefinitionToRequesterStyle()

        // Assert
        assertThat(actualResult).isInstanceOf(VerifiedIdManifestIssuerStyle::class.java)
        assertThat(actualResult.name).isEmpty()
    }
}