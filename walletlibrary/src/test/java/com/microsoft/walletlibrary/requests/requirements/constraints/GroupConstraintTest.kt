package com.microsoft.walletlibrary.requests.requirements.constraints

import com.microsoft.walletlibrary.verifiedid.VerifiableCredential
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class GroupConstraintTest {

    @Test
    fun doesMatch_AnyOperatorAndOneVcTypeMatches_ReturnsTrue() {
        // Arrange
        val expectedVcType1 = "TestVC1"
        val firstVcTypeConstraint = VcTypeConstraint(expectedVcType1)
        val expectedVcType2 = "TestVC2"
        val secondVcTypeConstraint = VcTypeConstraint(expectedVcType2)
        val groupConstraint = GroupConstraint(
            listOf(firstVcTypeConstraint, secondVcTypeConstraint),
            GroupConstraintOperator.ANY
        )
        val mockVerifiableCredential: VerifiableCredential = mockk()
        every { mockVerifiableCredential.types } returns listOf(expectedVcType1)

        // Act
        val actualResult = groupConstraint.doesMatch(mockVerifiableCredential)

        // Assert
        assertThat(actualResult).isTrue
    }

    @Test
    fun doesMatch_AnyOperatorAndNoVcTypeMatches_ReturnsFalse() {
        // Arrange
        val expectedVcType1 = "TestVC1"
        val firstVcTypeConstraint = VcTypeConstraint(expectedVcType1)
        val expectedVcType2 = "TestVC2"
        val secondVcTypeConstraint = VcTypeConstraint(expectedVcType2)
        val groupConstraint = GroupConstraint(
            listOf(firstVcTypeConstraint, secondVcTypeConstraint),
            GroupConstraintOperator.ANY
        )
        val mockVerifiableCredential: VerifiableCredential = mockk()
        every { mockVerifiableCredential.types } returns listOf("TestVC")

        // Act
        val actualResult = groupConstraint.doesMatch(mockVerifiableCredential)

        // Assert
        assertThat(actualResult).isFalse
    }

    @Test
    fun doesMatch_AllOperatorAndAllVcTypesMatch_ReturnsTrue() {
        // Arrange
        val expectedVcType1 = "TestVC1"
        val firstVcTypeConstraint = VcTypeConstraint(expectedVcType1)
        val expectedVcType2 = "TestVC2"
        val secondVcTypeConstraint = VcTypeConstraint(expectedVcType2)
        val groupConstraint = GroupConstraint(
            listOf(firstVcTypeConstraint, secondVcTypeConstraint),
            GroupConstraintOperator.ALL
        )
        val mockVerifiableCredential: VerifiableCredential = mockk()
        every { mockVerifiableCredential.types } returns listOf(expectedVcType1, expectedVcType2)

        // Act
        val actualResult = groupConstraint.doesMatch(mockVerifiableCredential)

        // Assert
        assertThat(actualResult).isTrue
    }

    @Test
    fun doesMatch_AllOperatorAndOneVcTypeDoesNotMatch_ReturnsFalse() {
        // Arrange
        val expectedVcType1 = "TestVC1"
        val firstVcTypeConstraint = VcTypeConstraint(expectedVcType1)
        val expectedVcType2 = "TestVC2"
        val secondVcTypeConstraint = VcTypeConstraint(expectedVcType2)
        val groupConstraint = GroupConstraint(
            listOf(firstVcTypeConstraint, secondVcTypeConstraint),
            GroupConstraintOperator.ALL
        )
        val mockVerifiableCredential: VerifiableCredential = mockk()
        every { mockVerifiableCredential.types } returns listOf(expectedVcType1)

        // Act
        val actualResult = groupConstraint.doesMatch(mockVerifiableCredential)

        // Assert
        assertThat(actualResult).isFalse
    }
}