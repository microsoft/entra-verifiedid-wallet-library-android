package com.microsoft.walletlibrary.requests

import com.microsoft.walletlibrary.WalletLibraryFlight
import com.microsoft.walletlibrary.did.sdk.credential.service.PresentationRequest
import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainResult
import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainMissing
import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainVerified
import com.microsoft.walletlibrary.did.sdk.credential.service.models.oidc.PresentationRequestContent
import com.microsoft.walletlibrary.did.sdk.credential.service.models.presentationexchange.PresentationDefinition
import com.microsoft.walletlibrary.requests.rawrequests.VerifiedIdOpenIdJwtRawRequest
import com.microsoft.walletlibrary.requests.requirements.GroupRequirement
import com.microsoft.walletlibrary.requests.requirements.GroupRequirementOperator
import com.microsoft.walletlibrary.requests.requirements.Requirement
import com.microsoft.walletlibrary.requests.requirements.VerifiedIdRequirement
import com.microsoft.walletlibrary.requests.requirements.constraints.VcTypeConstraint
import com.microsoft.walletlibrary.requests.styles.RequesterStyle
import com.microsoft.walletlibrary.util.LibraryConfiguration
import com.microsoft.walletlibrary.util.OpenIdResponseCompletionException
import com.microsoft.walletlibrary.util.PreviewFeatureFlags
import com.microsoft.walletlibrary.util.RequirementNotMetException
import com.microsoft.walletlibrary.util.UnspecifiedVerifiedIdException
import com.microsoft.walletlibrary.util.UserCanceledException
import com.microsoft.walletlibrary.verifiedid.VerifiableCredential
import com.microsoft.walletlibrary.wrapper.OpenIdResponder
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
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
    private val mockLibraryConfiguration = mockk<LibraryConfiguration>()

    @Test
    fun isSatisfied_ValidRequirement_ReturnsTrue() {
        // Arrange
        val mockVerifiedId: VerifiableCredential = mockk()
        val expectedVcType = "TestVc"
        requirement = VerifiedIdRequirement(
            "id",
            listOf(expectedVcType),
            encrypted = false,
            required = true
        )
        (requirement as VerifiedIdRequirement).constraint = VcTypeConstraint(expectedVcType)
        openIdPresentationRequest = OpenIdPresentationRequest(
            requesterStyle,
            requirement,
            rootOfTrust,
            null,
            rawRequest,
            mockLibraryConfiguration
        )
        every { mockVerifiedId.types } returns listOf(expectedVcType)
        (requirement as VerifiedIdRequirement).fulfill(mockVerifiedId)

        // Act
        val actualResult = openIdPresentationRequest.isSatisfied()

        // Assert
        Assertions.assertThat(actualResult).isTrue
    }

    @Test
    fun isSatisfied_RequirementNotFulfilled_ReturnsFalse() {
        // Arrange
        val expectedVcType = "TestVc"
        requirement = VerifiedIdRequirement(
            "id",
            listOf(expectedVcType),
            encrypted = false,
            required = true
        )
        (requirement as VerifiedIdRequirement).constraint = VcTypeConstraint(expectedVcType)
        openIdPresentationRequest = OpenIdPresentationRequest(
            requesterStyle,
            requirement,
            rootOfTrust,
            null,
            rawRequest,
            mockLibraryConfiguration
        )

        // Act
        val actualResult = openIdPresentationRequest.isSatisfied()

        // Assert
        Assertions.assertThat(actualResult).isFalse
    }

    @Test
    fun isSatisfied_OneSatisfiedRequirementInGroupRequirementAllOperator_ReturnsFalse() {
        // Arrange
        setupGroupRequirement(FulFilledRequirement.ONE)

        // Act
        val actualResult = openIdPresentationRequest.isSatisfied()

        // Assert
        Assertions.assertThat(actualResult).isFalse
    }

    @Test
    fun isSatisfied_MultipleNotSatisfiedRequirementsInGroupRequirementAllOperator_ReturnsFalse() {
        // Arrange
        setupGroupRequirement(FulFilledRequirement.NONE)

        // Act
        val actualResult = openIdPresentationRequest.isSatisfied()

        // Assert
        Assertions.assertThat(actualResult).isFalse
    }

    @Test
    fun isSatisfied_AllSatisfiedRequirementsInGroupRequirementAllOperator_ReturnsTrue() {
        // Arrange
        setupGroupRequirement(FulFilledRequirement.ALL)

        // Act
        val actualResult = openIdPresentationRequest.isSatisfied()

        // Assert
        Assertions.assertThat(actualResult).isTrue
    }

    @Test
    fun completeRequest_ValidRequest_ReturnsSuccess() {
        // Arrange
        every { mockLibraryConfiguration.isPreviewFeatureEnabled(PreviewFeatureFlags.FEATURE_FLAG_PRESENTATION_EXCHANGE_SERIALIZATION_SUPPORT) } returns false
        every {
            mockLibraryConfiguration.isFlightEnabled(
                WalletLibraryFlight.PresentationResponseDestinationValidation
            )
        } returns false
        val expectedVcType = "TestVc"
        requirement = VerifiedIdRequirement(
            "id",
            listOf(expectedVcType),
            encrypted = false,
            required = true
        )
        (requirement as VerifiedIdRequirement).constraint = VcTypeConstraint(expectedVcType)
        openIdPresentationRequest = OpenIdPresentationRequest(
            requesterStyle,
            requirement,
            rootOfTrust,
            null,
            rawRequest,
            mockLibraryConfiguration
        )
        mockkObject(OpenIdResponder)
        coJustRun {
            OpenIdResponder.sendPresentationResponse(
                openIdPresentationRequest.request.presentationRequest,
                openIdPresentationRequest.requirement,
                additionalHeaders = emptyMap(),
                mockLibraryConfiguration
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
        every {
            mockLibraryConfiguration.isFlightEnabled(
                WalletLibraryFlight.PresentationResponseDestinationValidation
            )
        } returns false
        val expectedVcType = "TestVc"
        requirement = VerifiedIdRequirement(
            "id",
            listOf(expectedVcType),
            encrypted = false,
            required = true
        )
        (requirement as VerifiedIdRequirement).constraint = VcTypeConstraint(expectedVcType)
        openIdPresentationRequest = OpenIdPresentationRequest(
            requesterStyle,
            requirement,
            rootOfTrust,
            null,
            rawRequest,
            mockLibraryConfiguration
        )
        mockkObject(OpenIdResponder)
        coEvery {
            OpenIdResponder.sendPresentationResponse(
                openIdPresentationRequest.request.presentationRequest,
                openIdPresentationRequest.requirement,
                additionalHeaders = emptyMap(),
                mockLibraryConfiguration
            )
        }.throws(OpenIdResponseCompletionException())

        runBlocking {
            // Act
            val actualResult = openIdPresentationRequest.complete()

            // Assert
            Assertions.assertThat(actualResult.isFailure).isTrue
            Assertions.assertThat(actualResult.exceptionOrNull()).isInstanceOf(
                UnspecifiedVerifiedIdException::class.java
            )
        }
    }

    @Test
    fun responseUrl_usesVerifiedRootOfTrustSuppliedByProcessorExtension() {
        val extensionRootOfTrust = RootOfTrust("https://verifier.example:8443", true)
        val presentationRequest: PresentationRequest = mockk()
        val presentationRequestContent: PresentationRequestContent = mockk()
        every { rawRequest.presentationRequest } returns presentationRequest
        every { presentationRequest.content } returns presentationRequestContent
        every { presentationRequestContent.redirectUrl } returns
            "https://verifier.example:8443/callback"
        every { presentationRequest.linkedDomainResult } returns LinkedDomainMissing
        every {
            mockLibraryConfiguration.isFlightEnabled(
                WalletLibraryFlight.PresentationResponseDestinationValidation
            )
        } returns true
        val request = OpenIdPresentationRequest(
            requesterStyle,
            mockk(),
            extensionRootOfTrust,
            null,
            rawRequest,
            mockLibraryConfiguration
        )

        val responseUrl = request.responseUrl()

        Assertions.assertThat(responseUrl).isEqualTo("https://verifier.example:8443/callback")
    }

    @Test
    fun responseUrl_preservesOriginalValidatedOriginWhenRootOfTrustIsUnchanged() {
        val originalResult = LinkedDomainVerified(
            "verifier.example",
            validatedOrigin = "https://verifier.example:8443"
        )
        val originalRootOfTrust = RootOfTrust("verifier.example", true)
        val presentationRequest: PresentationRequest = mockk()
        val presentationRequestContent: PresentationRequestContent = mockk()
        every { rawRequest.presentationRequest } returns presentationRequest
        every { presentationRequest.content } returns presentationRequestContent
        every { presentationRequestContent.redirectUrl } returns
            "https://verifier.example:8443/callback"
        every { presentationRequest.linkedDomainResult } returns originalResult
        every {
            mockLibraryConfiguration.isFlightEnabled(
                WalletLibraryFlight.PresentationResponseDestinationValidation
            )
        } returns true
        val request = OpenIdPresentationRequest(
            requesterStyle,
            mockk(),
            originalRootOfTrust,
            null,
            rawRequest,
            mockLibraryConfiguration
        )

        val responseUrl = request.responseUrl()

        Assertions.assertThat(responseUrl).isEqualTo("https://verifier.example:8443/callback")
    }

    @Test
    fun completeRequest_deprecatedPathUsesRootOfTrustSuppliedByProcessorExtension() {
        val expectedRootOfTrust = RootOfTrust("https://verifier.example:8443", true)
        val expectedVcType = "TestVc"
        requirement = VerifiedIdRequirement(
            "id",
            listOf(expectedVcType),
            encrypted = false,
            required = true
        ).also {
            it.constraint = VcTypeConstraint(expectedVcType)
            it.fulfill(mockk<VerifiableCredential> {
                every { types } returns listOf(expectedVcType)
            })
        }
        val presentationRequest: PresentationRequest = mockk()
        every { rawRequest.presentationRequest } returns presentationRequest
        every { presentationRequest.linkedDomainResult } returns LinkedDomainMissing
        every {
            mockLibraryConfiguration.isPreviewFeatureEnabled(
                PreviewFeatureFlags.FEATURE_FLAG_PRESENTATION_EXCHANGE_SERIALIZATION_SUPPORT
            )
        } returns false
        every {
            mockLibraryConfiguration.isFlightEnabled(
                WalletLibraryFlight.PresentationResponseDestinationValidation
            )
        } returns true
        openIdPresentationRequest = OpenIdPresentationRequest(
            requesterStyle,
            requirement,
            expectedRootOfTrust,
            null,
            rawRequest,
            mockLibraryConfiguration
        )
        val linkedDomainResult = slot<LinkedDomainResult>()
        mockkObject(OpenIdResponder)
        coJustRun {
            OpenIdResponder.sendPresentationResponse(
                presentationRequest,
                requirement,
                emptyMap(),
                mockLibraryConfiguration,
                capture(linkedDomainResult)
            )
        }

        val actualResult = runBlocking { openIdPresentationRequest.complete() }

        Assertions.assertThat(actualResult.isSuccess).isTrue
        Assertions.assertThat(linkedDomainResult.captured).isInstanceOf(LinkedDomainVerified::class.java)
        Assertions.assertThat((linkedDomainResult.captured as LinkedDomainVerified).domainUrl)
            .isEqualTo(expectedRootOfTrust.source)
    }

    @Test
    fun completeRequest_FailureWhileAddingRequirements_ThrowsException() {
        // Arrange
        every { mockLibraryConfiguration.isPreviewFeatureEnabled(PreviewFeatureFlags.FEATURE_FLAG_PRESENTATION_EXCHANGE_SERIALIZATION_SUPPORT) } returns false
        every {
            mockLibraryConfiguration.isFlightEnabled(
                WalletLibraryFlight.PresentationResponseDestinationValidation
            )
        } returns false
        val expectedVcType = "TestVc"
        requirement = VerifiedIdRequirement(
            "id",
            listOf(expectedVcType),
            encrypted = false,
            required = true
        )
        (requirement as VerifiedIdRequirement).constraint = VcTypeConstraint(expectedVcType)
        openIdPresentationRequest = OpenIdPresentationRequest(
            requesterStyle,
            requirement,
            rootOfTrust,
            null,
            rawRequest,
            mockLibraryConfiguration
        )
        val presentationRequest: PresentationRequest = mockk()
        val presentationRequestContent: PresentationRequestContent = mockk()
        val presentationDefinition: PresentationDefinition = mockk()
        every { rawRequest.presentationRequest } returns presentationRequest
        every { presentationRequest.content } returns presentationRequestContent
        every { presentationRequestContent.clientId } returns ""
        every { presentationRequest.getPresentationDefinitions() } returns listOf(presentationDefinition)
        every { presentationDefinition.id } returns ""

        runBlocking {
            // Act
            val actualResult = openIdPresentationRequest.complete()

            // Assert
            Assertions.assertThat(actualResult.isFailure).isTrue
            Assertions.assertThat(actualResult.exceptionOrNull()).isInstanceOf(
                RequirementNotMetException::class.java
            )
                .hasMessage("Verified ID has not been set.")
        }
    }

    @Test
    fun cancelRequest_throwsUserCanceledException() {
        // Arrange
        val expectedVcType = "TestVc"
        requirement = VerifiedIdRequirement(
            "id",
            listOf(expectedVcType),
            encrypted = false,
            required = true
        )
        (requirement as VerifiedIdRequirement).constraint = VcTypeConstraint(expectedVcType)
        openIdPresentationRequest = OpenIdPresentationRequest(
            requesterStyle,
            requirement,
            rootOfTrust,
            null,
            rawRequest,
            mockLibraryConfiguration
        )

        // Act
        val actualResult = runBlocking { openIdPresentationRequest.cancel() }

        // Assert
        Assertions.assertThat(actualResult.isFailure).isTrue
        Assertions.assertThat(actualResult.exceptionOrNull()).isInstanceOf(
            UserCanceledException::class.java
        )
    }

    private fun setupGroupRequirement(fulFilledRequirement: FulFilledRequirement) {
        val expectedVcType1 = "VcType1"
        val verifiedIdRequirement1 = VerifiedIdRequirement(
            "id",
            listOf(expectedVcType1),
            encrypted = false,
            required = true
        )
        verifiedIdRequirement1.constraint = VcTypeConstraint(expectedVcType1)
        val expectedVcType2 = "VcType2"
        val verifiedIdRequirement2 = VerifiedIdRequirement(
            "id",
            listOf(expectedVcType2),
            encrypted = false,
            required = true
        )
        verifiedIdRequirement2.constraint = VcTypeConstraint(expectedVcType2)
        val groupRequirement = GroupRequirement(
            true,
            mutableListOf(verifiedIdRequirement1, verifiedIdRequirement2),
            GroupRequirementOperator.ALL
        )
        openIdPresentationRequest = OpenIdPresentationRequest(
            requesterStyle,
            groupRequirement,
            rootOfTrust,
            null,
            rawRequest,
            mockLibraryConfiguration
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