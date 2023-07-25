package com.microsoft.walletlibrary.verifiedid

import com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredentialContent
import com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredentialDescriptor
import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.VerifiableCredentialContract
import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.display.CardDescriptor
import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.display.ClaimDescriptor
import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.display.DisplayContract
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.*

class VerifiableCredentialTest {
    private val verifiableCredentialFromSdk: com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredential =
        mockk()
    private val verifiableCredentialContract: VerifiableCredentialContract = mockk()
    private val mockVcContent: VerifiableCredentialContent = mockk()
    private val mockDisplayContract: DisplayContract = mockk()
    private val mockCardDescriptor: CardDescriptor = mockk()
    private val expectedCardTitle = "Test VC"
    private val expectedCardIssuer = "Test Issuer"
    private val expectedCardBackgroundColor = "#000000"
    private val expectedCardTextColor = "#ffffff"
    private val expectedCardDescription = "VC issued for testing purposes"

    private lateinit var verifiableCredential: VerifiableCredential
    private val expectedClaim1Name = "name"
    private val expectedClaim1Value = "test"
    private val expectedClaim2Name = "company"
    private val expectedClaim2Value = "test"
    private val expectedDisplayNameForClaim1 = "vc.credentialSubject.name"
    private val expectedDisplayNameForClaim2 = "vc.credentialSubject.company"
    private val expectedClaimLabelForClaim1 = "Test Name"
    private val expectedClaimLabelForClaim2 = "Test Company"

    @Test
    fun getClaims_EmptyDisplayContract_ReturnsClaimLabelsFromVc() {
        // Arrange
        every { verifiableCredentialContract.display } returns mockDisplayContract
        every { mockDisplayContract.claims } returns emptyMap()
        setupVcContent()
        setupDisplayContract()
        verifiableCredential =
            VerifiableCredential(verifiableCredentialFromSdk, verifiableCredentialContract)

        // Act
        val actualClaims = verifiableCredential.getClaims()

        // Assert
        assertThat(actualClaims.size).isEqualTo(2)
        assertThat(actualClaims.first().id).isEqualTo(expectedClaim1Name)
        assertThat(actualClaims.first().value).isEqualTo(expectedClaim1Value)
        assertThat(actualClaims.last().id).isEqualTo(expectedClaim2Name)
        assertThat(actualClaims.last().value).isEqualTo(expectedClaim2Value)
    }

    @Test
    fun getClaims_AllClaimLabelsPresentInDisplayContract_ReturnsClaimLabelsFromDisplayContract() {
        // Arrange
        val expectedClaimDescriptorForClaim1 = ClaimDescriptor("String", expectedClaimLabelForClaim1)
        val expectedClaimDescriptorForClaim2 = ClaimDescriptor("String", expectedClaimLabelForClaim2)
        val displayContractClaims = mapOf(
            expectedDisplayNameForClaim1 to expectedClaimDescriptorForClaim1,
            expectedDisplayNameForClaim2 to expectedClaimDescriptorForClaim2
        )
        every { verifiableCredentialContract.display } returns mockDisplayContract
        every { mockDisplayContract.claims } returns displayContractClaims
        setupVcContent()
        setupDisplayContract()
        verifiableCredential =
            VerifiableCredential(verifiableCredentialFromSdk, verifiableCredentialContract)

        // Act
        val actualClaims = verifiableCredential.getClaims()

        // Assert
        assertThat(actualClaims.size).isEqualTo(2)
        assertThat(actualClaims.first().id).isEqualTo(expectedClaimLabelForClaim1)
        assertThat(actualClaims.first().value).isEqualTo(expectedClaim1Value)
        assertThat(actualClaims.last().id).isEqualTo(expectedClaimLabelForClaim2)
        assertThat(actualClaims.last().value).isEqualTo(expectedClaim2Value)
    }

    @Test
    fun getClaims_OneClaimLabelPresentInDisplayContract_ReturnsClaimLabelsFromDisplayContractAndVc() {
        // Arrange
        val expectedClaimDescriptorForClaim1 = ClaimDescriptor("String", expectedClaimLabelForClaim1)
        val displayContractClaims = mapOf(expectedDisplayNameForClaim1 to expectedClaimDescriptorForClaim1)
        every { verifiableCredentialContract.display } returns mockDisplayContract
        every { mockDisplayContract.claims } returns displayContractClaims
        setupVcContent()
        setupDisplayContract()
        verifiableCredential =
            VerifiableCredential(verifiableCredentialFromSdk, verifiableCredentialContract)

        // Act
        val actualClaims = verifiableCredential.getClaims()

        // Assert
        assertThat(actualClaims.size).isEqualTo(2)
        assertThat(actualClaims.first().id).isEqualTo(expectedClaimLabelForClaim1)
        assertThat(actualClaims.first().value).isEqualTo(expectedClaim1Value)
        assertThat(actualClaims.last().id).isEqualTo(expectedClaim2Name)
        assertThat(actualClaims.last().value).isEqualTo(expectedClaim2Value)
    }

    @Test
    fun createVc_PopulateIssuedOn_ReturnsIssuedDateInVc() {
        // Arrange
        every { verifiableCredentialContract.display } returns mockDisplayContract
        every { mockDisplayContract.claims } returns emptyMap()
        setupVcContent()
        setupDisplayContract()
        val expectedIssuedDate = Date(5 * 1000)

        // Act
        verifiableCredential =
            VerifiableCredential(verifiableCredentialFromSdk, verifiableCredentialContract)

        // Assert
        assertThat(verifiableCredential.issuedOn).isEqualTo(expectedIssuedDate)
    }

    @Test
    fun createVc_PopulateExpiry_ReturnsExpiryDateInVc() {
        // Arrange
        every { verifiableCredentialContract.display } returns mockDisplayContract
        every { mockDisplayContract.claims } returns emptyMap()
        setupVcContent()
        setupDisplayContract()
        val expectedExpiryDate = Date(1000 * 1000)

        // Act
        verifiableCredential =
            VerifiableCredential(verifiableCredentialFromSdk, verifiableCredentialContract)

        // Assert
        assertThat(verifiableCredential.expiresOn).isEqualTo(expectedExpiryDate)
    }

    @Test
    fun createVc_PopulateNullExpiry_ReturnsNullExpiryDateInVc() {
        // Arrange
        every { verifiableCredentialContract.display } returns mockDisplayContract
        every { mockDisplayContract.claims } returns emptyMap()
        setupVcContent()
        setupDisplayContract()
        every { mockVcContent.exp } returns null

        // Act
        verifiableCredential =
            VerifiableCredential(verifiableCredentialFromSdk, verifiableCredentialContract)

        // Assert
        assertThat(verifiableCredential.expiresOn).isNull()
    }

    private fun setupVcContent() {
        every { verifiableCredentialFromSdk.jti } returns ""
        every { verifiableCredentialFromSdk.contents } returns mockVcContent
        every { mockVcContent.iat } returns 5
        every { mockVcContent.exp } returns 1000
        val mockVerifiableCredentialDescriptor: VerifiableCredentialDescriptor = mockk()
        val credentialSubjectMap = mapOf(
            expectedClaim1Name to expectedClaim1Value,
            expectedClaim2Name to expectedClaim2Value
        )
        every { mockVcContent.vc } returns mockVerifiableCredentialDescriptor
        every { mockVerifiableCredentialDescriptor.credentialSubject } returns credentialSubjectMap
        every { mockVerifiableCredentialDescriptor.type } returns listOf("TestCredential")
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
}