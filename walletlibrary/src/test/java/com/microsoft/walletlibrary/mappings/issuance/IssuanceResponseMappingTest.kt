package com.microsoft.walletlibrary.mappings.issuance

import com.microsoft.did.sdk.credential.service.IssuanceRequest
import com.microsoft.did.sdk.credential.service.IssuanceResponse
import com.microsoft.did.sdk.credential.service.models.contracts.InputContract
import com.microsoft.did.sdk.credential.service.models.contracts.VerifiableCredentialContract
import com.microsoft.walletlibrary.requests.requirements.*
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class IssuanceResponseMappingTest {
    private val mockIssuanceRequest: IssuanceRequest = mockk()
    private lateinit var issuanceResponse: IssuanceResponse
    private val mockContract: VerifiableCredentialContract = mockk()
    private val mockInput: InputContract = mockk()
    private val credentialIssuer = "Test Issuer"
    private val expectedClaim = "name"
    private val expectedClaimValue = "Test"
    private val selfAttestedClaimRequirement = SelfAttestedClaimRequirement(
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
    private val idTokenRequirement =
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
    private val accessTokenRequirement =
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
        listOf(selfAttestedClaimRequirement, accessTokenRequirement),
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
}