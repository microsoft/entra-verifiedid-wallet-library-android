package com.microsoft.walletlibrary.requests.requirements

import com.microsoft.walletlibrary.requests.requirements.constraints.VcTypeConstraint
import com.microsoft.walletlibrary.util.VerifiedIdRequirementDoesNotMatchConstraintsException
import com.microsoft.walletlibrary.util.VerifiedIdRequirementNotFulfilledException
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
            .isInstanceOf(VerifiedIdRequirementDoesNotMatchConstraintsException::class.java)
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
            .isInstanceOf(VerifiedIdRequirementDoesNotMatchConstraintsException::class.java)
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