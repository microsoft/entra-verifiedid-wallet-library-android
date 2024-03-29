package com.microsoft.walletlibrary.requests.requirements

import com.microsoft.walletlibrary.util.RequirementNotMetException
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class GroupRequirementTest {
    private val expectedClaimName = "name"
    private lateinit var selfAttestedClaimRequirement: SelfAttestedClaimRequirement
    private lateinit var groupRequirement: GroupRequirement
    private lateinit var idTokenRequirement: IdTokenRequirement
    private val requestedClaim = RequestedClaim(false, expectedClaimName, true)

    init {
        setupInput()
    }

    private fun setupInput() {
        selfAttestedClaimRequirement = SelfAttestedClaimRequirement(
            "", expectedClaimName,
            encrypted = false,
            required = true
        )
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
        groupRequirement = GroupRequirement(
            true,
            mutableListOf(selfAttestedClaimRequirement, idTokenRequirement),
            GroupRequirementOperator.ALL
        )
    }

    @Test
    fun groupRequirement_validateUnFulfilledRequirement_ReturnsFailure() {
        // Act
        val actualResult = groupRequirement.validate()

        // Assert
        assertThat(actualResult).isInstanceOf(Result::class.java)
        assertThat(actualResult.isFailure).isTrue
        assertThat(actualResult.exceptionOrNull()).isNotNull
        assertThat(actualResult.exceptionOrNull()).isInstanceOf(RequirementNotMetException::class.java)
        (actualResult.exceptionOrNull() as RequirementNotMetException).innerErrors?.forEach {
            assertThat(it).isInstanceOf(RequirementNotMetException::class.java)
                .hasMessage("Id Token has not been set.")
        }
    }

    @Test
    fun groupRequirement_validateAllRequirementsFulfilled_ReturnsSuccess() {
        // Arrange
        val expectedClaimValue = "Test"
        selfAttestedClaimRequirement.fulfill(expectedClaimValue)
        val expectedIdToken = "Test IdToken"
        idTokenRequirement.fulfill(expectedIdToken)

        // Act
        val actualResult = groupRequirement.validate()

        // Assert
        assertThat(actualResult).isInstanceOf(Result::class.java)
        assertThat(actualResult.isSuccess).isTrue
        assertThat(groupRequirement.requirements.size).isEqualTo(2)
        assertThat((groupRequirement.requirements.first() as SelfAttestedClaimRequirement).value)
            .isEqualTo(expectedClaimValue)
        assertThat((groupRequirement.requirements.last() as IdTokenRequirement).idToken)
            .isEqualTo(expectedIdToken)
    }

    @Test
    fun groupRequirement_validateOneFulfilledAndOneUnFulfilledRequirement_ReturnsFailure() {
        // Arrange
        val expectedClaimValue = "Test"
        selfAttestedClaimRequirement.fulfill(expectedClaimValue)

        // Act
        val actualResult = groupRequirement.validate()

        // Assert
        assertThat(actualResult).isInstanceOf(Result::class.java)
        assertThat(actualResult.isFailure).isTrue
        assertThat(actualResult.exceptionOrNull()).isNotNull
        assertThat(actualResult.exceptionOrNull()).isInstanceOf(RequirementNotMetException::class.java)
        (actualResult.exceptionOrNull() as RequirementNotMetException).innerErrors?.forEach {
            assertThat(it).isInstanceOf(RequirementNotMetException::class.java)
                .hasMessage("Id Token has not been set.")
        }
    }
}