package com.microsoft.walletlibrary.wrapper

import com.microsoft.walletlibrary.did.sdk.PresentationService
import com.microsoft.walletlibrary.did.sdk.VerifiableCredentialSdk
import com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredentialContent
import com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredentialDescriptor
import com.microsoft.walletlibrary.did.sdk.credential.service.PresentationRequest
import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.VerifiableCredentialContract
import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.display.CardDescriptor
import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.display.DisplayContract
import com.microsoft.walletlibrary.did.sdk.credential.service.models.oidc.PresentationRequestContent
import com.microsoft.walletlibrary.did.sdk.credential.service.models.presentationexchange.CredentialPresentationInputDescriptor
import com.microsoft.walletlibrary.did.sdk.credential.service.models.presentationexchange.PresentationDefinition
import com.microsoft.walletlibrary.did.sdk.util.controlflow.Result
import com.microsoft.walletlibrary.did.sdk.util.controlflow.SdkException
import com.microsoft.walletlibrary.requests.requirements.Requirement
import com.microsoft.walletlibrary.requests.requirements.VerifiedIdRequirement
import com.microsoft.walletlibrary.requests.requirements.constraints.VcTypeConstraint
import com.microsoft.walletlibrary.util.OpenIdResponseCompletionException
import com.microsoft.walletlibrary.util.RequirementNotMetException
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
    private val vcContractFromSdk: VerifiableCredentialContract = mockk()
    private val vcFromSdk: com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredential = mockk()
    private val mockDisplayContract: DisplayContract = mockk()
    private val mockCardDescriptor: CardDescriptor = mockk()
    private val expectedCardTitle = "Test VC"
    private val expectedCardIssuer = "Test Issuer"
    private val expectedCardBackgroundColor = "#000000"
    private val expectedCardTextColor = "#ffffff"
    private val expectedCardDescription = "VC issued for testing purposes"

    init {
        setupInput()
    }

    private fun setupInput() {
        val expectedVcType = "testVc"
        val expectedVcId = "TestVC1"
        requirement = VerifiedIdRequirement(expectedVcId, listOf(expectedVcType))
        (requirement as VerifiedIdRequirement).constraint = VcTypeConstraint(expectedVcType)
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
        every { vcContractFromSdk.display } returns mockDisplayContract
        setupDisplayContract()
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

    private fun setupDisplayContract() {
        every { mockDisplayContract.card } returns mockCardDescriptor
        every { mockCardDescriptor.title } returns expectedCardTitle
        every { mockCardDescriptor.issuedBy } returns expectedCardIssuer
        every { mockCardDescriptor.textColor } returns expectedCardTextColor
        every { mockCardDescriptor.backgroundColor } returns expectedCardBackgroundColor
        every { mockCardDescriptor.description } returns expectedCardDescription
        every { mockCardDescriptor.logo } returns null
    }

    @Test
    fun completePresentation_SuccessFromVcSDK_SuccessfulInWalletLibrary() {
        // Arrange
        every { vcContractFromSdk.display } returns mockDisplayContract
        setupDisplayContract()
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
        every { vcContractFromSdk.display } returns mockDisplayContract
        setupDisplayContract()
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
        every { vcContractFromSdk.display } returns mockDisplayContract
        setupDisplayContract()
        coEvery {
            mockPresentationService.sendResponse(any())
        } returns Result.Failure(SdkException("Test failure"))

        // Act and Assert
        Assertions.assertThatThrownBy {
            runBlocking {
                OpenIdResponder.sendPresentationResponse(mockPresentationRequest, requirement)
            }
        }.isInstanceOf(RequirementNotMetException::class.java)
            .hasMessage("Verified ID has not been set.")
    }
}