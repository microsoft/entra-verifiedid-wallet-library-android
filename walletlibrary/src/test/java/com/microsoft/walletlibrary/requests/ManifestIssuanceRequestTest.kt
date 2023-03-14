package com.microsoft.walletlibrary.requests

import com.microsoft.walletlibrary.requests.rawrequests.RawManifest
import com.microsoft.walletlibrary.requests.requirements.*
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
    private lateinit var requirement: Requirement
    private val rootOfTrust: RootOfTrust = mockk()
    private val verifiedIdStyle: VerifiedIDStyle = mockk()
    private val rawManifest: RawManifest = mockk()
    private lateinit var manifestIssuanceRequest: ManifestIssuanceRequest

    @Test
    fun manifestIssuanceRequest_SelfAttestedRequirementSatisfied_ReturnsTrue() {
        // Arrange
        requirement = SelfAttestedClaimRequirement(
            "id", "name",
            encrypted = false,
            required = true
        )
        manifestIssuanceRequest = ManifestIssuanceRequest(
            requesterStyle,
            requirement,
            rootOfTrust,
            verifiedIdStyle,
            rawManifest
        )
        (requirement as SelfAttestedClaimRequirement).fulfill("test")

        // Act
        val actualResult = manifestIssuanceRequest.isSatisfied()

        // Assert
        assertThat(actualResult).isTrue
    }

    @Test
    fun manifestIssuanceRequest_SelfAttestedRequirementNotSatisfied_ReturnsFalse() {
        // Arrange
        requirement = SelfAttestedClaimRequirement(
            "id", "name",
            encrypted = false,
            required = true
        )
        manifestIssuanceRequest = ManifestIssuanceRequest(
            requesterStyle,
            requirement,
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
    fun manifestIssuanceRequest_OneNotSatisfiedRequirementInGroupRequirement_ReturnsFalse() {
        // Arrange
        setupGroupRequirement(fulfillSelfAttested = true, fulfillIdToken = false)

        // Act
        val actualResult = manifestIssuanceRequest.isSatisfied()

        // Assert
        assertThat(actualResult).isFalse
    }

    @Test
    fun manifestIssuanceRequest_MultipleNotSatisfiedRequirementsInGroupRequirement_ReturnsFalse() {
        // Arrange
        setupGroupRequirement(fulfillSelfAttested = false, fulfillIdToken = false)

        // Act
        val actualResult = manifestIssuanceRequest.isSatisfied()

        // Assert
        assertThat(actualResult).isFalse
    }

    @Test
    fun manifestIssuanceRequest_MultipleSatisfiedRequirementsInGroupRequirement_ReturnsTrue() {
        // Arrange
        setupGroupRequirement(fulfillSelfAttested = true, fulfillIdToken = true)

        // Act
        val actualResult = manifestIssuanceRequest.isSatisfied()

        // Assert
        assertThat(actualResult).isTrue
    }

    @Test
    fun manifestIssuanceRequest_CompleteIssuanceSuccessfully_ReturnsVerifiedId() {
        // Arrange
        requirement = SelfAttestedClaimRequirement(
            "id", "name",
            encrypted = false,
            required = true
        )
        manifestIssuanceRequest = ManifestIssuanceRequest(
            requesterStyle,
            requirement,
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
    fun manifestIssuanceRequest_CompleteIssuanceFailure_ThrowsException() {
        // Arrange
        requirement = SelfAttestedClaimRequirement(
            "id", "name",
            encrypted = false,
            required = true
        )
        manifestIssuanceRequest = ManifestIssuanceRequest(
            requesterStyle,
            requirement,
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

    private fun setupGroupRequirement(fulfillSelfAttested: Boolean, fulfillIdToken: Boolean) {
        val selfAttestedClaimRequirement = SelfAttestedClaimRequirement(
            "id", "name",
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
        manifestIssuanceRequest = ManifestIssuanceRequest(
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
    }
}