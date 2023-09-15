package com.microsoft.walletlibrary.mappings.issuance

import com.microsoft.walletlibrary.did.sdk.credential.service.IssuanceRequest
import com.microsoft.walletlibrary.did.sdk.credential.service.IssuanceResponse
import com.microsoft.walletlibrary.did.sdk.credential.service.models.attestations.PresentationAttestation
import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.InputContract
import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.VerifiableCredentialContract
import com.microsoft.walletlibrary.requests.requirements.AccessTokenRequirement
import com.microsoft.walletlibrary.requests.requirements.GroupRequirement
import com.microsoft.walletlibrary.requests.requirements.GroupRequirementOperator
import com.microsoft.walletlibrary.requests.requirements.IdTokenRequirement
import com.microsoft.walletlibrary.requests.requirements.PinRequirement
import com.microsoft.walletlibrary.requests.requirements.RequestedClaim
import com.microsoft.walletlibrary.requests.requirements.SelfAttestedClaimRequirement
import com.microsoft.walletlibrary.requests.requirements.VerifiedIdRequirement
import com.microsoft.walletlibrary.requests.requirements.constraints.VcTypeConstraint
import com.microsoft.walletlibrary.util.RequirementNotMetException
import com.microsoft.walletlibrary.verifiedid.VerifiableCredential
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import kotlin.test.assertTrue

class IssuanceResponseMappingTest {
    private val mockIssuanceRequest: IssuanceRequest = mockk()
    private lateinit var issuanceResponse: IssuanceResponse
    private val mockContract: VerifiableCredentialContract = mockk()
    private val mockInput: InputContract = mockk()
    private val credentialIssuer = "Test Issuer"
    private val expectedClaim = "name"
    private val expectedClaimValue = "Test"
    private var selfAttestedClaimRequirement = SelfAttestedClaimRequirement(
        "",
        expectedClaim,
        encrypted = false,
        required = true,
        value = expectedClaimValue
    )
    private val expectedRequestedClaim = RequestedClaim(false, expectedClaim, true)
    private val expectedIdTokenValue = "Test Id Token"
    private val expectedConfiguration = "configuration"
    private val expectedAccessTokenValue = "Test Access Token"
    private val expectedPinValue = "1234"
    private var idTokenRequirement =
        IdTokenRequirement(
            "",
            expectedConfiguration,
            "",
            "",
            "",
            "",
            listOf(expectedRequestedClaim),
            encrypted = false,
            required = true,
            idToken = expectedIdTokenValue
        )
    private var accessTokenRequirement =
        AccessTokenRequirement(
            "",
            expectedConfiguration,
            "",
            "",
            "",
            listOf(expectedRequestedClaim),
            encrypted = false,
            required = true,
            accessToken = expectedAccessTokenValue
        )
    private val groupRequirement = GroupRequirement(
        true,
        mutableListOf(selfAttestedClaimRequirement, accessTokenRequirement),
        GroupRequirementOperator.ALL
    )

    init {
        // Arrange
        setupInput()
    }

    private fun setupInput() {
        every { mockIssuanceRequest.contract } returns mockContract
        every { mockContract.input } returns mockInput
        every { mockInput.credentialIssuer } returns credentialIssuer
        issuanceResponse = IssuanceResponse(mockIssuanceRequest)
    }

    @Test
    fun addRequirementToResponse_AddSelfAttestedRequirement_AddsRequirementToIssuanceResponse() {
        // Act
        issuanceResponse.addRequirements(selfAttestedClaimRequirement)

        // Assert
        assertThat(issuanceResponse.requestedSelfAttestedClaimMap.size).isEqualTo(1)
        assertThat(issuanceResponse.requestedSelfAttestedClaimMap[expectedClaim]).isEqualTo(
            expectedClaimValue
        )
    }

    @Test
    fun addRequirementToResponse_SelfAttestedRequirementNotFulfilled_ThrowsException() {
        // Arrange
        selfAttestedClaimRequirement = SelfAttestedClaimRequirement(
            "",
            expectedClaim,
            encrypted = false,
            required = true
        )

        // Act and Assert
        assertThatThrownBy {
            issuanceResponse.addRequirements(selfAttestedClaimRequirement)
        }.isInstanceOf(RequirementNotMetException::class.java)
            .hasMessage("Self Attested Claim has not been set.")
    }

    @Test
    fun addRequirementToResponse_AddIdTokenRequirement_AddsRequirementToIssuanceResponse() {
        // Act
        issuanceResponse.addRequirements(idTokenRequirement)

        // Assert
        assertThat(issuanceResponse.requestedIdTokenMap.size).isEqualTo(1)
        assertThat(issuanceResponse.requestedIdTokenMap[expectedConfiguration]).isEqualTo(
            expectedIdTokenValue
        )
    }

    @Test
    fun addRequirementToResponse_IdTokenRequirementNotFulfilled_ThrowsException() {
        // Arrange
        idTokenRequirement =
            IdTokenRequirement(
                "",
                expectedConfiguration,
                "",
                "",
                "",
                "",
                listOf(expectedRequestedClaim),
                encrypted = false,
                required = true
            )

        // Act and Assert
        assertThatThrownBy {
            issuanceResponse.addRequirements(idTokenRequirement)
        }.isInstanceOf(RequirementNotMetException::class.java)
            .hasMessage("Id Token has not been set.")
    }

    @Test
    fun addRequirementToResponse_AddAccessTokenRequirement_AddsRequirementToIssuanceResponse() {
        // Act
        issuanceResponse.addRequirements(accessTokenRequirement)

        // Assert
        assertThat(issuanceResponse.requestedAccessTokenMap.size).isEqualTo(1)
        assertThat(issuanceResponse.requestedAccessTokenMap[expectedConfiguration]).isEqualTo(
            expectedAccessTokenValue
        )
    }

    @Test
    fun addRequirementToResponse_AddPinRequirementWithNoSalt_AddsRequirementToIssuanceResponse() {
        // Arrange
        val pinRequirement = PinRequirement(4, "numeric", true, pin = "1234")

        // Act
        issuanceResponse.addRequirements(pinRequirement)

        // Assert
        assertThat(issuanceResponse.issuancePin?.pin).isEqualTo(
            expectedPinValue
        )
    }

    @Test
    fun addRequirementToResponse_AddPinRequirementWithSalt_AddsRequirementToIssuanceResponse() {
        // Arrange
        val expectedPinSalt = "abcdefg"
        val pinRequirement = PinRequirement(4, "numeric", true, expectedPinSalt, "1234")

        // Act
        issuanceResponse.addRequirements(pinRequirement)

        // Assert
        assertThat(issuanceResponse.issuancePin?.pin).isEqualTo(
            expectedPinValue
        )
        assertThat(issuanceResponse.issuancePin?.pinSalt).isEqualTo(expectedPinSalt)
    }

    @Test
    fun addRequirementToResponse_PinRequirementNotFulfilled_ThrowsException() {
        // Arrange
        val expectedPinSalt = "abcdefg"
        val pinRequirement = PinRequirement(4, "numeric", true, expectedPinSalt)

        // Act and Assert
        assertThatThrownBy{
            issuanceResponse.addRequirements(pinRequirement)
        }.isInstanceOf(RequirementNotMetException::class.java)
    }

    @Test
    fun addRequirementToResponse_AddGroupRequirement_AddsRequirementToIssuanceResponse() {
        // Act
        issuanceResponse.addRequirements(groupRequirement)

        // Assert
        assertThat(issuanceResponse.requestedSelfAttestedClaimMap.size).isEqualTo(1)
        assertThat(issuanceResponse.requestedSelfAttestedClaimMap[expectedClaim]).isEqualTo(
            expectedClaimValue
        )
        assertThat(issuanceResponse.requestedAccessTokenMap.size).isEqualTo(1)
        assertThat(issuanceResponse.requestedAccessTokenMap[expectedConfiguration]).isEqualTo(
            expectedAccessTokenValue
        )
    }

    @Test
    fun addRequirementToResponse_AddVerifiedIdRequirement_AddsRequirementToIssuanceResponse() {
        // Arrange
        val expectedCredentialType = "TestVc"
        val verifiedIdRequirement = VerifiedIdRequirement(
            "TestId",
            listOf(expectedCredentialType)
        )
        val mockVerifiableCredential: VerifiableCredential = mockk()
        every { mockVerifiableCredential.raw } returns mockk()
        every { mockVerifiableCredential.types } returns listOf(expectedCredentialType)
        verifiedIdRequirement.verifiedId = mockVerifiableCredential
        val mockPresentationAttestation: PresentationAttestation = mockk()
        every { mockIssuanceRequest.getAttestations().presentations } returns listOf(mockPresentationAttestation)
        every { mockPresentationAttestation.credentialType } returns expectedCredentialType

        // Act
        issuanceResponse.addRequirements(verifiedIdRequirement)

        // Assert
        assertTrue(verifiedIdRequirement.constraint is VcTypeConstraint)
        assertThat((verifiedIdRequirement.constraint as VcTypeConstraint).vcType).isEqualTo(expectedCredentialType)
        assertThat(issuanceResponse.requestedVcMap.size).isEqualTo(1)
    }

    @Test
    fun addRequirementToResponse_VerifiedIdRequirementNotFulfilled_ThrowsException() {
        // Arrange
        val expectedCredentialType = "TestVc"
        val verifiedIdRequirement = VerifiedIdRequirement(
            "TestId",
            listOf(expectedCredentialType)
        )

        // Act and Assert
        assertTrue(verifiedIdRequirement.constraint is VcTypeConstraint)
        assertThat((verifiedIdRequirement.constraint as VcTypeConstraint).vcType).isEqualTo(expectedCredentialType)
        assertThatThrownBy {
            issuanceResponse.addRequirements(verifiedIdRequirement)
        }.isInstanceOf(RequirementNotMetException::class.java)
    }
}