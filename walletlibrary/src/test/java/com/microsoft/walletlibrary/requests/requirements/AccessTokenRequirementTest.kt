package com.microsoft.walletlibrary.requests.requirements

import com.microsoft.walletlibrary.util.AccessTokenRequirementNotFulfilledException
import org.assertj.core.api.Assertions
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
        Assertions.assertThat(accessTokenRequirement.accessToken).isNotNull
        Assertions.assertThat(accessTokenRequirement.accessToken).isEqualTo(expectedAccessToken)
    }

    @Test
    fun accessTokenRequirement_validateUnFulfilledRequirement_ThrowsException() {
        Assertions.assertThatThrownBy {
            accessTokenRequirement.validate()
        }.isInstanceOf(AccessTokenRequirementNotFulfilledException::class.java)
    }

    @Test
    fun accessTokenRequirement_validateFulfilledRequirement_SucceedsWithNoException() {
        // Arrange
        val expectedAccessToken = "Test AccessToken"
        accessTokenRequirement.fulfill(expectedAccessToken)

        // Act
        accessTokenRequirement.validate()

        // Assert
        Assertions.assertThat(accessTokenRequirement.accessToken).isNotNull
        Assertions.assertThat(accessTokenRequirement.accessToken).isEqualTo(expectedAccessToken)
    }
}