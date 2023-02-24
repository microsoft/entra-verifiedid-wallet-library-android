package com.microsoft.walletlibrary.requests.requirements

import com.microsoft.walletlibrary.util.PinRequirementNotFulfilledException
import org.assertj.core.api.Assertions
import org.junit.Test

class PinRequirementTest {
    private lateinit var pinRequirement: PinRequirement

    init {
        setupInput()
    }

    private fun setupInput() {
        pinRequirement = PinRequirement(
            "id",
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
        Assertions.assertThat(pinRequirement.pin).isNotNull
        Assertions.assertThat(pinRequirement.pin).isEqualTo(expectedPin)
    }

    @Test
    fun pinRequirement_validateUnFulfilledRequirement_ThrowsException() {
        // Act and Assert
        Assertions.assertThatThrownBy {
            pinRequirement.validate()
        }.isInstanceOf(PinRequirementNotFulfilledException::class.java)
    }

    @Test
    fun pinRequirement_validateFulfilledRequirement_SucceedsWithNoException() {
        // Arrange
        val expectedPin = "1234"
        pinRequirement.fulfill(expectedPin)

        // Act
        pinRequirement.validate()

        // Assert
        Assertions.assertThat(pinRequirement.pin).isNotNull
        Assertions.assertThat(pinRequirement.pin).isEqualTo(expectedPin)
    }
}