package com.microsoft.walletlibrary.requests.rawrequests

import com.microsoft.did.sdk.credential.service.IssuanceRequest
import com.microsoft.did.sdk.credential.service.models.attestations.*
import com.microsoft.did.sdk.credential.service.models.linkedDomains.LinkedDomainVerified
import com.microsoft.walletlibrary.requests.VerifiedIdRequestContent
import com.microsoft.walletlibrary.requests.requirements.*
import com.microsoft.walletlibrary.requests.styles.OpenIdRequesterStyle
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class RawManifestTest {
    private val expectedClaimName = "name"
    private val expectedIssuerInCard = "Test Issuer"
    private lateinit var mockIssuanceRequest: IssuanceRequest
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
    private val expectedCredentialType = "TestCredential"
    private val expectedAcceptedIssuers = listOf("TestIssuer1")
    private lateinit var presentationAttestation: PresentationAttestation
    private val expectedLinkedDomainSource = "https://test.com"
    private lateinit var rawManifest: RawManifest

    init {
        setupInput()
    }

    private fun setupInput() {
        mockIssuanceRequest = mockk()
        rawManifest = RawManifest(mockIssuanceRequest, RequestType.ISSUANCE)
        every { mockIssuanceRequest.getAttestations() } returns credentialAttestations
        every { mockIssuanceRequest.entityName } returns expectedIssuerInCard
        every { mockIssuanceRequest.linkedDomainResult } returns LinkedDomainVerified(
            expectedLinkedDomainSource
        )
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
        val verifiedIdRequestContent = rawManifest.mapToRequestContent()

        // Assert
        assertThat(verifiedIdRequestContent).isInstanceOf(VerifiedIdRequestContent::class.java)
        assertThat(verifiedIdRequestContent.requirement).isInstanceOf(SelfAttestedClaimRequirement::class.java)
        assertThat(verifiedIdRequestContent.injectedIdToken).isNull()
        assertThat((verifiedIdRequestContent.requirement as SelfAttestedClaimRequirement).claim).isEqualTo(
            expectedClaimName
        )
        assertThat(verifiedIdRequestContent.requirement.required).isEqualTo(true)
        assertThat(verifiedIdRequestContent.requesterStyle.requester).isEqualTo(expectedIssuerInCard)
        assertThat(verifiedIdRequestContent.rootOfTrust.verified).isEqualTo(true)
        assertThat(verifiedIdRequestContent.rootOfTrust.source).isEqualTo(expectedLinkedDomainSource)
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
        val verifiedIdRequestContent = rawManifest.mapToRequestContent()

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
        val verifiedIdRequestContent = rawManifest.mapToRequestContent()

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
        val verifiedIdRequestContent = rawManifest.mapToRequestContent()

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
        val verifiedIdRequestContent = rawManifest.mapToRequestContent()

        // Assert
        assertThat(verifiedIdRequestContent.rootOfTrust.verified).isEqualTo(true)
        assertThat(verifiedIdRequestContent.rootOfTrust.source).isEqualTo(expectedLinkedDomainSource)
        assertThat(verifiedIdRequestContent.requesterStyle.requester).isEqualTo(expectedIssuerInCard)
        assertThat((verifiedIdRequestContent.requesterStyle as OpenIdRequesterStyle).logo).isNull()
        assertThat(verifiedIdRequestContent.requesterStyle.locale).isEqualTo("")
    }
}