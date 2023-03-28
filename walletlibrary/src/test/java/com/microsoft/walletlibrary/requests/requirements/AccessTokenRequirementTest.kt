package com.microsoft.walletlibrary.requests.requirements

import com.microsoft.walletlibrary.util.AccessTokenRequirementNotFulfilledException
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class AccessTokenRequirementTest {
    private lateinit var accessTokenRequirement: AccessTokenRequirement
    private val expectedClaimName = "name"
    private val requestedClaim = RequestedClaim(false, expectedClaimName, true)

    init {
        setupInput()
    }

    private fun setupInput() {
        accessTokenRequirement = AccessTokenRequirement(
            "id",
            "configuration",
            "",
            "",
            "",
            listOf(requestedClaim),
            encrypted = false,
            required = true
        )
    }

    @Test
    fun accessTokenRequirement_fulfillRequirement_AssignsValueInRequirement() {
        // Arrange
        val expectedAccessToken = "Test AccessToken"

        // Act
        accessTokenRequirement.fulfill(expectedAccessToken)

        // Assert
        assertThat(accessTokenRequirement.accessToken).isNotNull
        assertThat(accessTokenRequirement.accessToken).isEqualTo(expectedAccessToken)
    }

    @Test
    fun accessTokenRequirement_validateUnFulfilledRequirement_ReturnsFailure() {
        // Act
        val actualResult = accessTokenRequirement.validate()

        // Assert
        assertThat(actualResult).isInstanceOf(Result::class.java)
        assertThat(actualResult.isFailure).isTrue
        assertThat(actualResult.exceptionOrNull()).isNotNull
        assertThat(actualResult.exceptionOrNull()).isInstanceOf(AccessTokenRequirementNotFulfilledException::class.java)
    }

    @Test
    fun accessTokenRequirement_validateFulfilledRequirement_ReturnsSuccess() {
        // Arrange
        val expectedAccessToken = "Test AccessToken"
        accessTokenRequirement.fulfill(expectedAccessToken)

        // Act
        val actualResult = accessTokenRequirement.validate()

        // Assert
        assertThat(actualResult).isInstanceOf(Result::class.java)
        assertThat(actualResult.isSuccess).isTrue
        assertThat(accessTokenRequirement.accessToken).isNotNull
        assertThat(accessTokenRequirement.accessToken).isEqualTo(expectedAccessToken)
    }
}