package com.microsoft.walletlibrary.requests.requirements.constraints

import com.microsoft.walletlibrary.util.VerifiedIdTypeIsNotRequestedTypeException
import com.microsoft.walletlibrary.verifiedid.VerifiableCredential
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class VcTypeConstraintTest {

    @Test
    fun doesMatch_VcTypeMatches_ReturnsTrue() {
        // Arrange
        val expectedVcType = "TestVC"
        val vcTypeConstraint = VcTypeConstraint(expectedVcType)
        val mockVerifiableCredential: VerifiableCredential = mockk()
        every { mockVerifiableCredential.types } returns listOf(expectedVcType)

        // Act
        val actualResult = vcTypeConstraint.doesMatch(mockVerifiableCredential)

        // Assert
        assertThat(actualResult).isTrue
    }

    @Test
    fun doesMatch_VcTypeDoesNotMatch_ReturnsFalse() {
        // Arrange
        val expectedVcType = "TestVC"
        val actualVcType = "TestVC1"
        val vcTypeConstraint = VcTypeConstraint(expectedVcType)
        val mockVerifiableCredential: VerifiableCredential = mockk()
        every { mockVerifiableCredential.types } returns listOf(actualVcType)

        // Act
        val actualResult = vcTypeConstraint.doesMatch(mockVerifiableCredential)

        // Assert
        assertThat(actualResult).isFalse
    }

    @Test
    fun doesMatch_VcTypeEmpty_ReturnsFalse() {
        // Arrange
        val expectedVcType = "TestVC"
        val vcTypeConstraint = VcTypeConstraint(expectedVcType)
        val mockVerifiableCredential: VerifiableCredential = mockk()
        every { mockVerifiableCredential.types } returns emptyList()

        // Act
        val actualResult = vcTypeConstraint.doesMatch(mockVerifiableCredential)

        // Assert
        assertThat(actualResult).isFalse
    }

    @Test
    fun doesMatch_VcContainsMultipleTypesAndMatches_ReturnsTrue() {
        // Arrange
        val expectedVcType = "TestVC"
        val vcTypeConstraint = VcTypeConstraint(expectedVcType)
        val mockVerifiableCredential: VerifiableCredential = mockk()
        every { mockVerifiableCredential.types } returns listOf(expectedVcType, "TestVC1")

        // Act
        val actualResult = vcTypeConstraint.doesMatch(mockVerifiableCredential)

        // Assert
        assertThat(actualResult).isTrue
    }

    @Test
    fun matches_VcTypeDoesNotMatchConstraint_ThrowsException() {
        // Arrange
        val expectedVcType = "TestVC"
        val vcTypeConstraint = VcTypeConstraint(expectedVcType)
        val mockVerifiableCredential: VerifiableCredential = mockk()
        every { mockVerifiableCredential.types } returns emptyList()

        // Act and Assert
        Assertions.assertThatThrownBy {
            vcTypeConstraint.matches(mockVerifiableCredential)
        }.isInstanceOf(VerifiedIdTypeIsNotRequestedTypeException::class.java)
    }

    @Test
    fun matches_VcTypeMatchesConstraint_DoesNotThrow() {
        // Arrange
        val expectedVcType = "TestVC"
        val vcTypeConstraint = VcTypeConstraint(expectedVcType)
        val mockVerifiableCredential: VerifiableCredential = mockk()
        every { mockVerifiableCredential.types } returns listOf(expectedVcType)

        // Act
        vcTypeConstraint.matches(mockVerifiableCredential)
    }
}