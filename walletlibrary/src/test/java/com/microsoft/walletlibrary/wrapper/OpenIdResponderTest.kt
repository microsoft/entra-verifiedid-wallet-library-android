package com.microsoft.walletlibrary.wrapper

import com.microsoft.did.sdk.PresentationService
import com.microsoft.did.sdk.VerifiableCredentialSdk
import com.microsoft.did.sdk.credential.models.VerifiableCredentialContent
import com.microsoft.did.sdk.credential.models.VerifiableCredentialDescriptor
import com.microsoft.did.sdk.credential.service.PresentationRequest
import com.microsoft.did.sdk.credential.service.models.contracts.VerifiableCredentialContract
import com.microsoft.did.sdk.credential.service.models.oidc.PresentationRequestContent
import com.microsoft.did.sdk.credential.service.models.presentationexchange.CredentialPresentationInputDescriptor
import com.microsoft.did.sdk.credential.service.models.presentationexchange.PresentationDefinition
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.SdkException
import com.microsoft.walletlibrary.requests.requirements.Requirement
import com.microsoft.walletlibrary.requests.requirements.VerifiedIdRequirement
import com.microsoft.walletlibrary.requests.requirements.constraints.VcTypeConstraint
import com.microsoft.walletlibrary.util.OpenIdResponseCompletionException
import com.microsoft.walletlibrary.util.VerifiedIdRequirementNotFulfilledException
import com.microsoft.walletlibrary.verifiedid.VerifiableCredential
import com.microsoft.walletlibrary.verifiedid.VerifiedId
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class OpenIdResponderTest {
    private val mockPresentationService: PresentationService = mockk()
    private val mockPresentationRequest: PresentationRequest = mockk()
    private val mockPresentationRequestContent: PresentationRequestContent = mockk()
    private val mockPresentationDefinition: PresentationDefinition = mockk()
    private val mockCredentialDescriptors: CredentialPresentationInputDescriptor = mockk()
    private lateinit var requirement: Requirement
    private lateinit var verifiedId: VerifiedId

    init {
        setupInput()
    }

    private fun setupInput() {
        val expectedVcType = "testVc"
        val expectedVcId = "TestVC1"
        requirement = VerifiedIdRequirement(expectedVcId, listOf(expectedVcType), VcTypeConstraint(expectedVcType))
        val vcFromSdk: com.microsoft.did.sdk.credential.models.VerifiableCredential = mockk()
        val mockVerifiableCredentialContent: VerifiableCredentialContent = mockk()
        val mockVerifiableCredentialDescriptor: VerifiableCredentialDescriptor = mockk()
        val expectedCredentialSubject = mutableMapOf<String, String>()
        val expectedCredentialSubjectClaimName = "name"
        val expectedCredentialSubjectClaimValue = "test"
        every { vcFromSdk.jti } returns ""
        every { vcFromSdk.raw } returns ""
        every { vcFromSdk.contents } returns mockVerifiableCredentialContent
        every { vcFromSdk.contents.iat } returns 1234567L
        every { vcFromSdk.contents.exp } returns 0L
        every { vcFromSdk.contents.vc } returns mockVerifiableCredentialDescriptor
        every { mockVerifiableCredentialDescriptor.type } returns listOf(expectedVcType)
        expectedCredentialSubject[expectedCredentialSubjectClaimName] = expectedCredentialSubjectClaimValue
        every { mockVerifiableCredentialDescriptor.credentialSubject } returns expectedCredentialSubject
        val vcContractFromSdk: VerifiableCredentialContract = mockk()
        verifiedId = VerifiableCredential(vcFromSdk, vcContractFromSdk)
        mockkStatic(VerifiableCredentialSdk::class)
        every { VerifiableCredentialSdk.presentationService } returns mockPresentationService
        every { mockPresentationRequest.content } returns mockPresentationRequestContent
        every { mockPresentationRequestContent.clientId } returns ""
        every { mockPresentationRequest.getPresentationDefinition() } returns mockPresentationDefinition
        every { mockPresentationDefinition.id } returns "definitionid"
        every { mockPresentationDefinition.credentialPresentationInputDescriptors } returns listOf(mockCredentialDescriptors)
        every { mockCredentialDescriptors.id } returns expectedVcId
    }

    @Test
    fun completePresentation_SuccessFromVcSDK_SuccessfulInWalletLibrary() {
        // Arrange
        coEvery {
            mockPresentationService.sendResponse(any())
        } returns Result.Success(Unit)
        (requirement as VerifiedIdRequirement).fulfill(verifiedId)

        runBlocking {
            // Act
            val actualResult = OpenIdResponder.sendPresentationResponse(mockPresentationRequest, requirement)

            // Assert
            assertThat(actualResult).isEqualTo(Unit)
        }
    }

    @Test
    fun completePresentation_FailureFromVcSDK_ThrowsException() {
        // Arrange
        coEvery {
            mockPresentationService.sendResponse(any())
        } returns Result.Failure(SdkException("Test failure"))
        (requirement as VerifiedIdRequirement).fulfill(verifiedId)

        // Act and Assert
        Assertions.assertThatThrownBy {
            runBlocking {
                OpenIdResponder.sendPresentationResponse(mockPresentationRequest, requirement)
            }
        }.isInstanceOf(OpenIdResponseCompletionException::class.java)
    }

    @Test
    fun completePresentation_FailureWhileAddingRequirementsNotFulfilled_ThrowsException() {
        // Arrange
        coEvery {
            mockPresentationService.sendResponse(any())
        } returns Result.Failure(SdkException("Test failure"))

        // Act and Assert
        Assertions.assertThatThrownBy {
            runBlocking {
                OpenIdResponder.sendPresentationResponse(mockPresentationRequest, requirement)
            }
        }.isInstanceOf(VerifiedIdRequirementNotFulfilledException::class.java)
    }
}