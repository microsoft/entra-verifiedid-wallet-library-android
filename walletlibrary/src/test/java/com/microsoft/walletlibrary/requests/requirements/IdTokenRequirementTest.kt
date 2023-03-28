package com.microsoft.walletlibrary.requests.requirements

import com.microsoft.walletlibrary.util.IdTokenRequirementNotFulfilledException
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
    fun idTokenRequirement_validateUnFulfilledRequirement_ThrowsException() {
        // Act
        val actualResult = idTokenRequirement.validate()

        // Assert
        assertThat(actualResult).isInstanceOf(Result::class.java)
        assertThat(actualResult.isFailure).isTrue
        assertThat(actualResult.exceptionOrNull()).isNotNull
        assertThat(actualResult.exceptionOrNull())
            .isInstanceOf(IdTokenRequirementNotFulfilledException::class.java)
    }

    @Test
    fun idTokenRequirement_validateFulfilledRequirement_SucceedsWithNoException() {
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