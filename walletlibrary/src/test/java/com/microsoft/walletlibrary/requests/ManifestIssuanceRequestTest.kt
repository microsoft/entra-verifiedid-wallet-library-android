package com.microsoft.walletlibrary.requests

import com.microsoft.walletlibrary.requests.rawrequests.RawManifest
import com.microsoft.walletlibrary.requests.requirements.SelfAttestedClaimRequirement
import com.microsoft.walletlibrary.requests.styles.RequesterStyle
import com.microsoft.walletlibrary.requests.styles.VerifiedIDStyle
import com.microsoft.walletlibrary.util.VerifiedIdResponseCompletionException
import com.microsoft.walletlibrary.verifiedid.VerifiedId
import com.microsoft.walletlibrary.wrapper.VerifiedIdRequester
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ManifestIssuanceRequestTest {
    private val requesterStyle: RequesterStyle = mockk()
    private val requirement = SelfAttestedClaimRequirement(
        "id", "name",
        encrypted = false,
        required = true
    )
    private val rootOfTrust: RootOfTrust = mockk()
    private val verifiedIdStyle: VerifiedIDStyle = mockk()
    private val rawManifest: RawManifest = mockk()
    private val manifestIssuanceRequest = ManifestIssuanceRequest(
        requesterStyle,
        requirement,
        rootOfTrust,
        verifiedIdStyle,
        rawManifest
    )

    @Test
    fun manifestIssuanceRequest_SelfAttestedRequirementSatisfied_ReturnsTrue() {
        // Arrange
        requirement.fulfill("test")

        // Act
        val actualResult = manifestIssuanceRequest.isSatisfied()

        // Assert
        assertThat(actualResult).isTrue
    }

    @Test
    fun manifestIssuanceRequest_SelfAttestedRequirementNotSatisfied_ReturnsFalse() {
        // Act
        val actualResult = manifestIssuanceRequest.isSatisfied()

        // Assert
        assertThat(actualResult).isFalse
    }

    @Test
    fun manifestIssuanceRequest_CompleteIssuanceSuccessfully_ReturnsVerifiedId() {
        // Arrange
        val mockVerifiedId: VerifiedId = mockk()
        mockkObject(VerifiedIdRequester)
        coEvery {
            VerifiedIdRequester.sendIssuanceResponse(
                manifestIssuanceRequest.request.rawRequest,
                manifestIssuanceRequest.requirement
            )
        } returns mockVerifiedId
        runBlocking {
            // Act
            val actualResult = manifestIssuanceRequest.complete()

            // Assert
            assertThat(actualResult.isSuccess).isTrue
            assertThat(actualResult.getOrDefault("")).isInstanceOf(VerifiedId::class.java)
        }
    }

    @Test
    fun manifestIssuanceRequest_CompleteIssuanceFailure_ThrowsException() {
        // Arrange
        mockkObject(VerifiedIdRequester)
        coEvery {
            VerifiedIdRequester.sendIssuanceResponse(
                manifestIssuanceRequest.request.rawRequest,
                manifestIssuanceRequest.requirement
            )
        }.throws(VerifiedIdResponseCompletionException())
        runBlocking {
            // Act
            val actualResult = manifestIssuanceRequest.complete()

            // Assert
            assertThat(actualResult.isFailure).isTrue
            assertThat(actualResult.exceptionOrNull()).isInstanceOf(VerifiedIdResponseCompletionException::class.java)
        }
    }
}