package com.microsoft.walletlibrary.mappings

import com.microsoft.did.sdk.credential.service.models.attestations.*
import com.microsoft.walletlibrary.requests.requirements.*
import com.microsoft.walletlibrary.util.UnsupportedRequirementTypeException
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

class CredentialAttestationsMappingTest {
    private val credentialAttestations: CredentialAttestations = mockk()
    private val expectedClaimName = "claimName"
    private val claimAttestations = mutableListOf(ClaimAttestation(expectedClaimName, true, "string"))
    private val expectedConfiguration: String = "https://testconfiguration.com"
    private val expectedClientId = "testClientId"
    private val expectedRedirectUri = "testRedirectUri"
    private val expectedScope = "testScope"
    private lateinit var idTokenAttestation: IdTokenAttestation
    private val expectedResourceId = "testResourceId"
    private lateinit var accessTokenAttestation: AccessTokenAttestation
    private val expectedCredentialType = "TestCredential"
    private val expectedAcceptedIssuers = listOf("TestIssuer1")
    private lateinit var presentationAttestation: PresentationAttestation

    private fun setupSelfIssuedAttestation() {
        val selfIssuedAttestation = SelfIssuedAttestation(
            claimAttestations,
            required = false,
            encrypted = false
        )
        every { credentialAttestations.selfIssued } returns selfIssuedAttestation
    }

    private fun setupIdTokenAttestation() {
        idTokenAttestation = IdTokenAttestation(
            claimAttestations,
            expectedConfiguration,
            expectedClientId,
            required = false,
            expectedRedirectUri,
            expectedScope,
            encrypted = false
        )
    }

    private fun setupAccessTokenAttestation() {
        accessTokenAttestation = AccessTokenAttestation(
            claimAttestations,
            expectedConfiguration,
            expectedResourceId,
            required = false,
            expectedRedirectUri,
            expectedScope,
            encrypted = false
        )
    }

    private fun setupPresentationAttestation() {
        presentationAttestation = PresentationAttestation(
            expectedCredentialType,
            issuers = expectedAcceptedIssuers.map { AcceptedIssuer(it) },
            emptyList(),
            required = false,
            encrypted = false,
            claims = emptyList()
        )
    }

    @Test
    fun credentialAttestationMapping_OnlySelfIssuedAttestationPresent_ReturnsSelfAttestedRequirement() {
        // Arrange
        setupSelfIssuedAttestation()
        every { credentialAttestations.idTokens } returns emptyList()
        every { credentialAttestations.accessTokens } returns emptyList()
        every { credentialAttestations.presentations } returns emptyList()

        // Act
        val actualRequirement = credentialAttestations.toRequirement()

        // Assert
        assertThat(actualRequirement)
            .isInstanceOf(SelfAttestedClaimRequirement::class.java)
        assertThat((actualRequirement as SelfAttestedClaimRequirement).claim.first().claim).isEqualTo(expectedClaimName)
        assertThat(actualRequirement.required).isEqualTo(false)
    }

    @Test
    fun credentialAttestationMapping_OnlyIdTokenAttestationPresent_ReturnsIdTokenRequirement() {
        // Arrange
        setupIdTokenAttestation()
        every { credentialAttestations.selfIssued } returns SelfIssuedAttestation()
        every { credentialAttestations.idTokens } returns listOf( idTokenAttestation)
        every { credentialAttestations.accessTokens } returns emptyList()
        every { credentialAttestations.presentations } returns emptyList()

        // Act
        val actualRequirement = credentialAttestations.toRequirement()

        // Assert
        assertThat(actualRequirement).isInstanceOf(IdTokenRequirement::class.java)
        assertThat((actualRequirement as IdTokenRequirement).claims.first().claim).isEqualTo(expectedClaimName)
        assertThat(actualRequirement.required).isEqualTo(false)
    }

    @Test
    fun credentialAttestationMapping_OnlyAccessTokenAttestationPresent_ReturnsAccessTokenRequirement() {
        // Arrange
        setupAccessTokenAttestation()
        every { credentialAttestations.selfIssued } returns SelfIssuedAttestation()
        every { credentialAttestations.idTokens } returns emptyList()
        every { credentialAttestations.accessTokens } returns listOf(accessTokenAttestation)
        every { credentialAttestations.presentations } returns emptyList()

        // Act
        val actualRequirement = credentialAttestations.toRequirement()

        // Assert
        assertThat(actualRequirement).isInstanceOf(AccessTokenRequirement::class.java)
        assertThat((actualRequirement as AccessTokenRequirement).claims.first().claim).isEqualTo(expectedClaimName)
        assertThat(actualRequirement.required).isEqualTo(false)
    }

    @Test
    fun credentialAttestationMapping_OnlyPresentationAttestationPresent_ReturnsVerifiedIdRequirement() {
        // Arrange
        setupPresentationAttestation()
        every { credentialAttestations.selfIssued } returns SelfIssuedAttestation()
        every { credentialAttestations.idTokens } returns emptyList()
        every { credentialAttestations.accessTokens } returns emptyList()
        every { credentialAttestations.presentations } returns listOf(presentationAttestation)

        // Act
        val actualRequirement = credentialAttestations.toRequirement()

        // Assert
        assertThat(actualRequirement).isInstanceOf(VerifiedIdRequirement::class.java)
        assertThat((actualRequirement as VerifiedIdRequirement).types).contains(expectedCredentialType)
        assertThat(actualRequirement.required).isEqualTo(false)
    }

    @Test
    fun credentialAttestationMapping_MultipleAttestationsPresent_ReturnsGroupRequirement() {
        // Arrange
        setupSelfIssuedAttestation()
        setupPresentationAttestation()
        every { credentialAttestations.idTokens } returns emptyList()
        every { credentialAttestations.accessTokens } returns emptyList()
        every { credentialAttestations.presentations } returns listOf(presentationAttestation)

        // Act
        val actualRequirement = credentialAttestations.toRequirement()

        // Assert
        assertThat(actualRequirement).isInstanceOf(GroupRequirement::class.java)
        assertThat((actualRequirement as GroupRequirement).requirementOperator).isEqualTo(GroupRequirementOperator.ALL)
        assertThat(actualRequirement.required).isEqualTo(true)
    }

    @Test
    fun credentialAttestationMapping_NoAttestationPresent_ThrowsException() {
        // Arrange
        every { credentialAttestations.selfIssued } returns SelfIssuedAttestation()
        every { credentialAttestations.idTokens } returns emptyList()
        every { credentialAttestations.accessTokens } returns emptyList()
        every { credentialAttestations.presentations } returns emptyList()

        // Act and Assert
        assertThatThrownBy {
            credentialAttestations.toRequirement()
        }.isInstanceOf(UnsupportedRequirementTypeException::class.java)
    }

}