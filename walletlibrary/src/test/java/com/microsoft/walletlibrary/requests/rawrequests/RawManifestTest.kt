package com.microsoft.walletlibrary.requests.rawrequests

import com.microsoft.did.sdk.credential.service.IssuanceRequest
import com.microsoft.did.sdk.credential.service.models.attestations.*
import com.microsoft.did.sdk.credential.service.models.contracts.VerifiableCredentialContract
import com.microsoft.did.sdk.credential.service.models.contracts.display.CardDescriptor
import com.microsoft.did.sdk.credential.service.models.contracts.display.ClaimDescriptor
import com.microsoft.did.sdk.credential.service.models.contracts.display.ConsentDescriptor
import com.microsoft.did.sdk.credential.service.models.contracts.display.DisplayContract
import com.microsoft.did.sdk.credential.service.models.linkedDomains.LinkedDomainVerified
import com.microsoft.walletlibrary.requests.IssuanceRequestContent
import com.microsoft.walletlibrary.requests.requirements.*
import com.microsoft.walletlibrary.requests.styles.VerifiedIdManifestIssuerStyle
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class RawManifestTest {
    private val expectedClaimName = "name"
    private val expectedIssuerInCard = "Test Issuer"
    private lateinit var mockIssuanceRequest: IssuanceRequest
    private val mockVerifiableCredentialContract: VerifiableCredentialContract = mockk()
    private lateinit var displayContract: DisplayContract
    private val mockCardDescriptor: CardDescriptor = mockk()
    private val expectedCardTitle = "test card title"
    private val expectedCardDescription = "test card description"
    private val expectedCardIssuer = "test issuer"
    private val expectedCardBackgroundColor = "#FFFFFF"
    private val expectedCardTextColor = "#000000"
    private val consentDescriptor: ConsentDescriptor = mockk()

    private val credentialAttestations: CredentialAttestations = mockk()
    private val expectedClaimDescriptorType = "claimType"
    private val expectedClaimDescriptorLabel = "claimLabel"
    private val expectedClaimName1 = "claimName1"
    private val claimsMap = mutableMapOf<String, ClaimDescriptor>()
    private val claimDescriptor1: ClaimDescriptor = mockk()
    private val claimAttestations = mutableListOf(ClaimAttestation("name", true, "string"))
    private lateinit var selfIssuedAttestation: SelfIssuedAttestation
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
    private val expectedLinkedDomainSource = "https://test.com"
    private lateinit var rawManifest: RawManifest

    init {
        claimsMap[expectedClaimName1] = claimDescriptor1
        setupInput()
    }

    private fun setupInput() {
        displayContract =
            DisplayContract(locale = "", card = mockCardDescriptor, consent = consentDescriptor, claims = claimsMap)
        mockIssuanceRequest = mockk()
        rawManifest = RawManifest(mockIssuanceRequest)
        every { mockIssuanceRequest.getAttestations() } returns credentialAttestations
        every { mockIssuanceRequest.entityName } returns expectedIssuerInCard
        every { mockIssuanceRequest.linkedDomainResult } returns LinkedDomainVerified(
            expectedLinkedDomainSource
        )
        every { mockIssuanceRequest.contract } returns mockVerifiableCredentialContract
        every { mockVerifiableCredentialContract.display } returns displayContract
        setupCardDescriptor()
        setupClaimDescriptor(claimsMap)
    }

    private fun setupCardDescriptor() {
        every { mockCardDescriptor.title } returns expectedCardTitle
        every { mockCardDescriptor.description } returns expectedCardDescription
        every { mockCardDescriptor.issuedBy } returns expectedCardIssuer
        every { mockCardDescriptor.backgroundColor } returns expectedCardBackgroundColor
        every { mockCardDescriptor.textColor } returns expectedCardTextColor
        every { mockCardDescriptor.logo } returns null
    }

    private fun setupClaimDescriptor(claimsMap: MutableMap<String, ClaimDescriptor>) {
        for (claimDescriptor in claimsMap.values) {
            every { claimDescriptor.type } returns expectedClaimDescriptorType
            every { claimDescriptor.label } returns expectedClaimDescriptorLabel
        }
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
    fun requestMapping_SelfIssuedAttestationPresent_ReturnsSelfAttestedRequirementInRequestContent() {
        // Arrange
        setupSelfIssuedAttestation()
        every { credentialAttestations.selfIssued } returns selfIssuedAttestation
        every { credentialAttestations.idTokens } returns emptyList()
        every { credentialAttestations.accessTokens } returns emptyList()
        every { credentialAttestations.presentations } returns emptyList()

        // Act
        val issuanceRequestContent = rawManifest.mapToIssuanceRequestContent()

        // Assert
        assertThat(issuanceRequestContent).isInstanceOf(IssuanceRequestContent::class.java)
        assertThat(issuanceRequestContent.requirement).isInstanceOf(SelfAttestedClaimRequirement::class.java)
        assertThat((issuanceRequestContent.requirement as SelfAttestedClaimRequirement).claim).isEqualTo(
            expectedClaimName
        )
        assertThat(issuanceRequestContent.requirement.required).isEqualTo(true)
        assertThat(issuanceRequestContent.requesterStyle.name).isEqualTo(expectedIssuerInCard)
        assertThat(issuanceRequestContent.rootOfTrust.verified).isEqualTo(true)
        assertThat(issuanceRequestContent.rootOfTrust.source).isEqualTo(expectedLinkedDomainSource)
    }

    @Test
    fun issuanceRequestMapping_IdTokenAttestationPresent_ReturnsIdTokenRequirementInOpenIdRequest() {
        // Arrange
        setupIdTokenAttestation()
        every { credentialAttestations.selfIssued } returns SelfIssuedAttestation()
        every { credentialAttestations.idTokens } returns listOf(idTokenAttestation)
        every { credentialAttestations.accessTokens } returns emptyList()
        every { credentialAttestations.presentations } returns emptyList()

        // Act
        val verifiedIdRequestContent = rawManifest.mapToIssuanceRequestContent()

        // Assert
        assertThat(verifiedIdRequestContent.requirement).isInstanceOf(IdTokenRequirement::class.java)
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
        val verifiedIdRequestContent = rawManifest.mapToIssuanceRequestContent()

        // Assert
        assertThat(verifiedIdRequestContent.requirement).isInstanceOf(AccessTokenRequirement::class.java)
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
        val verifiedIdRequestContent = rawManifest.mapToIssuanceRequestContent()

        // Assert
        assertThat(verifiedIdRequestContent.requirement).isInstanceOf(GroupRequirement::class.java)
        assertThat((verifiedIdRequestContent.requirement as GroupRequirement).requirementOperator).isEqualTo(
            GroupRequirementOperator.ALL
        )
        assertThat(verifiedIdRequestContent.requirement.required).isEqualTo(true)
    }

    @Test
    fun issuanceRequestMapping_mapRootOfTrust_ReturnsRequest() {
        // Arrange
        setupInput()
        setupSelfIssuedAttestation()
        every { credentialAttestations.selfIssued } returns selfIssuedAttestation
        every { credentialAttestations.idTokens } returns emptyList()
        every { credentialAttestations.accessTokens } returns emptyList()
        every { credentialAttestations.presentations } returns emptyList()

        // Act
        val verifiedIdRequestContent = rawManifest.mapToIssuanceRequestContent()

        // Assert
        assertThat(verifiedIdRequestContent.rootOfTrust.verified).isEqualTo(true)
        assertThat(verifiedIdRequestContent.rootOfTrust.source).isEqualTo(expectedLinkedDomainSource)
        assertThat(verifiedIdRequestContent.requesterStyle).isInstanceOf(
            VerifiedIdManifestIssuerStyle::class.java
        )
        assertThat((verifiedIdRequestContent.requesterStyle as VerifiedIdManifestIssuerStyle).name).isEqualTo(
            expectedIssuerInCard
        )
    }
}