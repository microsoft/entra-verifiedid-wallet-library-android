package com.microsoft.walletlibrary.requests

import com.microsoft.walletlibrary.requests.rawrequests.RawManifest
import com.microsoft.walletlibrary.requests.requirements.GroupRequirement
import com.microsoft.walletlibrary.requests.requirements.GroupRequirementOperator
import com.microsoft.walletlibrary.requests.requirements.IdTokenRequirement
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
    private val rootOfTrust: RootOfTrust = mockk()
    private val verifiedIdStyle: VerifiedIDStyle = mockk()
    private val rawManifest: RawManifest = mockk()

    @Test
    fun isSatisfied_SelfAttestedRequirementFulfilled_ReturnsTrue() {
        // Arrange
        val selfAttestedClaimRequirement = SelfAttestedClaimRequirement(
            "id",
            "name",
            encrypted = false,
            required = true
        )
        val manifestIssuanceRequest = ManifestIssuanceRequest(
            requesterStyle,
            selfAttestedClaimRequirement,
            rootOfTrust,
            verifiedIdStyle,
            rawManifest
        )
        selfAttestedClaimRequirement.fulfill("test")

        // Act
        val actualResult = manifestIssuanceRequest.isSatisfied()

        // Assert
        assertThat(actualResult).isTrue
    }

    @Test
    fun isSatisfied_SelfAttestedRequirementNotFulfilled_ReturnsFalse() {
        // Arrange
        val selfAttestedClaimRequirement = SelfAttestedClaimRequirement(
            "id",
            "name",
            encrypted = false,
            required = true
        )
        val manifestIssuanceRequest = ManifestIssuanceRequest(
            requesterStyle,
            selfAttestedClaimRequirement,
            rootOfTrust,
            verifiedIdStyle,
            rawManifest
        )

        // Act
        val actualResult = manifestIssuanceRequest.isSatisfied()

        // Assert
        assertThat(actualResult).isFalse
    }

    @Test
    fun isSatisfied_OneRequirementNotFulfilledInGroupRequirement_ReturnsFalse() {
        // Arrange
        val manifestIssuanceRequest =
            setupGroupRequirement(fulfillSelfAttested = true, fulfillIdToken = false)

        // Act
        val actualResult = manifestIssuanceRequest.isSatisfied()

        // Assert
        assertThat(actualResult).isFalse
    }

    @Test
    fun isSatisfied_MultipleRequirementsNotFulfilledInGroupRequirement_ReturnsFalse() {
        // Arrange
        val manifestIssuanceRequest =
            setupGroupRequirement(fulfillSelfAttested = false, fulfillIdToken = false)

        // Act
        val actualResult = manifestIssuanceRequest.isSatisfied()

        // Assert
        assertThat(actualResult).isFalse
    }

    @Test
    fun isSatisfied_MultipleRequirementsFulfilledInGroupRequirement_ReturnsTrue() {
        // Arrange
        val manifestIssuanceRequest =
            setupGroupRequirement(fulfillSelfAttested = true, fulfillIdToken = true)

        // Act
        val actualResult = manifestIssuanceRequest.isSatisfied()

        // Assert
        assertThat(actualResult).isTrue
    }

    @Test
    fun completeIssuance_CompleteIssuanceSuccessfully_ReturnsVerifiedId() {
        // Arrange
        val selfAttestedClaimRequirement = SelfAttestedClaimRequirement(
            "id",
            "name",
            encrypted = false,
            required = true
        )
        val manifestIssuanceRequest = ManifestIssuanceRequest(
            requesterStyle,
            selfAttestedClaimRequirement,
            rootOfTrust,
            verifiedIdStyle,
            rawManifest
        )
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
    fun completeIssuance_CompleteIssuanceFailure_ThrowsException() {
        // Arrange
        val selfAttestedClaimRequirement = SelfAttestedClaimRequirement(
            "id",
            "name",
            encrypted = false,
            required = true
        )
        val manifestIssuanceRequest = ManifestIssuanceRequest(
            requesterStyle,
            selfAttestedClaimRequirement,
            rootOfTrust,
            verifiedIdStyle,
            rawManifest
        )
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
            assertThat(actualResult.exceptionOrNull()).isInstanceOf(
                VerifiedIdResponseCompletionException::class.java
            )
        }
    }

    private fun setupGroupRequirement(
        fulfillSelfAttested: Boolean,
        fulfillIdToken: Boolean
    ): ManifestIssuanceRequest {
        val selfAttestedClaimRequirement = SelfAttestedClaimRequirement(
            "id",
            "name",
            encrypted = false,
            required = true
        )
        val idTokenRequirement = IdTokenRequirement(
            "id",
            "configuration",
            "clientId",
            "redirectUri",
            "scope",
            "nonce",
            emptyList()
        )
        val groupRequirement = GroupRequirement(
            true,
            mutableListOf(selfAttestedClaimRequirement, idTokenRequirement),
            GroupRequirementOperator.ALL
        )
        val manifestIssuanceRequest = ManifestIssuanceRequest(
            requesterStyle,
            groupRequirement,
            rootOfTrust,
            verifiedIdStyle,
            rawManifest
        )
        if (fulfillSelfAttested)
            selfAttestedClaimRequirement.fulfill("Test")
        if (fulfillIdToken)
            idTokenRequirement.fulfill("Test IdToken")
        return manifestIssuanceRequest
    }
}