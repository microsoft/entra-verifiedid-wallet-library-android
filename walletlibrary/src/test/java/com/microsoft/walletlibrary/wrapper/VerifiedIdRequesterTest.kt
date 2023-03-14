package com.microsoft.walletlibrary.wrapper

import com.microsoft.did.sdk.IssuanceService
import com.microsoft.did.sdk.VerifiableCredentialSdk
import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.credential.models.VerifiableCredentialContent
import com.microsoft.did.sdk.credential.models.VerifiableCredentialDescriptor
import com.microsoft.did.sdk.credential.service.IssuanceRequest
import com.microsoft.did.sdk.credential.service.models.attestations.ClaimAttestation
import com.microsoft.did.sdk.credential.service.models.attestations.CredentialAttestations
import com.microsoft.did.sdk.credential.service.models.attestations.SelfIssuedAttestation
import com.microsoft.did.sdk.credential.service.models.contracts.InputContract
import com.microsoft.did.sdk.credential.service.models.contracts.VerifiableCredentialContract
import com.microsoft.did.sdk.credential.service.models.contracts.display.CardDescriptor
import com.microsoft.did.sdk.credential.service.models.contracts.display.ConsentDescriptor
import com.microsoft.did.sdk.credential.service.models.contracts.display.DisplayContract
import com.microsoft.did.sdk.credential.service.models.linkedDomains.LinkedDomainMissing
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.SdkException
import com.microsoft.walletlibrary.mappings.issuance.toRequirement
import com.microsoft.walletlibrary.requests.requirements.Requirement
import com.microsoft.walletlibrary.requests.requirements.SelfAttestedClaimRequirement
import com.microsoft.walletlibrary.util.VerifiedIdResponseCompletionException
import com.microsoft.walletlibrary.verifiedid.VerifiedId
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class VerifiedIdRequesterTest {
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
    private val credentialAttestations = CredentialAttestations(selfIssued = selfIssuedAttestation)
    private val inputContract = InputContract(
        "",
        "",
        expectedIssuer,
        credentialAttestations
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
    private val mockVerifiableCredential: VerifiableCredential = mockk()
    private val mockVerifiableCredentialContent: VerifiableCredentialContent = mockk()
    private val mockVerifiableCredentialDescriptor: VerifiableCredentialDescriptor = mockk()
    private val expectedCredentialSubject = mutableMapOf<String, String>()
    private val expectedCredentialSubjectClaimName = "name"
    private val expectedCredentialSubjectClaimValue = "test"
    private lateinit var requirement: Requirement

    init {
        setupInput(false)
    }

    private fun setupInput(isFailure: Boolean) {
        requirement = credentialAttestations.toRequirement()
        (requirement as SelfAttestedClaimRequirement).fulfill("Test")
        mockkStatic(VerifiableCredentialSdk::class)
        every { VerifiableCredentialSdk.issuanceService } returns mockIssuanceService
        if (!isFailure) {
            coEvery { mockIssuanceService.sendResponse(any()) } returns Result.Success(
                mockVerifiableCredential
            )
            every { mockVerifiableCredential.jti } returns ""
            every { mockVerifiableCredential.raw } returns ""
            every { mockVerifiableCredential.contents } returns mockVerifiableCredentialContent
            every { mockVerifiableCredential.contents.iat } returns 1234567L
            every { mockVerifiableCredential.contents.exp } returns 0L
            every { mockVerifiableCredential.contents.vc } returns mockVerifiableCredentialDescriptor
            every { mockVerifiableCredentialDescriptor.type } returns listOf("testCredential")
            expectedCredentialSubject[expectedCredentialSubjectClaimName] = expectedCredentialSubjectClaimValue
            every { mockVerifiableCredentialDescriptor.credentialSubject } returns expectedCredentialSubject
        } else
            coEvery { mockIssuanceService.sendResponse(any()) } returns Result.Failure(SdkException())
    }

    @Test
    fun completeIssuanceRequest_SuccessfulVerifiedCredentialFromSdk_ReturnsVerifiedId() {
        runBlocking {
            // Act
            val actualResult =
                VerifiedIdRequester.sendIssuanceResponse(mockIssuanceRequest, requirement)

            // Assert
            assertThat(actualResult).isInstanceOf(VerifiedId::class.java)
            assertThat(actualResult).isInstanceOf(com.microsoft.walletlibrary.verifiedid.VerifiableCredential::class.java)
            assertThat(actualResult.issuedOn).isEqualTo(1234567L)
            assertThat(actualResult.getClaims().size).isEqualTo(1)
            assertThat(actualResult.getClaims().first().id).isEqualTo(expectedCredentialSubjectClaimName)
            assertThat(actualResult.getClaims().first().value).isEqualTo(expectedCredentialSubjectClaimValue)
        }
    }

    @Test
    fun completeIssuanceRequest_FailureFromSdk_ThrowsException() {
        // Arrange
        setupInput(true)

        // Act and Assert
        Assertions.assertThatThrownBy {
            runBlocking {
                VerifiedIdRequester.sendIssuanceResponse(
                    mockIssuanceRequest,
                    requirement
                )
            }
        }.isInstanceOf(
            VerifiedIdResponseCompletionException::class.java
        )
    }
}