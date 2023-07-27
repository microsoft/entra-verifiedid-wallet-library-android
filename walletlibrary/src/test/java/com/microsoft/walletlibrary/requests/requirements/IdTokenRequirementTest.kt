package com.microsoft.walletlibrary.requests.requirements

import com.microsoft.walletlibrary.util.RequirementNotMetException
import org.assertj.core.api.Assertions.assertThat
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
        assertThat(idTokenRequirement.idToken).isNotNull
        assertThat(idTokenRequirement.idToken).isEqualTo(expectedIdToken)
    }

    @Test
    fun idTokenRequirement_validateUnFulfilledRequirement_ReturnsFailure() {
        // Act
        val actualResult = idTokenRequirement.validate()

        // Assert
        assertThat(actualResult).isInstanceOf(Result::class.java)
        assertThat(actualResult.isFailure).isTrue
        assertThat(actualResult.exceptionOrNull()).isNotNull
        assertThat(actualResult.exceptionOrNull())
            .isInstanceOf(RequirementNotMetException::class.java)
            .hasMessage("Id Token has not been set.")
    }

    @Test
    fun idTokenRequirement_validateFulfilledRequirement_ReturnsSuccess() {
        // Arrange
        val expectedIdToken = "Test IdToken"
        idTokenRequirement.fulfill(expectedIdToken)

        // Act
        val actualResult = idTokenRequirement.validate()

        // Assert
        assertThat(actualResult).isInstanceOf(Result::class.java)
        assertThat(actualResult.isSuccess).isTrue
        assertThat(idTokenRequirement.idToken).isNotNull
        assertThat(idTokenRequirement.idToken).isEqualTo(expectedIdToken)
    }
}