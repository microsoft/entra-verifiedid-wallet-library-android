package com.microsoft.walletlibrary.mappings

import com.microsoft.did.sdk.credential.service.IssuanceRequest
import com.microsoft.did.sdk.credential.service.models.attestations.*
import com.microsoft.did.sdk.credential.service.models.contracts.display.ClaimDescriptor
import com.microsoft.did.sdk.credential.service.models.contracts.display.Logo
import com.microsoft.did.sdk.credential.service.models.linkedDomains.LinkedDomainVerified
import com.microsoft.walletlibrary.requests.requirements.*
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
    private val expectedLogoUri = "testLogoUri"
    private val expectedLogoImage ="testLogoImage"
    private val expectedLogoDescription ="testLogoDescription"
    private val expectedCredentialType = "TestCredential"
    private val expectedAcceptedIssuers = listOf("TestIssuer1")
    private lateinit var presentationAttestation: PresentationAttestation

    init {
        setupInput(true)
    }

    private fun setupInput(logoPresent: Boolean) {
        issuanceRequest = mockk()
        every { issuanceRequest.getAttestations() } returns credentialAttestations
        every { issuanceRequest.entityName } returns expectedEntityName
        every { issuanceRequest.linkedDomainResult } returns LinkedDomainVerified(expectedLinkedDomainSource)
        setupContract()
        setupDisplayContract()
        setupCardDescriptor(logoPresent)
    }

    private fun setupContract() {
        every { issuanceRequest.contract } returns mockk()
        every { issuanceRequest.contract.display } returns mockk()
    }

    private fun setupDisplayContract() {
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
        every { logo.uri } returns expectedLogoUri
        every { logo.image } returns expectedLogoImage
        every { logo.description } returns expectedLogoDescription
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
    fun issuanceRequestMapping_MultipleAttestationPresent_ReturnsGroupRequirementInOpenIdRequest() {
        // Arrange
        setupSelfIssuedAttestation()
        setupPresentationAttestation()
        every { credentialAttestations.selfIssued } returns selfIssuedAttestation
        every { credentialAttestations.idTokens } returns emptyList()
        every { credentialAttestations.accessTokens } returns emptyList()
        every { credentialAttestations.presentations } returns listOf(presentationAttestation)

        // Act
        val actualOpenIdRequest = issuanceRequest.toOpenIdIssuanceRequest()

        // Assert
        assertThat(actualOpenIdRequest.requirement).isInstanceOf(GroupRequirement::class.java)
        assertThat((actualOpenIdRequest.requirement as GroupRequirement).requirementOperator).isEqualTo(
            GroupRequirementOperator.ALL)
        assertThat(actualOpenIdRequest.requirement.required).isEqualTo(true)
    }

    @Test
    fun issuanceRequestMapping_NoLogoInRequesterStyle_ReturnsSuccessfulRequestWithNoLogo() {
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

    @Test
    fun issuanceRequestMapping_LogoPresentInVerifiedIdStyle_ReturnsSuccessfulRequestWithLogo() {
        // Arrange
        setupInput(true)
        setupSelfIssuedAttestation()
        every { credentialAttestations.selfIssued } returns selfIssuedAttestation
        every { credentialAttestations.idTokens } returns emptyList()
        every { credentialAttestations.accessTokens } returns emptyList()
        every { credentialAttestations.presentations } returns emptyList()

        // Act
        val actualOpenIdRequest = issuanceRequest.toOpenIdIssuanceRequest()

        // Assert
        assertThat(actualOpenIdRequest.verifiedIdStyle.logo).isNotNull
        assertThat(actualOpenIdRequest.verifiedIdStyle.logo?.uri).isEqualTo(expectedLogoUri)
        assertThat(actualOpenIdRequest.verifiedIdStyle.logo?.image).isEqualTo(expectedLogoImage)
        assertThat(actualOpenIdRequest.verifiedIdStyle.logo?.description).isEqualTo(expectedLogoDescription)
    }

    @Test
    fun issuanceRequestMapping_NoLogoInVerifiedIdStyle_ReturnsSuccessfulRequestWithNoLogo() {
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
        assertThat(actualOpenIdRequest.verifiedIdStyle.logo).isNull()
    }

    @Test
    fun issuanceRequestMapping_mapRootOfTrust_ReturnsRequest() {
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
        assertThat(actualOpenIdRequest.rootOfTrust.verified).isEqualTo(true)
        assertThat(actualOpenIdRequest.rootOfTrust.source).isEqualTo(expectedLinkedDomainSource)
    }

    @Test
    fun issuanceRequestMapping_mapRequesterStyle_ReturnsRequesterStyle() {
        // Act
        val actualRequesterStyle = issuanceRequest.toRequesterStyle()

        // Assert
        assertThat(actualRequesterStyle.requester).isEqualTo(expectedEntityName)
        assertThat(actualRequesterStyle.logo).isNull()
        assertThat(actualRequesterStyle.locale).isEqualTo("")
    }
}