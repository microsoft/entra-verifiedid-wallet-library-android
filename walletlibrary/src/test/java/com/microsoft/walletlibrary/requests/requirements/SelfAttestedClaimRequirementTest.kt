package com.microsoft.walletlibrary.requests.requirements

import com.microsoft.walletlibrary.util.SelfAttestedClaimRequirementNotFulfilledException
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SelfAttestedClaimRequirementTest {
    private val expectedClaimName = "name"
    private lateinit var selfAttestedClaimRequirement: SelfAttestedClaimRequirement

    init {
        setupInput()
    }

    private fun setupInput() {
        selfAttestedClaimRequirement = SelfAttestedClaimRequirement(
            "", expectedClaimName,
            encrypted = false,
            required = true
        )
    }

    @Test
    fun selfAttestedRequirement_fulfillRequirement_AssignsValueInRequirement() {
        // Arrange
        val expectedClaimValue = "Test"

        // Act
        selfAttestedClaimRequirement.fulfill(expectedClaimValue)

        // Assert
        assertThat(selfAttestedClaimRequirement.value).isNotNull
        assertThat(selfAttestedClaimRequirement.value).isEqualTo(expectedClaimValue)
    }

    @Test
    fun selfAttestedRequirement_validateUnFulfilledRequirement_ThrowsException() {
        // Act and Assert
        val actualResult = selfAttestedClaimRequirement.validate()

        assertThat(actualResult.isFailure).isTrue
        assertThat(actualResult.exceptionOrNull()).isNotNull
        assertThat(actualResult.exceptionOrNull()).isInstanceOf(
            SelfAttestedClaimRequirementNotFulfilledException::class.java)
    }

    @Test
    fun selfAttestedRequirement_validateFulfilledRequirement_SucceedsWithNoException() {
        // Arrange
        val expectedClaimValue = "Test"
        selfAttestedClaimRequirement.fulfill(expectedClaimValue)

        // Act
        selfAttestedClaimRequirement.validate()

        // Assert
        assertThat(selfAttestedClaimRequirement.value).isNotNull
        assertThat(selfAttestedClaimRequirement.value).isEqualTo(expectedClaimValue)
    }
}