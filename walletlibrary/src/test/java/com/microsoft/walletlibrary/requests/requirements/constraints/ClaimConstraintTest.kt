package com.microsoft.walletlibrary.requests.requirements.constraints

import com.microsoft.did.sdk.credential.models.VerifiableCredentialContent
import com.microsoft.did.sdk.credential.models.VerifiableCredentialDescriptor
import com.microsoft.walletlibrary.util.VerifiedIdIssuerIsNotRequestedException
import com.microsoft.walletlibrary.verifiedid.VerifiableCredential
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ClaimConstraintTest {

    @Test
    fun doesMatch_VcIssuerMatches_ReturnsTrue() {
        // Arrange
        val expectedIssuer = "did:web:test"
        val claimConstraint =
            ClaimConstraint(listOf("$.issuer", "$.vc.issuer", "$.iss"), expectedIssuer)
        val mockVerifiableCredential: VerifiableCredential = mockk()
        val verifiableCredentialContent = VerifiableCredentialContent("urn:pic:71d9f132fa904325a6520e6bc6007c36", VerifiableCredentialDescriptor(listOf("https://www.w3.org/2018/credentials/v1"), listOf("VerifiableCredential", "BusinessCard"), mapOf("name" to "n", "company" to "m"), null, null), "did:ion:testsubject", "did:web:test", 1686870564, 1689462564)
        every { mockVerifiableCredential.raw.contents } returns verifiableCredentialContent

        // Act
        val actualResult = claimConstraint.doesMatch(mockVerifiableCredential)

        // Assert
        assertThat(actualResult).isTrue
    }

    @Test
    fun doesMatch_VcIssuerDoesNotMatch_ReturnsFalse() {
        // Arrange
        val expectedIssuer = "did:web:test"
        val actualIssuer = "did:web:invalid"
        val claimConstraint =
            ClaimConstraint(listOf("$.issuer", "$.vc.issuer", "$.iss"), expectedIssuer)
        val mockVerifiableCredential: VerifiableCredential = mockk()
        val verifiableCredentialContent = VerifiableCredentialContent("urn:pic:71d9f132fa904325a6520e6bc6007c36", VerifiableCredentialDescriptor(listOf("https://www.w3.org/2018/credentials/v1"), listOf("VerifiableCredential", "BusinessCard"), mapOf("name" to "n", "company" to "m"), null, null), "did:ion:testsubject", actualIssuer, 1686870564, 1689462564)
        every { mockVerifiableCredential.raw.contents } returns verifiableCredentialContent

        // Act
        val actualResult = claimConstraint.doesMatch(mockVerifiableCredential)

        // Assert
        assertThat(actualResult).isFalse
    }

    @Test
    fun doesMatch_VcIssuerEmpty_ReturnsFalse() {
        // Arrange
        val expectedIssuer = "did:web:test"
        val claimConstraint =
            ClaimConstraint(listOf("$.issuer", "$.vc.issuer", "$.iss"), expectedIssuer)
        val mockVerifiableCredential: VerifiableCredential = mockk()
        val verifiableCredentialContent = VerifiableCredentialContent("urn:pic:71d9f132fa904325a6520e6bc6007c36", VerifiableCredentialDescriptor(listOf("https://www.w3.org/2018/credentials/v1"), listOf("VerifiableCredential", "BusinessCard"), mapOf("name" to "n", "company" to "m"), null, null), "did:ion:testsubject", "", 1686870564, 1689462564)
        every { mockVerifiableCredential.raw.contents } returns verifiableCredentialContent

        // Act
        val actualResult = claimConstraint.doesMatch(mockVerifiableCredential)

        // Assert
        assertThat(actualResult).isFalse
    }

    @Test
    fun matches_VcIssuerDoesNotMatchConstraint_ThrowsException() {
        // Arrange
        val expectedIssuer = "did:web:test"
        val actualIssuer = "did:web:invalid"
        val claimConstraint =
            ClaimConstraint(listOf("$.issuer", "$.vc.issuer", "$.iss"), expectedIssuer)
        val mockVerifiableCredential: VerifiableCredential = mockk()
        val verifiableCredentialContent = VerifiableCredentialContent("urn:pic:71d9f132fa904325a6520e6bc6007c36", VerifiableCredentialDescriptor(listOf("https://www.w3.org/2018/credentials/v1"), listOf("VerifiableCredential", "BusinessCard"), mapOf("name" to "n", "company" to "m"), null, null), "did:ion:testsubject", actualIssuer, 1686870564, 1689462564)
        every { mockVerifiableCredential.raw.contents } returns verifiableCredentialContent

        // Act and Assert
        Assertions.assertThatThrownBy {
            claimConstraint.matches(mockVerifiableCredential)
        }.isInstanceOf(VerifiedIdIssuerIsNotRequestedException::class.java)
    }

    @Test
    fun matches_VcIssuerMatchesConstraint_DoesNotThrow() {
        // Arrange
        val expectedIssuer = "did:web:test"
        val claimConstraint =
            ClaimConstraint(listOf("$.issuer", "$.vc.issuer", "$.iss"), expectedIssuer)
        val mockVerifiableCredential: VerifiableCredential = mockk()
        val verifiableCredentialContent = VerifiableCredentialContent("urn:pic:71d9f132fa904325a6520e6bc6007c36", VerifiableCredentialDescriptor(listOf("https://www.w3.org/2018/credentials/v1"), listOf("VerifiableCredential", "BusinessCard"), mapOf("name" to "n", "company" to "m"), null, null), "did:ion:testsubject", "did:web:test", 1686870564, 1689462564)
        every { mockVerifiableCredential.raw.contents } returns verifiableCredentialContent

        // Act
        claimConstraint.matches(mockVerifiableCredential)
    }

    @Test
    fun matchPattern_StartsWithMatching_ReturnsTrue() {
        // Arrange
        val pattern = """/^test/gi"""
        val value = "test purposes"

        // Act
        val actualResult = ClaimConstraint(emptyList(), "").matchPattern(pattern, value)

        // Assert
        assertThat(actualResult).isTrue
    }

    @Test
    fun matchPattern_StartsWithNotMatching_ReturnsFalse() {
        // Arrange
        val pattern = """/^test/gi"""
        val value = "unit test purposes"

        // Act
        val actualResult = ClaimConstraint(emptyList(), "").matchPattern(pattern, value)

        // Assert
        assertThat(actualResult).isFalse
    }

    @Test
    fun matchPattern_ContainsMatching_ReturnsTrue() {
        // Arrange
        val pattern = "/test/gi"
        val value = "unit test purposes"

        // Act
        val actualResult = ClaimConstraint(emptyList(), "").matchPattern(pattern, value)

        // Assert
        assertThat(actualResult).isTrue
    }

    @Test
    fun matchPattern_ContainsNotMatching_ReturnsFalse() {
        // Arrange
        val pattern = "/test/gi"
        val value = "purposes"

        // Act
        val actualResult = ClaimConstraint(emptyList(), "").matchPattern(pattern, value)

        // Assert
        assertThat(actualResult).isFalse
    }

    @Test
    fun matchPattern_ValuesMatching_ReturnsTrue() {
        // Arrange
        val pattern = """/^test${'$'}/gi"""
        val value = "test"

        // Act
        val actualResult = ClaimConstraint(emptyList(), "").matchPattern(pattern, value)

        // Assert
        assertThat(actualResult).isTrue
    }

    @Test
    fun matchPattern_ValueNotMatching_ReturnsFalse() {
        // Arrange
        val pattern = """/^test${'$'}/gi"""
        val value = "test purposes"

        // Act
        val actualResult = ClaimConstraint(emptyList(), "").matchPattern(pattern, value)

        // Assert
        assertThat(actualResult).isFalse
    }

    @Test
    fun matchPattern_EnumValuesMatchingFirst_ReturnsTrue() {
        // Arrange
        val pattern = """/^purpose${'$'}|^test${'$'}/gi"""
        val value = "test"

        // Act
        val actualResult = ClaimConstraint(emptyList(), "").matchPattern(pattern, value)

        // Assert
        assertThat(actualResult).isTrue
    }

    @Test
    fun matchPattern_EnumValueNotMatchingLast_ReturnsTrue() {
        // Arrange
        val pattern = """/^purposes${'$'}|^test${'$'}/gi"""
        val value = "purposes"

        // Act
        val actualResult = ClaimConstraint(emptyList(), "").matchPattern(pattern, value)

        // Assert
        assertThat(actualResult).isTrue
    }

    @Test
    fun matchPattern_EnumValueNotMatchingLast1_ReturnsTrue() {
        // Arrange
        val pattern = """did:web:test"""
        val value = "did:web:test1"

        // Act
        val actualResult = ClaimConstraint(emptyList(), "").matchPattern(pattern, value)

        // Assert
        assertThat(actualResult).isTrue
    }
}