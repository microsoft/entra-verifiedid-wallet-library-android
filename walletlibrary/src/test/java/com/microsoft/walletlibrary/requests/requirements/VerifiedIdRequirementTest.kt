package com.microsoft.walletlibrary.requests.requirements

import com.microsoft.did.sdk.credential.models.VerifiableCredentialContent
import com.microsoft.did.sdk.credential.models.VerifiableCredentialDescriptor
import com.microsoft.walletlibrary.requests.requirements.constraints.VcPathRegexConstraint
import com.microsoft.walletlibrary.requests.requirements.constraints.GroupConstraint
import com.microsoft.walletlibrary.requests.requirements.constraints.GroupConstraintOperator
import com.microsoft.walletlibrary.requests.requirements.constraints.VcTypeConstraint
import com.microsoft.walletlibrary.util.NoMatchForAnyConstraintsException
import com.microsoft.walletlibrary.util.NoMatchForAtLeastOneConstraintException
import com.microsoft.walletlibrary.util.VerifiedIdRequirementNotFulfilledException
import com.microsoft.walletlibrary.util.VerifiedIdTypeIsNotRequestedTypeException
import com.microsoft.walletlibrary.verifiedid.VerifiableCredential
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class VerifiedIdRequirementTest {
    private lateinit var verifiedIdRequirement: VerifiedIdRequirement
    private val expectedVcType = "TestCredential"

    init {
        setupInput()
    }

    private fun setupInput() {
        verifiedIdRequirement = VerifiedIdRequirement(
            "id",
            listOf("TestCredential"),
            VcTypeConstraint(expectedVcType),
            encrypted = false,
            required = true,
            "testing purposes",
        )
    }

    @Test
    fun fulfillVerifiedIdRequirement_validVerifiedId_AssignsValueInRequirement() {
        // Arrange
        val expectedVerifiedId: VerifiableCredential = mockk()
        every { expectedVerifiedId.types } returns listOf(expectedVcType)

        // Act
        verifiedIdRequirement.fulfill(expectedVerifiedId)

        // Assert
        assertThat(verifiedIdRequirement.verifiedId).isNotNull
        assertThat(verifiedIdRequirement.verifiedId).isEqualTo(expectedVerifiedId)
    }

    @Test
    fun fulfillVerifiedIdRequirement_VcTypeConstraintDoesNotMatchVerifiedId_ReturnsFailure() {
        // Arrange
        val expectedVerifiedId: VerifiableCredential = mockk()
        every { expectedVerifiedId.types } returns listOf("WrongVcType")

        // Act
        val actualResult = verifiedIdRequirement.fulfill(expectedVerifiedId)

        // Assert
        assertThat(actualResult).isInstanceOf(Result::class.java)
        assertThat(actualResult.isFailure).isTrue
        assertThat(actualResult.exceptionOrNull()).isNotNull
        assertThat(actualResult.exceptionOrNull()).isInstanceOf(
            VerifiedIdTypeIsNotRequestedTypeException::class.java
        )
    }

    @Test
    fun fulfillVerifiedIdRequirement_GroupConstraintWithAnyOperatorAndNoVcTypeMatches_ThrowsException() {
        // Arrange
        val expectedVcType1 = "TestVC1"
        val firstVcTypeConstraint = VcTypeConstraint(expectedVcType1)
        val expectedVcType2 = "TestVC2"
        val secondVcTypeConstraint = VcTypeConstraint(expectedVcType2)
        val groupConstraint = GroupConstraint(
            listOf(firstVcTypeConstraint, secondVcTypeConstraint), GroupConstraintOperator.ANY
        )
        verifiedIdRequirement = VerifiedIdRequirement(
            "id",
            listOf("TestCredential"),
            groupConstraint,
            encrypted = false,
            required = true,
            "testing purposes"
        )
        val mockVerifiableCredential: VerifiableCredential = mockk()
        every { mockVerifiableCredential.types } returns listOf("TestVC")

        // Act
        val actualResult = verifiedIdRequirement.fulfill(mockVerifiableCredential)

        // Assert
        assertThat(actualResult).isInstanceOf(Result::class.java)
        assertThat(actualResult.isFailure).isTrue
        assertThat(actualResult.exceptionOrNull()).isNotNull
        assertThat(actualResult.exceptionOrNull()).isInstanceOf(NoMatchForAnyConstraintsException::class.java)
        (actualResult.exceptionOrNull() as NoMatchForAnyConstraintsException).exceptions.forEach {
            assertThat(it).isInstanceOf(VerifiedIdTypeIsNotRequestedTypeException::class.java)
        }
    }

    @Test
    fun fulfillVerifiedIdRequirement_GroupConstraintWithAllOperatorAndOneVcTypeDoesNotMatch_ThrowsException() {
        // Arrange
        val expectedVcType1 = "TestVC1"
        val firstVcTypeConstraint = VcTypeConstraint(expectedVcType1)
        val expectedVcType2 = "TestVC2"
        val secondVcTypeConstraint = VcTypeConstraint(expectedVcType2)
        val groupConstraint = GroupConstraint(
            listOf(firstVcTypeConstraint, secondVcTypeConstraint), GroupConstraintOperator.ALL
        )
        verifiedIdRequirement = VerifiedIdRequirement(
            "id",
            listOf("TestCredential"),
            groupConstraint,
            encrypted = false,
            required = true,
            "testing purposes"
        )

        val mockVerifiableCredential: VerifiableCredential = mockk()
        every { mockVerifiableCredential.types } returns listOf(expectedVcType1)

        // Act
        val actualResult = verifiedIdRequirement.fulfill(mockVerifiableCredential)

        // Assert
        assertThat(actualResult).isInstanceOf(Result::class.java)
        assertThat(actualResult.isFailure).isTrue
        assertThat(actualResult.exceptionOrNull()).isNotNull
        assertThat(actualResult.exceptionOrNull()).isInstanceOf(
            NoMatchForAtLeastOneConstraintException::class.java
        )
        (actualResult.exceptionOrNull() as NoMatchForAtLeastOneConstraintException).exceptions.forEach {
            assertThat(it).isInstanceOf(VerifiedIdTypeIsNotRequestedTypeException::class.java)
        }
    }

    @Test
    fun validateVerifiedIdRequirement_VcTypeConstraintDoesNotMatch_ReturnsFailure() {
        // Arrange
        val expectedVerifiedId: VerifiableCredential = mockk()
        every { expectedVerifiedId.types } returns listOf("WrongVcType")
        verifiedIdRequirement.verifiedId = expectedVerifiedId

        // Act
        val actualResult = verifiedIdRequirement.validate()

        // Assert
        assertThat(actualResult).isInstanceOf(Result::class.java)
        assertThat(actualResult.isFailure).isTrue
        assertThat(actualResult.exceptionOrNull()).isNotNull
        assertThat(actualResult.exceptionOrNull()).isInstanceOf(
            VerifiedIdTypeIsNotRequestedTypeException::class.java
        )
    }

    @Test
    fun validateVerifiedIdRequirement_UnFulfilledRequirement_ReturnsFailure() {
        // Act
        val actualResult = verifiedIdRequirement.validate()

        // Assert
        assertThat(actualResult).isInstanceOf(Result::class.java)
        assertThat(actualResult.isFailure).isTrue
        assertThat(actualResult.exceptionOrNull()).isNotNull
        assertThat(actualResult.exceptionOrNull()).isInstanceOf(
            VerifiedIdRequirementNotFulfilledException::class.java
        )
    }

    @Test
    fun validateVerifiedIdRequirement_ValidVerifiedId_ReturnsSuccess() {
        // Arrange
        val expectedVerifiedId: VerifiableCredential = mockk()
        every { expectedVerifiedId.types } returns listOf(expectedVcType)
        verifiedIdRequirement.fulfill(expectedVerifiedId)

        // Act
        val actualResult = verifiedIdRequirement.validate()

        // Assert
        assertThat(actualResult).isInstanceOf(Result::class.java)
        assertThat(actualResult.isSuccess).isTrue
        assertThat(verifiedIdRequirement.verifiedId).isNotNull
        assertThat(verifiedIdRequirement.verifiedId).isEqualTo(expectedVerifiedId)
    }

    @Test
    fun getMatches_VcTypeConstraintDoesNotMatchVerifiedId_ReturnsEmptyList() {
        // Arrange
        val mockVerifiedId1: VerifiableCredential = mockk()
        every { mockVerifiedId1.types } returns listOf("VcType1")
        val mockVerifiedId2: VerifiableCredential = mockk()
        every { mockVerifiedId2.types } returns listOf("VcType2")

        // Act
        val actualResult =
            verifiedIdRequirement.getMatches(listOf(mockVerifiedId1, mockVerifiedId2))

        // Assert
        assertThat(actualResult.size).isEqualTo(0)
    }

    @Test
    fun getMatches_OneVerifiedIdMatchesVcTypeConstraint_ReturnsListWithMatchingVc() {
        // Arrange
        val mockVerifiedId1: VerifiableCredential = mockk()
        every { mockVerifiedId1.types } returns listOf(expectedVcType)
        val mockVerifiedId2: VerifiableCredential = mockk()
        every { mockVerifiedId2.types } returns listOf("VcType2")

        // Act
        val actualResult =
            verifiedIdRequirement.getMatches(listOf(mockVerifiedId1, mockVerifiedId2))

        // Assert
        assertThat(actualResult.size).isEqualTo(1)
        assertThat(actualResult.first()).isEqualTo(mockVerifiedId1)
    }

    @Test
    fun getMatches_MultipleVerifiedIdsMatchVcTypeConstraint_ReturnsListOfMatchingVcs() {
        // Arrange
        val mockVerifiedId1: VerifiableCredential = mockk()
        every { mockVerifiedId1.types } returns listOf(expectedVcType)
        val mockVerifiedId2: VerifiableCredential = mockk()
        every { mockVerifiedId2.types } returns listOf(expectedVcType)

        // Act
        val actualResult =
            verifiedIdRequirement.getMatches(listOf(mockVerifiedId1, mockVerifiedId2))

        // Assert
        assertThat(actualResult.size).isEqualTo(2)
        assertThat(actualResult.contains(mockVerifiedId1)).isTrue
        assertThat(actualResult.contains(mockVerifiedId2)).isTrue
    }

    @Test
    fun getMatches_ClaimConstraintDoesNotMatchVerifiedId_ReturnsEmptyList() {
        // Arrange
        val expectedVcTypes = listOf("VerifiableCredential", "BusinessCard")
        val mockVerifiedId: VerifiableCredential = mockk()
        val verifiableCredentialContent = VerifiableCredentialContent(
            "urn:pic:71d9f132fa904325a6520e6bc6007c36", VerifiableCredentialDescriptor(
                listOf("https://www.w3.org/2018/credentials/v1"),
                expectedVcTypes,
                mapOf("name" to "n", "company" to "m"),
                null,
                null
            ), "did:ion:testsubject", "TestIssuer", 1686870564, 1689462564
        )
        every { mockVerifiedId.raw.contents } returns verifiableCredentialContent
        verifiedIdRequirement = VerifiedIdRequirement(
            "id",
            expectedVcTypes,
            VcPathRegexConstraint(listOf("$.iss"), "WrongIssuer"),
            encrypted = false,
            required = true,
            "testing purposes",
        )

        // Act
        val actualResult = verifiedIdRequirement.getMatches(listOf(mockVerifiedId))

        // Assert
        assertThat(actualResult.size).isEqualTo(0)
    }

    @Test
    fun getMatches_OneVerifiedIdMatchesClaimConstraint_ReturnsListWithMatchingVc() {
        // Arrange
        val expectedVcTypes = listOf("VerifiableCredential", "BusinessCard")
        val mockVerifiedId: VerifiableCredential = mockk()
        val verifiableCredentialContent = VerifiableCredentialContent(
            "urn:pic:71d9f132fa904325a6520e6bc6007c36", VerifiableCredentialDescriptor(
                listOf("https://www.w3.org/2018/credentials/v1"),
                expectedVcTypes,
                mapOf("name" to "n", "company" to "m"),
                null,
                null
            ), "did:ion:testsubject", "TestIssuer", 1686870564, 1689462564
        )
        every { mockVerifiedId.raw.contents } returns verifiableCredentialContent
        verifiedIdRequirement = VerifiedIdRequirement(
            "id",
            expectedVcTypes,
            VcPathRegexConstraint(listOf("$.iss"), "TestIssuer"),
            encrypted = false,
            required = true,
            "testing purposes",
        )

        // Act
        val actualResult = verifiedIdRequirement.getMatches(listOf(mockVerifiedId))

        // Assert
        assertThat(actualResult.size).isEqualTo(1)
        assertThat(actualResult.first()).isEqualTo(mockVerifiedId)
    }

    @Test
    fun getMatches_MultipleVerifiedIdsMatchClaimConstraint_ReturnsListOfMatchingVcs() {
        // Arrange
        val expectedVcTypes = listOf("VerifiableCredential", "BusinessCard")
        val mockVerifiedId1: VerifiableCredential = mockk()
        val verifiableCredentialContent1 = VerifiableCredentialContent(
            "urn:pic:71d9f132fa904325a6520e6bc6007c36", VerifiableCredentialDescriptor(
                listOf("https://www.w3.org/2018/credentials/v1"),
                expectedVcTypes,
                mapOf("name" to "n", "company" to "m"),
                null,
                null
            ), "did:ion:testsubject", "TestIssuer", 1686870564, 1689462564
        )
        every { mockVerifiedId1.raw.contents } returns verifiableCredentialContent1
        val mockVerifiedId2: VerifiableCredential = mockk()
        val verifiableCredentialContent2 = VerifiableCredentialContent(
            "urn:pic:71d9f132fa904325a6520e6bc6007c36", VerifiableCredentialDescriptor(
                listOf("https://www.w3.org/2018/credentials/v1"),
                expectedVcTypes,
                mapOf("name" to "n", "company" to "m"),
                null,
                null
            ), "did:ion:testsubject", "TestIssuer", 1686870564, 1689462564
        )
        every { mockVerifiedId2.raw.contents } returns verifiableCredentialContent2
        verifiedIdRequirement = VerifiedIdRequirement(
            "id",
            expectedVcTypes,
            VcPathRegexConstraint(listOf("$.iss"), "TestIssuer"),
            encrypted = false,
            required = true,
            "testing purposes",
        )

        // Act
        val actualResult =
            verifiedIdRequirement.getMatches(listOf(mockVerifiedId1, mockVerifiedId2))

        // Assert
        assertThat(actualResult.size).isEqualTo(2)
        assertThat(actualResult.contains(mockVerifiedId1)).isTrue
        assertThat(actualResult.contains(mockVerifiedId2)).isTrue
    }

    @Test
    fun getMatches_MatchingVcTypeConstraintAndNonMatchingClaimConstraint_ReturnsEmptyList() {
        // Arrange
        val expectedVcTypes = listOf("VerifiableCredential", "BusinessCard")
        val mockVerifiedId: VerifiableCredential = mockk()
        val verifiableCredentialContent = VerifiableCredentialContent(
            "urn:pic:71d9f132fa904325a6520e6bc6007c36", VerifiableCredentialDescriptor(
                listOf("https://www.w3.org/2018/credentials/v1"),
                expectedVcTypes,
                mapOf("name" to "n", "company" to "m"),
                null,
                null
            ), "did:ion:testsubject", "TestIssuer", 1686870564, 1689462564
        )
        every { mockVerifiedId.raw.contents } returns verifiableCredentialContent
        every { mockVerifiedId.types } returns expectedVcTypes
        verifiedIdRequirement = VerifiedIdRequirement(
            "id",
            expectedVcTypes,
            GroupConstraint(
                listOf(
                    VcPathRegexConstraint(listOf("$.iss"), "WrongIssuer"),
                    VcTypeConstraint("BusinessCard")
                ), GroupConstraintOperator.ALL
            ),
            encrypted = false,
            required = true,
            "testing purposes",
        )

        // Act
        val actualResult = verifiedIdRequirement.getMatches(listOf(mockVerifiedId))

        // Assert
        assertThat(actualResult.size).isEqualTo(0)
    }

    @Test
    fun getMatches_MatchingClaimConstraintAndNonMatchingVcTypeConstraint_ReturnsEmptyList() {
        // Arrange
        val expectedVcTypes = listOf("VerifiableCredential", "BusinessCard")
        val mockVerifiedId: VerifiableCredential = mockk()
        val verifiableCredentialContent = VerifiableCredentialContent(
            "urn:pic:71d9f132fa904325a6520e6bc6007c36", VerifiableCredentialDescriptor(
                listOf("https://www.w3.org/2018/credentials/v1"),
                expectedVcTypes,
                mapOf("name" to "n", "company" to "m"),
                null,
                null
            ), "did:ion:testsubject", "TestIssuer", 1686870564, 1689462564
        )
        every { mockVerifiedId.raw.contents } returns verifiableCredentialContent
        every { mockVerifiedId.types } returns expectedVcTypes
        verifiedIdRequirement = VerifiedIdRequirement(
            "id",
            expectedVcTypes,
            GroupConstraint(
                listOf(
                    VcPathRegexConstraint(listOf("$.iss"), "TestIssuer"),
                    VcTypeConstraint("TestCredential")
                ), GroupConstraintOperator.ALL
            ),
            encrypted = false,
            required = true,
            "testing purposes",
        )

        // Act
        val actualResult = verifiedIdRequirement.getMatches(listOf(mockVerifiedId))

        // Assert
        assertThat(actualResult.size).isEqualTo(0)
    }

    @Test
    fun getMatches_MatchingClaimConstraintAndMatchingVcTypeConstraint_ReturnsMatchingVc() {
        // Arrange
        val expectedVcTypes = listOf("VerifiableCredential", "BusinessCard")
        val mockVerifiedId: VerifiableCredential = mockk()
        val verifiableCredentialContent = VerifiableCredentialContent(
            "urn:pic:71d9f132fa904325a6520e6bc6007c36", VerifiableCredentialDescriptor(
                listOf("https://www.w3.org/2018/credentials/v1"),
                expectedVcTypes,
                mapOf("name" to "n", "company" to "m"),
                null,
                null
            ), "did:ion:testsubject", "TestIssuer", 1686870564, 1689462564
        )
        every { mockVerifiedId.raw.contents } returns verifiableCredentialContent
        every { mockVerifiedId.types } returns expectedVcTypes
        verifiedIdRequirement = VerifiedIdRequirement(
            "id",
            expectedVcTypes,
            GroupConstraint(
                listOf(
                    VcPathRegexConstraint(listOf("$.iss"), "TestIssuer"),
                    VcTypeConstraint("BusinessCard")
                ), GroupConstraintOperator.ALL
            ),
            encrypted = false,
            required = true,
            "testing purposes",
        )

        // Act
        val actualResult = verifiedIdRequirement.getMatches(listOf(mockVerifiedId))

        // Assert
        assertThat(actualResult.size).isEqualTo(1)
    }

    @Test
    fun getMatches_MatchingMultipleClaimConstraintsAndMatchingVcTypeConstraint_ReturnsMatchingVc() {
        // Arrange
        val expectedVcTypes = listOf("VerifiableCredential", "BusinessCard")
        val mockVerifiedId: VerifiableCredential = mockk()
        val verifiableCredentialContent = VerifiableCredentialContent(
            "urn:pic:71d9f132fa904325a6520e6bc6007c36", VerifiableCredentialDescriptor(
                listOf("https://www.w3.org/2018/credentials/v1"),
                expectedVcTypes,
                mapOf("name" to "n", "company" to "m"),
                null,
                null
            ), "did:ion:testsubject", "TestIssuer", 1686870564, 1689462564
        )
        every { mockVerifiedId.raw.contents } returns verifiableCredentialContent
        every { mockVerifiedId.types } returns expectedVcTypes
        verifiedIdRequirement = VerifiedIdRequirement(
            "id",
            expectedVcTypes,
            GroupConstraint(
                listOf(
                    GroupConstraint(
                        listOf(
                            VcPathRegexConstraint(listOf("$.iss"), "TestIssuer"),
                            VcPathRegexConstraint(listOf("$.vc.credentialSubject.name"), "/n/gi")
                        ), GroupConstraintOperator.ALL
                    ),
                    VcTypeConstraint("BusinessCard")
                ), GroupConstraintOperator.ALL
            ),
            encrypted = false,
            required = true,
            "testing purposes",
        )

        // Act
        val actualResult = verifiedIdRequirement.getMatches(listOf(mockVerifiedId))

        // Assert
        assertThat(actualResult.size).isEqualTo(1)
    }

    @Test
    fun getMatches_MatchingClaimConstraintAndMatchingMultipleVcTypeConstraints_ReturnsMatchingVc() {
        // Arrange
        val expectedVcTypes = listOf("VerifiableCredential", "BusinessCard")
        val mockVerifiedId: VerifiableCredential = mockk()
        val verifiableCredentialContent = VerifiableCredentialContent(
            "urn:pic:71d9f132fa904325a6520e6bc6007c36", VerifiableCredentialDescriptor(
                listOf("https://www.w3.org/2018/credentials/v1"),
                expectedVcTypes,
                mapOf("name" to "n", "company" to "m"),
                null,
                null
            ), "did:ion:testsubject", "TestIssuer", 1686870564, 1689462564
        )
        every { mockVerifiedId.raw.contents } returns verifiableCredentialContent
        every { mockVerifiedId.types } returns expectedVcTypes
        verifiedIdRequirement = VerifiedIdRequirement(
            "id",
            expectedVcTypes,
            GroupConstraint(
                listOf(
                    VcPathRegexConstraint(listOf("$.vc.credentialSubject.name"), "/n/gi"),
                    GroupConstraint(
                        listOf(
                            VcTypeConstraint("BusinessCard"),
                            VcTypeConstraint("VerifiableCredential")
                        ),
                        GroupConstraintOperator.ANY
                    )
                ), GroupConstraintOperator.ALL
            ),
            encrypted = false,
            required = true,
            "testing purposes",
        )

        // Act
        val actualResult = verifiedIdRequirement.getMatches(listOf(mockVerifiedId))

        // Assert
        assertThat(actualResult.size).isEqualTo(1)
    }
}