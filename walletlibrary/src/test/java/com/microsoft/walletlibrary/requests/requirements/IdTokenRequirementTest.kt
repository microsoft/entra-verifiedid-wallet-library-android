package com.microsoft.walletlibrary.requests.requirements

import com.microsoft.walletlibrary.util.IdTokenRequirementNotFulfilledException
import org.assertj.core.api.Assertions
import org.junit.Test

class IdTokenRequirementTest {
    private lateinit var idTokenRequirement: IdTokenRequirement
    private val expectedClaimName = "name"
    private val requestedClaim = RequestedClaim(false, expectedClaimName, true)

    init {
        setupInput()
    }

    private fun setupInput() {
        idTokenRequirement = IdTokenRequirement(
            "id",
            "configuration",
            "",
            "",
            "",
            "",
            listOf(requestedClaim),
            encrypted = false,
            required = true
        )
    }

    @Test
    fun idTokenRequirement_fulfillRequirement_AssignsValueInRequirement() {
        // Arrange
        val expectedIdToken = "Test IdToken"

        // Act
        idTokenRequirement.fulfill(expectedIdToken)

        // Assert
        Assertions.assertThat(idTokenRequirement.idToken).isNotNull
        Assertions.assertThat(idTokenRequirement.idToken).isEqualTo(expectedIdToken)
    }

    @Test
    fun idTokenRequirement_validateUnFulfilledRequirement_ThrowsException() {
        // Act and Assert
        Assertions.assertThatThrownBy {
            idTokenRequirement.validate()
        }.isInstanceOf(IdTokenRequirementNotFulfilledException::class.java)
    }

    @Test
    fun idTokenRequirement_validateFulfilledRequirement_SucceedsWithNoException() {
        // Arrange
        val expectedIdToken = "Test IdToken"
        idTokenRequirement.fulfill(expectedIdToken)

        // Act
        idTokenRequirement.validate()

        // Assert
        Assertions.assertThat(idTokenRequirement.idToken).isNotNull
        Assertions.assertThat(idTokenRequirement.idToken).isEqualTo(expectedIdToken)
    }
}