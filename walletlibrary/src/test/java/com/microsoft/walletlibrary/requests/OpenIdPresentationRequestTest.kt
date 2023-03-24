package com.microsoft.walletlibrary.requests

import com.microsoft.did.sdk.credential.service.PresentationRequest
import com.microsoft.did.sdk.credential.service.models.oidc.PresentationRequestContent
import com.microsoft.did.sdk.credential.service.models.presentationexchange.PresentationDefinition
import com.microsoft.walletlibrary.requests.rawrequests.VerifiedIdOpenIdJwtRawRequest
import com.microsoft.walletlibrary.requests.requirements.GroupRequirement
import com.microsoft.walletlibrary.requests.requirements.GroupRequirementOperator
import com.microsoft.walletlibrary.requests.requirements.Requirement
import com.microsoft.walletlibrary.requests.requirements.VerifiedIdRequirement
import com.microsoft.walletlibrary.requests.requirements.constraints.VcTypeConstraint
import com.microsoft.walletlibrary.requests.styles.RequesterStyle
import com.microsoft.walletlibrary.util.AggregateException
import com.microsoft.walletlibrary.util.OpenIdResponseCompletionException
import com.microsoft.walletlibrary.util.VerifiedIdRequirementNotFulfilledException
import com.microsoft.walletlibrary.verifiedid.VerifiableCredential
import com.microsoft.walletlibrary.wrapper.OpenIdResponder
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.junit.Test

enum class FulFilledRequirement {
    ONE,
    NONE,
    ALL
}

class OpenIdPresentationRequestTest {
    private val requesterStyle: RequesterStyle = mockk()
    private lateinit var requirement: Requirement
    private val rootOfTrust: RootOfTrust = mockk()
    private val rawRequest: VerifiedIdOpenIdJwtRawRequest = mockk()
    private lateinit var openIdPresentationRequest: OpenIdPresentationRequest

    @Test
    fun isSatisfied_ValidRequirement_ReturnsTrue() {
        // Arrange
        val mockVerifiedId: VerifiableCredential = mockk()
        val expectedVcType = "TestVc"
        requirement = VerifiedIdRequirement(
            "id",
            listOf(expectedVcType),
            VcTypeConstraint(expectedVcType),
            encrypted = false,
            required = true
        )
        openIdPresentationRequest = OpenIdPresentationRequest(
            requesterStyle,
            requirement,
            rootOfTrust,
            rawRequest
        )
        every { mockVerifiedId.types } returns listOf(expectedVcType)
        (requirement as VerifiedIdRequirement).fulfill(mockVerifiedId)

        // Act
        val actualResult = openIdPresentationRequest.isSatisfied()

        // Assert
        Assertions.assertThat(actualResult.isSuccess).isTrue
        Assertions.assertThat(actualResult.getOrNull()).isNotNull
        Assertions.assertThat(actualResult.getOrNull()).isTrue
    }

    @Test
    fun isSatisfied_RequirementNotFulfilled_ReturnsFalse() {
        // Arrange
        val expectedVcType = "TestVc"
        requirement = VerifiedIdRequirement(
            "id",
            listOf(expectedVcType),
            VcTypeConstraint(expectedVcType),
            encrypted = false,
            required = true
        )
        openIdPresentationRequest = OpenIdPresentationRequest(
            requesterStyle,
            requirement,
            rootOfTrust,
            rawRequest
        )

        // Act
        val actualResult = openIdPresentationRequest.isSatisfied()

        // Assert
        Assertions.assertThat(actualResult.isFailure).isTrue
        Assertions.assertThat(actualResult.exceptionOrNull()).isNotNull
        Assertions.assertThat(actualResult.exceptionOrNull()).isInstanceOf(VerifiedIdRequirementNotFulfilledException::class.java)
    }

    @Test
    fun isSatisfied_OneSatisfiedRequirementInGroupRequirementAllOperator_ReturnsFalse() {
        // Arrange
        setupGroupRequirement(FulFilledRequirement.ONE)

        // Act
        val actualResult = openIdPresentationRequest.isSatisfied()

        // Assert
        Assertions.assertThat(actualResult.isFailure).isTrue
        Assertions.assertThat(actualResult.exceptionOrNull()).isNotNull
        Assertions.assertThat(actualResult.exceptionOrNull()).isInstanceOf(VerifiedIdRequirementNotFulfilledException::class.java)
    }

    @Test
    fun isSatisfied_MultipleNotSatisfiedRequirementsInGroupRequirementAllOperator_ReturnsFalse() {
        // Arrange
        setupGroupRequirement(FulFilledRequirement.NONE)

        // Act
        val actualResult = openIdPresentationRequest.isSatisfied()

        // Assert
        Assertions.assertThat(actualResult.isFailure).isTrue
        Assertions.assertThat(actualResult.exceptionOrNull()).isNotNull
        Assertions.assertThat(actualResult.exceptionOrNull()).isInstanceOf(AggregateException::class.java)
        Assertions.assertThat((actualResult.exceptionOrNull() as AggregateException).exceptionsList.size).isEqualTo(2)
    }

    @Test
    fun isSatisfied_AllSatisfiedRequirementsInGroupRequirementAllOperator_ReturnsTrue() {
        // Arrange
        setupGroupRequirement(FulFilledRequirement.ALL)

        // Act
        val actualResult = openIdPresentationRequest.isSatisfied()

        // Assert
        Assertions.assertThat(actualResult.isSuccess).isTrue
        Assertions.assertThat(actualResult.getOrNull()).isNotNull
        Assertions.assertThat(actualResult.getOrNull()).isTrue
    }

    @Test
    fun completeRequest_ValidRequest_ReturnsSuccess() {
        // Arrange
        val expectedVcType = "TestVc"
        requirement = VerifiedIdRequirement(
            "id",
            listOf(expectedVcType),
            VcTypeConstraint(expectedVcType),
            encrypted = false,
            required = true
        )
        openIdPresentationRequest = OpenIdPresentationRequest(
            requesterStyle,
            requirement,
            rootOfTrust,
            rawRequest
        )
        mockkObject(OpenIdResponder)
        coJustRun {
            OpenIdResponder.sendPresentationResponse(
                openIdPresentationRequest.request.rawRequest,
                openIdPresentationRequest.requirement
            )
        }
        runBlocking {
            // Act
            val actualResult = openIdPresentationRequest.complete()

            // Assert
            Assertions.assertThat(actualResult.isSuccess).isTrue
        }
    }

    @Test
    fun completeRequest_PresentationFailureFromSdk_ThrowsException() {
        // Arrange
        val expectedVcType = "TestVc"
        requirement = VerifiedIdRequirement(
            "id",
            listOf(expectedVcType),
            VcTypeConstraint(expectedVcType),
            encrypted = false,
            required = true
        )
        openIdPresentationRequest = OpenIdPresentationRequest(
            requesterStyle,
            requirement,
            rootOfTrust,
            rawRequest
        )
        mockkObject(OpenIdResponder)
        coEvery {
            OpenIdResponder.sendPresentationResponse(
                openIdPresentationRequest.request.rawRequest,
                openIdPresentationRequest.requirement
            )
        }.throws(OpenIdResponseCompletionException())

        runBlocking {
            // Act
            val actualResult = openIdPresentationRequest.complete()

            // Assert
            Assertions.assertThat(actualResult.isFailure).isTrue
            Assertions.assertThat(actualResult.exceptionOrNull()).isInstanceOf(
                OpenIdResponseCompletionException::class.java
            )
        }
    }

    @Test
    fun completeRequest_FailureWhileAddingRequirements_ThrowsException() {
        // Arrange
        val expectedVcType = "TestVc"
        requirement = VerifiedIdRequirement(
            "id",
            listOf(expectedVcType),
            VcTypeConstraint(expectedVcType),
            encrypted = false,
            required = true
        )
        openIdPresentationRequest = OpenIdPresentationRequest(
            requesterStyle,
            requirement,
            rootOfTrust,
            rawRequest
        )
        val presentationRequest: PresentationRequest = mockk()
        val presentationRequestContent: PresentationRequestContent = mockk()
        val presentationDefinition: PresentationDefinition = mockk()
        every { rawRequest.rawRequest } returns presentationRequest
        every { presentationRequest.content } returns presentationRequestContent
        every { presentationRequestContent.clientId } returns ""
        every { presentationRequest.getPresentationDefinition() } returns presentationDefinition
        every { presentationDefinition.id } returns ""

        runBlocking {
            // Act
            val actualResult = openIdPresentationRequest.complete()

            // Assert
            Assertions.assertThat(actualResult.isFailure).isTrue
            Assertions.assertThat(actualResult.exceptionOrNull()).isInstanceOf(
                VerifiedIdRequirementNotFulfilledException::class.java
            )
        }
    }

    private fun setupGroupRequirement(fulFilledRequirement: FulFilledRequirement) {
        val expectedVcType1 = "VcType1"
        val verifiedIdRequirement1 = VerifiedIdRequirement(
            "id",
            listOf(expectedVcType1),
            VcTypeConstraint(expectedVcType1),
            encrypted = false,
            required = true
        )
        val expectedVcType2 = "VcType2"
        val verifiedIdRequirement2 = VerifiedIdRequirement(
            "id",
            listOf(expectedVcType2),
            VcTypeConstraint(expectedVcType2),
            encrypted = false,
            required = true
        )
        val groupRequirement = GroupRequirement(
            true,
            mutableListOf(verifiedIdRequirement1, verifiedIdRequirement2),
            GroupRequirementOperator.ALL
        )
        openIdPresentationRequest = OpenIdPresentationRequest(
            requesterStyle,
            groupRequirement,
            rootOfTrust,
            rawRequest
        )
        val mockVerifiedId1: VerifiableCredential = mockk()
        every { mockVerifiedId1.types } returns listOf(expectedVcType1)
        val mockVerifiedId2: VerifiableCredential = mockk()
        every { mockVerifiedId2.types } returns listOf(expectedVcType2)
        when (fulFilledRequirement) {
            FulFilledRequirement.ALL -> {
                verifiedIdRequirement1.fulfill(mockVerifiedId1)
                verifiedIdRequirement2.fulfill(mockVerifiedId2)
            }
            FulFilledRequirement.ONE -> verifiedIdRequirement1.fulfill(mockVerifiedId1)
            else -> {}
        }
    }
}