package com.microsoft.walletlibrary.requests.requirements

import com.microsoft.walletlibrary.util.VerifiedIdRequirementNotFulfilledException
import com.microsoft.walletlibrary.verifiedid.VerifiedId
import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.junit.Test

class VerifiedIdRequirementTest {
    private lateinit var verifiedIdRequirement: VerifiedIdRequirement

    init {
        setupInput()
    }

    private fun setupInput() {
        verifiedIdRequirement = VerifiedIdRequirement(
            "id",
            listOf("TestCredential"),
            encrypted = false,
            required = true,
            "testing purposes"
        )
    }

    @Test
    fun verifiedIdRequirement_fulfillRequirement_AssignsValueInRequirement() {
        // Arrange
        val expectedVerifiedId: VerifiedId = mockk()

        // Act
        verifiedIdRequirement.fulfill(expectedVerifiedId)

        // Assert
        Assertions.assertThat(verifiedIdRequirement.verifiedId).isNotNull
        Assertions.assertThat(verifiedIdRequirement.verifiedId).isEqualTo(expectedVerifiedId)
    }

    @Test
    fun verifiedIdRequirement_validateUnFulfilledRequirement_ThrowsException() {
        Assertions.assertThatThrownBy {
            verifiedIdRequirement.validate()
        }.isInstanceOf(VerifiedIdRequirementNotFulfilledException::class.java)
    }

    @Test
    fun verifiedIdRequirement_validateFulfilledRequirement_SucceedsWithNoException() {
        // Arrange
        val expectedVerifiedId: VerifiedId = mockk()
        verifiedIdRequirement.fulfill(expectedVerifiedId)

        // Act
        verifiedIdRequirement.validate()

        // Assert
        Assertions.assertThat(verifiedIdRequirement.verifiedId).isNotNull
        Assertions.assertThat(verifiedIdRequirement.verifiedId).isEqualTo(expectedVerifiedId)
    }
}