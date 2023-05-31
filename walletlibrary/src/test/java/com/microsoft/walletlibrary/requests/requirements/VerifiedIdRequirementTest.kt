package com.microsoft.walletlibrary.requests.requirements

import com.microsoft.walletlibrary.requests.requirements.constraints.GroupConstraint
import com.microsoft.walletlibrary.requests.requirements.constraints.GroupConstraintOperator
import com.microsoft.walletlibrary.requests.requirements.constraints.VcTypeConstraint
import com.microsoft.walletlibrary.util.NoMatchForAnyConstraintsException
import com.microsoft.walletlibrary.util.NoMatchForAtLeastOneConstraintException
import com.microsoft.walletlibrary.util.VerifiedIdRequirementNotFulfilledException
import com.microsoft.walletlibrary.util.VerifiedIdTypeIsNotRequestedTypeException
import com.microsoft.walletlibrary.verifiedid.VerifiableCredential
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class VerifiedIdRequirementTest {
    private lateinit var verifiedIdRequirement: VerifiedIdRequirement
    private val expectedVcType = "TestCredential"

    init {
        setupInput()
    }

    private fun setupInput() {
        verifiedIdRequirement = VerifiedIdRequirement(
            "id",
            listOf("TestCredential"),
            VcTypeConstraint(expectedVcType),
            encrypted = false,
            required = true,
            "testing purposes"
        )
    }

    @Test
    fun fulfillVerifiedIdRequirement_validVerifiedId_AssignsValueInRequirement() {
        // Arrange
        val expectedVerifiedId: VerifiableCredential = mockk()
        every { expectedVerifiedId.types } returns listOf(expectedVcType)

        // Act
        verifiedIdRequirement.fulfill(expectedVerifiedId)

        // Assert
        assertThat(verifiedIdRequirement.verifiedId).isNotNull
        assertThat(verifiedIdRequirement.verifiedId).isEqualTo(expectedVerifiedId)
    }

    @Test
    fun fulfillVerifiedIdRequirement_ConstraintDoesNotMatchVerifiedId_ReturnsFailure() {
        // Arrange
        val expectedVerifiedId: VerifiableCredential = mockk()
        every { expectedVerifiedId.types } returns listOf("WrongVcType")

        // Act
        val actualResult = verifiedIdRequirement.fulfill(expectedVerifiedId)

        // Assert
        assertThat(actualResult).isInstanceOf(Result::class.java)
        assertThat(actualResult.isFailure).isTrue
        assertThat(actualResult.exceptionOrNull()).isNotNull
        assertThat(actualResult.exceptionOrNull())
            .isInstanceOf(VerifiedIdTypeIsNotRequestedTypeException::class.java)
    }

    @Test
    fun fulfillVerifiedIdRequirement_GroupConstraintWithAnyOperatorAndNoVcTypeMatches_ThrowsException() {
        // Arrange
        val expectedVcType1 = "TestVC1"
        val firstVcTypeConstraint = VcTypeConstraint(expectedVcType1)
        val expectedVcType2 = "TestVC2"
        val secondVcTypeConstraint = VcTypeConstraint(expectedVcType2)
        val groupConstraint = GroupConstraint(
            listOf(firstVcTypeConstraint, secondVcTypeConstraint),
            GroupConstraintOperator.ANY
        )
        verifiedIdRequirement = VerifiedIdRequirement(
            "id",
            listOf("TestCredential"),
            groupConstraint,
            encrypted = false,
            required = true,
            "testing purposes"
        )
        val mockVerifiableCredential: VerifiableCredential = mockk()
        every { mockVerifiableCredential.types } returns listOf("TestVC")

        // Act
        val actualResult = verifiedIdRequirement.fulfill(mockVerifiableCredential)

        // Assert
        assertThat(actualResult).isInstanceOf(Result::class.java)
        assertThat(actualResult.isFailure).isTrue
        assertThat(actualResult.exceptionOrNull()).isNotNull
        assertThat(actualResult.exceptionOrNull())
            .isInstanceOf(NoMatchForAnyConstraintsException::class.java)
        (actualResult.exceptionOrNull() as NoMatchForAnyConstraintsException).exceptions.forEach {
            assertThat(it).isInstanceOf(VerifiedIdTypeIsNotRequestedTypeException::class.java)
        }
    }

    @Test
    fun fulfillVerifiedIdRequirement_GroupConstraintWithAllOperatorAndOneVcTypeDoesNotMatch_ThrowsException() {
        // Arrange
        val expectedVcType1 = "TestVC1"
        val firstVcTypeConstraint = VcTypeConstraint(expectedVcType1)
        val expectedVcType2 = "TestVC2"
        val secondVcTypeConstraint = VcTypeConstraint(expectedVcType2)
        val groupConstraint = GroupConstraint(
            listOf(firstVcTypeConstraint, secondVcTypeConstraint),
            GroupConstraintOperator.ALL
        )
        verifiedIdRequirement = VerifiedIdRequirement(
            "id",
            listOf("TestCredential"),
            groupConstraint,
            encrypted = false,
            required = true,
            "testing purposes"
        )

        val mockVerifiableCredential: VerifiableCredential = mockk()
        every { mockVerifiableCredential.types } returns listOf(expectedVcType1)

        // Act
        val actualResult = verifiedIdRequirement.fulfill(mockVerifiableCredential)

        // Assert
        assertThat(actualResult).isInstanceOf(Result::class.java)
        assertThat(actualResult.isFailure).isTrue
        assertThat(actualResult.exceptionOrNull()).isNotNull
        assertThat(actualResult.exceptionOrNull())
            .isInstanceOf(NoMatchForAtLeastOneConstraintException::class.java)
        (actualResult.exceptionOrNull() as NoMatchForAtLeastOneConstraintException).exceptions.forEach {
            assertThat(it).isInstanceOf(VerifiedIdTypeIsNotRequestedTypeException::class.java)
        }
    }

    @Test
    fun validateVerifiedIdRequirement_ConstraintDoesNotMatch_ReturnsFailure() {
        // Arrange
        val expectedVerifiedId: VerifiableCredential = mockk()
        every { expectedVerifiedId.types } returns listOf("WrongVcType")
        verifiedIdRequirement.verifiedId = expectedVerifiedId

        // Act
        val actualResult = verifiedIdRequirement.validate()

        // Assert
        assertThat(actualResult).isInstanceOf(Result::class.java)
        assertThat(actualResult.isFailure).isTrue
        assertThat(actualResult.exceptionOrNull()).isNotNull
        assertThat(actualResult.exceptionOrNull())
            .isInstanceOf(VerifiedIdTypeIsNotRequestedTypeException::class.java)
    }

    @Test
    fun validateVerifiedIdRequirement_UnFulfilledRequirement_ReturnsFailure() {
        // Act
        val actualResult = verifiedIdRequirement.validate()

        // Assert
        assertThat(actualResult).isInstanceOf(Result::class.java)
        assertThat(actualResult.isFailure).isTrue
        assertThat(actualResult.exceptionOrNull()).isNotNull
        assertThat(actualResult.exceptionOrNull())
            .isInstanceOf(VerifiedIdRequirementNotFulfilledException::class.java)
    }

    @Test
    fun validateVerifiedIdRequirement_ValidVerifiedId_ReturnsSuccess() {
        // Arrange
        val expectedVerifiedId: VerifiableCredential = mockk()
        every { expectedVerifiedId.types } returns listOf(expectedVcType)
        verifiedIdRequirement.fulfill(expectedVerifiedId)

        // Act
        val actualResult = verifiedIdRequirement.validate()

        // Assert
        assertThat(actualResult).isInstanceOf(Result::class.java)
        assertThat(actualResult.isSuccess).isTrue
        assertThat(verifiedIdRequirement.verifiedId).isNotNull
        assertThat(verifiedIdRequirement.verifiedId).isEqualTo(expectedVerifiedId)
    }

    @Test
    fun getMatches_DoesNotMatchVerifiedId_ReturnsEmptyList() {
        // Arrange
        val mockVerifiedId1: VerifiableCredential = mockk()
        every { mockVerifiedId1.types } returns listOf("VcType1")
        val mockVerifiedId2: VerifiableCredential = mockk()
        every { mockVerifiedId2.types } returns listOf("VcType2")

        // Act
        val actualResult = verifiedIdRequirement.getMatches(listOf(mockVerifiedId1, mockVerifiedId2))

        // Assert
        assertThat(actualResult.size).isEqualTo(0)
    }

    @Test
    fun getMatches_OneVerifiedIdMatches_ReturnsList() {
        // Arrange
        val mockVerifiedId1: VerifiableCredential = mockk()
        every { mockVerifiedId1.types } returns listOf(expectedVcType)
        val mockVerifiedId2: VerifiableCredential = mockk()
        every { mockVerifiedId2.types } returns listOf("VcType2")

        // Act
        val actualResult = verifiedIdRequirement.getMatches(listOf(mockVerifiedId1, mockVerifiedId2))

        // Assert
        assertThat(actualResult.size).isEqualTo(1)
        assertThat(actualResult.first()).isEqualTo(mockVerifiedId1)
    }

    @Test
    fun getMatches_MultipleVerifiedIdsMatch_ReturnsList() {
        // Arrange
        val mockVerifiedId1: VerifiableCredential = mockk()
        every { mockVerifiedId1.types } returns listOf(expectedVcType)
        val mockVerifiedId2: VerifiableCredential = mockk()
        every { mockVerifiedId2.types } returns listOf(expectedVcType)

        // Act
        val actualResult = verifiedIdRequirement.getMatches(listOf(mockVerifiedId1, mockVerifiedId2))

        // Assert
        assertThat(actualResult.size).isEqualTo(2)
        assertThat(actualResult.contains(mockVerifiedId1)).isTrue
        assertThat(actualResult.contains(mockVerifiedId2)).isTrue
    }
}