package com.microsoft.walletlibrary.requests.requirements

import com.microsoft.walletlibrary.util.PinRequirementNotFulfilledException
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PinRequirementTest {
    private lateinit var pinRequirement: PinRequirement

    init {
        setupInput()
    }

    private fun setupInput() {
        pinRequirement = PinRequirement(
            4,
            "numeric",
            true
        )
    }

    @Test
    fun pinRequirement_fulfillRequirement_AssignsValueInRequirement() {
        // Arrange
        val expectedPin = "1234"

        // Act
        pinRequirement.fulfill(expectedPin)

        // Assert
        assertThat(pinRequirement.pin).isNotNull
        assertThat(pinRequirement.pin).isEqualTo(expectedPin)
    }

    @Test
    fun pinRequirement_validateUnFulfilledRequirement_ReturnsFailure() {
        // Act
        val actualResult = pinRequirement.validate()

        // Assert
        assertThat(actualResult).isInstanceOf(Result::class.java)
        assertThat(actualResult.isFailure).isTrue
        assertThat(actualResult.exceptionOrNull()).isNotNull
        assertThat(actualResult.exceptionOrNull()).isInstanceOf(PinRequirementNotFulfilledException::class.java)
    }

    @Test
    fun pinRequirement_validateFulfilledRequirement_ReturnsSuccess() {
        // Arrange
        val expectedPin = "1234"
        pinRequirement.fulfill(expectedPin)

        // Act
        val actualResult = pinRequirement.validate()

        // Assert
        assertThat(actualResult).isInstanceOf(Result::class.java)
        assertThat(actualResult.isSuccess).isTrue
        assertThat(pinRequirement.pin).isNotNull
        assertThat(pinRequirement.pin).isEqualTo(expectedPin)
    }
}