package com.microsoft.walletlibrary.mappings.presentation

import com.microsoft.walletlibrary.did.sdk.credential.service.PresentationRequest
import com.microsoft.walletlibrary.did.sdk.credential.service.PresentationResponse
import com.microsoft.walletlibrary.did.sdk.credential.service.models.presentationexchange.CredentialPresentationInputDescriptor
import com.microsoft.walletlibrary.did.sdk.credential.service.models.presentationexchange.PresentationDefinition
import com.microsoft.walletlibrary.requests.requirements.*
import com.microsoft.walletlibrary.requests.requirements.constraints.VcTypeConstraint
import com.microsoft.walletlibrary.util.IdInVerifiedIdRequirementDoesNotMatchRequestException
import com.microsoft.walletlibrary.util.UnSupportedRequirementException
import com.microsoft.walletlibrary.util.VerifiedIdRequirementMissingIdException
import com.microsoft.walletlibrary.util.VerifiedIdRequirementNotFulfilledException
import com.microsoft.walletlibrary.verifiedid.VerifiableCredential
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

class PresentationResponseMappingTest {
    private val mockPresentationRequest: PresentationRequest = mockk()
    private val mockPresentationDefinition: PresentationDefinition = mockk()
    private val mockCredentialPresentationInputDescriptor: CredentialPresentationInputDescriptor =
        mockk()
    private lateinit var presentationResponse: PresentationResponse
    private val expectedVcType = "TestVc"
    private val verifiedIdRequirement = VerifiedIdRequirement(
        "id",
        listOf(expectedVcType),
        VcTypeConstraint(expectedVcType),
        encrypted = false,
        required = true
    )
    private val expectedClaim = "name"
    private val expectedRequestedClaim = RequestedClaim(false, expectedClaim, true)
    private val expectedIdTokenValue = "Test Id Token"
    private val expectedConfiguration = "configuration"
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

    init {
        // Arrange
        setupInput()
    }

    private fun setupInput() {
        every { mockPresentationRequest.content.clientId } returns ""
        every { mockPresentationRequest.getPresentationDefinition() } returns mockPresentationDefinition
        every { mockPresentationDefinition.id } returns ""
        presentationResponse = PresentationResponse(mockPresentationRequest)
    }

    @Test
    fun addRequirement_WithInvalidRequirementType_ThrowsException() {
        // Act and Assert
        assertThatThrownBy {
            presentationResponse.addRequirements(idTokenRequirement)
        }.isInstanceOf(UnSupportedRequirementException::class.java)
    }

    @Test
    fun addRequirement_WithNoIdInVerifiedIdRequirement_ThrowsException() {
        // Arrange
        val verifiedIdRequirement = VerifiedIdRequirement(
            null,
            listOf(expectedVcType),
            VcTypeConstraint(expectedVcType),
            encrypted = false,
            required = true
        )

        // Act and Assert
        assertThatThrownBy {
            presentationResponse.addRequirements(verifiedIdRequirement)
        }.isInstanceOf(VerifiedIdRequirementMissingIdException::class.java)
    }

    @Test
    fun addRequirement_FulfilledVerifiedIdRequirementDoesNotMatchTypeConstraint_ThrowsException() {
        // Act and Assert
        assertThatThrownBy {
            presentationResponse.addRequirements(verifiedIdRequirement)
        }.isInstanceOf(VerifiedIdRequirementNotFulfilledException::class.java)
    }

    @Test
    fun addRequirement_validVerifiedIdRequirement_addsRequirementToPresentationResponse() {
        // Arrange
        val expectedVerifiedId: VerifiableCredential = mockk()
        val expectedVerifiableCredential: com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredential =
            mockk()
        every { expectedVerifiedId.types } returns listOf(expectedVcType)
        verifiedIdRequirement.fulfill(expectedVerifiedId)
        every { mockPresentationDefinition.credentialPresentationInputDescriptors } returns listOf(
            mockCredentialPresentationInputDescriptor
        )
        every { mockCredentialPresentationInputDescriptor.id } returns "id"
        every { expectedVerifiedId.raw } returns expectedVerifiableCredential

        // Act
        presentationResponse.addRequirements(verifiedIdRequirement)

        // Assert
        assertThat(presentationResponse.requestedVcPresentationSubmissionMap.size).isEqualTo(1)
        assertThat(presentationResponse.requestedVcPresentationSubmissionMap[mockCredentialPresentationInputDescriptor]).isEqualTo(
            expectedVerifiableCredential
        )
    }

    @Test
    fun addRequirement_validVerifiedIdRequirementsInGroupRequirement_addsRequirementsToPresentationResponse() {
        // Arrange
        val verifiedIdRequirement1 = VerifiedIdRequirement(
            "id1",
            listOf(expectedVcType),
            VcTypeConstraint(expectedVcType),
            encrypted = false,
            required = true
        )
        val verifiedIdRequirement2 = VerifiedIdRequirement(
            "id2",
            listOf(expectedVcType),
            VcTypeConstraint(expectedVcType),
            encrypted = false,
            required = true
        )
        val expectedVerifiedId1: VerifiableCredential = mockk()
        val expectedVerifiableCredential1: com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredential =
            mockk()
        val expectedVerifiedId2: VerifiableCredential = mockk()
        val expectedVerifiableCredential2: com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredential =
            mockk()
        every { expectedVerifiedId1.types } returns listOf(expectedVcType)
        every { expectedVerifiedId2.types } returns listOf(expectedVcType)
        verifiedIdRequirement1.fulfill(expectedVerifiedId1)
        verifiedIdRequirement2.fulfill(expectedVerifiedId2)
        val mockCredentialPresentationInputDescriptor1: CredentialPresentationInputDescriptor = mockk()
        val mockCredentialPresentationInputDescriptor2: CredentialPresentationInputDescriptor = mockk()
        every { mockPresentationDefinition.credentialPresentationInputDescriptors } returns listOf(
            mockCredentialPresentationInputDescriptor1, mockCredentialPresentationInputDescriptor2
        )
        every { mockCredentialPresentationInputDescriptor1.id } returns "id1"
        every { mockCredentialPresentationInputDescriptor2.id } returns "id2"
        every { expectedVerifiedId1.raw } returns expectedVerifiableCredential1
        every { expectedVerifiedId2.raw } returns expectedVerifiableCredential2
        val groupRequirement = GroupRequirement(
            true,
            mutableListOf(verifiedIdRequirement1, verifiedIdRequirement2),
            GroupRequirementOperator.ALL
        )

        // Act
        presentationResponse.addRequirements(groupRequirement)

        // Assert
        assertThat(presentationResponse.requestedVcPresentationSubmissionMap.size).isEqualTo(2)
        assertThat(presentationResponse.requestedVcPresentationSubmissionMap[mockCredentialPresentationInputDescriptor1]).isEqualTo(
            expectedVerifiableCredential1
        )
        assertThat(presentationResponse.requestedVcPresentationSubmissionMap[mockCredentialPresentationInputDescriptor2]).isEqualTo(
            expectedVerifiableCredential2
        )
    }

    @Test
    fun addRequirement_IdInVerifiedIdRequirementDoesNotMatchRequest_ThrowsException() {
        // Arrange
        val verifiedIdRequirement1 = VerifiedIdRequirement(
            "id1",
            listOf(expectedVcType),
            VcTypeConstraint(expectedVcType),
            encrypted = false,
            required = true
        )
        val expectedVerifiedId1: VerifiableCredential = mockk()
        every { expectedVerifiedId1.types } returns listOf(expectedVcType)
        verifiedIdRequirement1.fulfill(expectedVerifiedId1)
        val mockCredentialPresentationInputDescriptor1: CredentialPresentationInputDescriptor = mockk()
        every { mockPresentationDefinition.credentialPresentationInputDescriptors } returns listOf(
            mockCredentialPresentationInputDescriptor1
        )
        every { mockCredentialPresentationInputDescriptor1.id } returns "id"

        assertThatThrownBy {
            // Act and Assert
            presentationResponse.addRequirements(verifiedIdRequirement1)
        }.isInstanceOf(IdInVerifiedIdRequirementDoesNotMatchRequestException::class.java)
    }
}