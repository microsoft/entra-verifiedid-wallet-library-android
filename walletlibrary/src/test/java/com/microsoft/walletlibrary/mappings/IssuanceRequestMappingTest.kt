package com.microsoft.walletlibrary.mappings

import com.microsoft.did.sdk.credential.service.IssuanceRequest
import com.microsoft.did.sdk.credential.service.models.attestations.*
import com.microsoft.did.sdk.credential.service.models.contracts.display.ClaimDescriptor
import com.microsoft.did.sdk.credential.service.models.contracts.display.Logo
import com.microsoft.did.sdk.credential.service.models.linkedDomains.LinkedDomainVerified
import com.microsoft.walletlibrary.requests.requirements.AccessTokenRequirement
import com.microsoft.walletlibrary.requests.requirements.IdTokenRequirement
import com.microsoft.walletlibrary.requests.requirements.SelfAttestedClaimRequirement
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class IssuanceRequestMappingTest {
    private lateinit var issuanceRequest: IssuanceRequest
    private val expectedEntityName = "testIssuer"
    private val expectedLinkedDomainSource = "https://test.com"
    private val credentialAttestations: CredentialAttestations = mockk()
    private val claimAttestations = mutableListOf(ClaimAttestation("name", true, "string"))
    private lateinit var selfIssuedAttestation: SelfIssuedAttestation
    private val expectedConfiguration: String = "https://testconfiguration.com"
    private val expectedClientId = "testClientId"
    private val expectedRedirectUri = "testRedirectUri"
    private val expectedScope = "testScope"
    private lateinit var idTokenAttestation: IdTokenAttestation
    private val expectedResourceId = "testResourceId"
    private lateinit var accessTokenAttestation: AccessTokenAttestation

    init {
        setupInput(true)
    }

    private fun setupInput(logoPresent: Boolean) {
        issuanceRequest = mockk()
        every { issuanceRequest.getAttestations() } returns credentialAttestations
        every { issuanceRequest.entityName } returns expectedEntityName
        every { issuanceRequest.linkedDomainResult } returns LinkedDomainVerified(expectedLinkedDomainSource)
        setupContract()
        setupDisplay()
        setupCardDescriptor(logoPresent)
    }

    private fun setupContract() {
        every { issuanceRequest.contract } returns mockk()
        every { issuanceRequest.contract.display } returns mockk()
    }

    private fun setupDisplay() {
        val claimDescriptor: ClaimDescriptor = mockk()
        every { claimDescriptor.type } returns "type"
        every { claimDescriptor.label } returns "label"
        val claims = mutableMapOf<String, ClaimDescriptor>()
        claims[""] = claimDescriptor
        every { issuanceRequest.contract.display.claims } returns claims
        every { issuanceRequest.contract.display.locale } returns ""
        every { issuanceRequest.contract.display.card } returns mockk()
    }

    private fun setupCardDescriptor(logoPresent: Boolean) {
        every { issuanceRequest.contract.display.card.description } returns "cardDescription"
        every { issuanceRequest.contract.display.card.issuedBy } returns "testIssuer"
        every { issuanceRequest.contract.display.card.backgroundColor } returns "#ffffff"
        every { issuanceRequest.contract.display.card.textColor } returns "#000000"
        every { issuanceRequest.contract.display.card.title } returns ""
        if (logoPresent) {
            val logoMock = mockk<Logo>()
            every { issuanceRequest.contract.display.card.logo } returns logoMock
            setupLogo(logoMock)
        } else
            every { issuanceRequest.contract.display.card.logo } returns null
    }

    private fun setupLogo(logo: Logo) {
        every { logo.uri } returns "testLogoUri"
        every { logo.image } returns "testLogoImage"
        every { logo.description } returns "testLogoDescription"
    }

    private fun setupSelfIssuedAttestation() {
        selfIssuedAttestation = SelfIssuedAttestation(
            claimAttestations,
            required = false,
            encrypted = false
        )
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

    @Test
    fun issuanceRequestMapping_SelfIssuedAttestationPresent_ReturnsSelfAttestedRequirementInOpenIdRequest() {
        // Arrange
        setupSelfIssuedAttestation()
        every { credentialAttestations.selfIssued } returns selfIssuedAttestation
        every { credentialAttestations.idTokens } returns emptyList()
        every { credentialAttestations.accessTokens } returns emptyList()
        every { credentialAttestations.presentations } returns emptyList()

        // Act
        val actualOpenIdRequest = issuanceRequest.toOpenIdIssuanceRequest()

        // Assert
        assertThat(actualOpenIdRequest.requirement).isInstanceOf(SelfAttestedClaimRequirement::class.java)
        assertThat(actualOpenIdRequest.requirement.required).isEqualTo(false)
        assertThat(actualOpenIdRequest.requesterStyle.requester).isEqualTo(expectedEntityName)
    }

    @Test
    fun issuanceRequestMapping_IdTokenAttestationPresent_ReturnsIdTokenRequirementInOpenIdRequest() {
        // Arrange
        setupIdTokenAttestation()
        every { credentialAttestations.selfIssued } returns SelfIssuedAttestation()
        every { credentialAttestations.idTokens } returns listOf( idTokenAttestation)
        every { credentialAttestations.accessTokens } returns emptyList()
        every { credentialAttestations.presentations } returns emptyList()

        // Act
        val actualOpenIdRequest = issuanceRequest.toOpenIdIssuanceRequest()

        // Assert
        assertThat(actualOpenIdRequest.requirement).isInstanceOf(IdTokenRequirement::class.java)
    }

    @Test
    fun issuanceRequestMapping_AccessTokenAttestationPresent_ReturnsAccessTokenRequirementInOpenIdRequest() {
        // Arrange
        setupAccessTokenAttestation()
        every { credentialAttestations.selfIssued } returns SelfIssuedAttestation()
        every { credentialAttestations.idTokens } returns emptyList()
        every { credentialAttestations.accessTokens } returns listOf(accessTokenAttestation)
        every { credentialAttestations.presentations } returns emptyList()

        // Act
        val actualOpenIdRequest = issuanceRequest.toOpenIdIssuanceRequest()

        // Assert
        assertThat(actualOpenIdRequest.requirement).isInstanceOf(AccessTokenRequirement::class.java)
    }

    @Test
    fun issuanceRequestMapping_NoLogoInStyle_ReturnsSuccessfulRequestWithNoLogo() {
        // Arrange
        setupInput(false)
        setupSelfIssuedAttestation()
        every { credentialAttestations.selfIssued } returns selfIssuedAttestation
        every { credentialAttestations.idTokens } returns emptyList()
        every { credentialAttestations.accessTokens } returns emptyList()
        every { credentialAttestations.presentations } returns emptyList()

        // Act
        val actualOpenIdRequest = issuanceRequest.toOpenIdIssuanceRequest()

        // Assert
        assertThat(actualOpenIdRequest.requesterStyle.logo).isNull()
    }
}