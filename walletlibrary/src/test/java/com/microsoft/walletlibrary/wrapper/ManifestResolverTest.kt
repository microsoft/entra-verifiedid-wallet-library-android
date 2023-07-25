package com.microsoft.walletlibrary.wrapper

import com.microsoft.walletlibrary.did.sdk.IssuanceService
import com.microsoft.walletlibrary.did.sdk.VerifiableCredentialSdk
import com.microsoft.walletlibrary.did.sdk.credential.service.IssuanceRequest
import com.microsoft.walletlibrary.did.sdk.credential.service.models.attestations.ClaimAttestation
import com.microsoft.walletlibrary.did.sdk.credential.service.models.attestations.CredentialAttestations
import com.microsoft.walletlibrary.did.sdk.credential.service.models.attestations.SelfIssuedAttestation
import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.InputContract
import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.VerifiableCredentialContract
import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.display.CardDescriptor
import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.display.ConsentDescriptor
import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.display.DisplayContract
import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainMissing
import com.microsoft.walletlibrary.did.sdk.util.controlflow.SdkException
import com.microsoft.walletlibrary.did.sdk.util.controlflow.Result
import com.microsoft.walletlibrary.requests.rawrequests.RawManifest
import com.microsoft.walletlibrary.requests.rawrequests.RequestType
import com.microsoft.walletlibrary.util.VerifiedIdRequestFetchException
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ManifestResolverTest {
    private val mockIssuanceService: IssuanceService = mockk()
    private val expectedClaimName = "name"
    private val expectedClaimType = "string"
    private val claimAttestation = ClaimAttestation(expectedClaimName, true, expectedClaimType)
    private val expectedIssuer = "issuer"
    private val expectedCardDescription = "card description"
    private val expectedTextColor = "#000000"
    private val expectedBackgroundColor = "#FFFFFF"
    private val expectedIssuerInCard = "Test Issuer"
    private val expectedCardTitle = "Card Title"
    private val expectedConsentTitle = "Consent Title"
    private val expectedConsentInstructions = "Consent Instructions"
    private val expectedContractUrl = "test.com"
    private val selfIssuedAttestation =
        SelfIssuedAttestation(required = true, claims = listOf(claimAttestation))
    private val inputContract = InputContract(
        "",
        "",
        expectedIssuer,
        CredentialAttestations(selfIssued = selfIssuedAttestation)
    )
    private val cardDescriptor = CardDescriptor(
        expectedCardTitle,
        expectedIssuerInCard,
        expectedBackgroundColor,
        expectedTextColor,
        null,
        expectedCardDescription
    )
    private val consentDescriptor =
        ConsentDescriptor(expectedConsentTitle, expectedConsentInstructions)
    private val displayContract =
        DisplayContract("", "", "", cardDescriptor, consentDescriptor, emptyMap())
    private val contract = VerifiableCredentialContract("", inputContract, displayContract)
    private val mockIssuanceRequest =
        IssuanceRequest(contract, expectedContractUrl, LinkedDomainMissing)

    init {
        setupInput(false)
    }

    private fun setupInput(isFailure: Boolean) {
        mockkStatic(VerifiableCredentialSdk::class)
        every { VerifiableCredentialSdk.issuanceService } returns mockIssuanceService
        if (!isFailure)
            coEvery { mockIssuanceService.getRequest(expectedContractUrl) } returns Result.Success(
                mockIssuanceRequest
            )
        else
            coEvery { mockIssuanceService.getRequest(expectedContractUrl) } returns Result.Failure(
                SdkException()
            )
    }

    @Test
    fun resolveIssuanceRequest_SuccessfulIssuanceRequestFromSdk_ReturnsManifestIssuanceRequest() {
        runBlocking {
            // Act
            val actualResult = ManifestResolver.getIssuanceRequest(expectedContractUrl)

            // Assert
            assertThat(actualResult).isInstanceOf(RawManifest::class.java)
            assertThat(actualResult.rawRequest).isEqualTo(mockIssuanceRequest)
            assertThat(actualResult.requestType).isEqualTo(RequestType.ISSUANCE)
        }
    }

    @Test
    fun resolveIssuanceRequest_FailureFromSdk_ThrowsException() {
        // Arrange
        setupInput(true)

        // Act and Assert
        Assertions.assertThatThrownBy {
            runBlocking {
                ManifestResolver.getIssuanceRequest(
                    expectedContractUrl
                )
            }
        }.isInstanceOf(
            VerifiedIdRequestFetchException::class.java
        )
    }
}