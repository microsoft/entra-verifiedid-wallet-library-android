package com.microsoft.walletlibrary.mappings.presentation

import com.microsoft.did.sdk.credential.service.models.presentationexchange.Constraints
import com.microsoft.did.sdk.credential.service.models.presentationexchange.CredentialPresentationInputDescriptor
import com.microsoft.did.sdk.credential.service.models.presentationexchange.Fields
import com.microsoft.did.sdk.credential.service.models.presentationexchange.Filter
import com.microsoft.did.sdk.credential.service.models.presentationexchange.Schema
import com.microsoft.walletlibrary.requests.requirements.constraints.GroupConstraint
import com.microsoft.walletlibrary.requests.requirements.constraints.GroupConstraintOperator
import com.microsoft.walletlibrary.requests.requirements.constraints.VcPathRegexConstraint
import com.microsoft.walletlibrary.requests.requirements.constraints.VcTypeConstraint
import com.microsoft.walletlibrary.util.MissingVerifiedIdTypeException
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

class CredentialPresentationInputDescriptorsMappingTest {
    private lateinit var credentialPresentationInputDescriptor: CredentialPresentationInputDescriptor
    private val expectedId = "TestId"
    private val expectedInputName = "TestInput"
    private val expectedInputPurpose = "For Testing"
    private val expectedSchema = mockk<Schema>()
    private val expectedRequiredFieldValue = true
    private val expectedEncryptedFieldValue = false

    init {
        setupInput(listOf(expectedSchema))
    }

    private fun setupInput(schemaList: List<Schema>) {
        credentialPresentationInputDescriptor = CredentialPresentationInputDescriptor(
            expectedId,
            schemaList,
            expectedInputName,
            expectedInputPurpose,
            emptyList()
        )
    }

    @Test
    fun inputDescriptorMapping_EmptySchema_ThrowsException() {
        // Arrange
        setupInput(emptyList())

        // Act and Assert
        assertThatThrownBy { credentialPresentationInputDescriptor.toVerifiedIdRequirement() }.isInstanceOf(
            MissingVerifiedIdTypeException::class.java
        )
    }

    @Test
    fun inputDescriptorMapping_WithOneIdType_ReturnsVerifiedIdRequirement() {
        // Arrange
        every { expectedSchema.uri } returns ""

        // Act
        val actualVerifiedIdRequirement =
            credentialPresentationInputDescriptor.toVerifiedIdRequirement()

        // Assert
        assertThat(actualVerifiedIdRequirement.id).isEqualTo(expectedId)
        assertThat(actualVerifiedIdRequirement.purpose).isEqualTo(expectedInputPurpose)
        assertThat(actualVerifiedIdRequirement.types.size).isEqualTo(1)
        assertThat(actualVerifiedIdRequirement.types.first()).isEqualTo(expectedSchema.uri)
        assertThat(actualVerifiedIdRequirement.required).isEqualTo(expectedRequiredFieldValue)
        assertThat(actualVerifiedIdRequirement.encrypted).isEqualTo(expectedEncryptedFieldValue)
    }

    @Test
    fun inputDescriptorMapping_WithMultipleIdTypes_ReturnsVerifiedIdRequirement() {
        // Arrange
        val schema1 = mockk<Schema>()
        val expectedSchema1Uri = "schema1"
        val schema2 = mockk<Schema>()
        val expectedSchema2Uri = "schema2"
        setupInput(listOf(schema1, schema2))
        every { schema1.uri } returns expectedSchema1Uri
        every { schema2.uri } returns expectedSchema2Uri

        // Act
        val actualVerifiedIdRequirement =
            credentialPresentationInputDescriptor.toVerifiedIdRequirement()

        // Assert
        assertThat(actualVerifiedIdRequirement.id).isEqualTo(expectedId)
        assertThat(actualVerifiedIdRequirement.purpose).isEqualTo(expectedInputPurpose)
        assertThat(actualVerifiedIdRequirement.types.size).isEqualTo(2)
        assertThat(actualVerifiedIdRequirement.types[0]).isEqualTo(expectedSchema1Uri)
        assertThat(actualVerifiedIdRequirement.types[1]).isEqualTo(expectedSchema2Uri)
        assertThat(actualVerifiedIdRequirement.required).isEqualTo(expectedRequiredFieldValue)
        assertThat(actualVerifiedIdRequirement.encrypted).isEqualTo(expectedEncryptedFieldValue)
    }

    @Test
    fun inputDescriptorMapping_WithNoContracts_ReturnsVerifiedIdRequirement() {
        // Arrange
        every { expectedSchema.uri } returns ""

        // Act
        val actualVerifiedIdRequirement =
            credentialPresentationInputDescriptor.toVerifiedIdRequirement()

        // Assert
        assertThat(actualVerifiedIdRequirement.id).isEqualTo(expectedId)
        assertThat(actualVerifiedIdRequirement.purpose).isEqualTo(expectedInputPurpose)
        assertThat(actualVerifiedIdRequirement.types.size).isEqualTo(1)
        assertThat(actualVerifiedIdRequirement.required).isEqualTo(expectedRequiredFieldValue)
        assertThat(actualVerifiedIdRequirement.encrypted).isEqualTo(expectedEncryptedFieldValue)
    }

    @Test
    fun inputDescriptorMapping_WithPurpose_ReturnsVerifiedIdRequirement() {
        // Arrange
        every { expectedSchema.uri } returns ""

        // Act
        val actualVerifiedIdRequirement =
            credentialPresentationInputDescriptor.toVerifiedIdRequirement()

        // Assert
        assertThat(actualVerifiedIdRequirement.id).isEqualTo(expectedId)
        assertThat(actualVerifiedIdRequirement.purpose).isEqualTo(expectedInputPurpose)
        assertThat(actualVerifiedIdRequirement.types.size).isEqualTo(1)
        assertThat(actualVerifiedIdRequirement.required).isEqualTo(expectedRequiredFieldValue)
        assertThat(actualVerifiedIdRequirement.encrypted).isEqualTo(expectedEncryptedFieldValue)
    }

    @Test
    fun constraintMapping_WithSingleValidSchemaUri_ReturnsTypeConstraint() {
        // Act
        val expectedVcType = "BusinessCard"
        val actualVcTypeConstraint = toVcTypeConstraint(listOf(expectedVcType))

        // Assert
        assertThat(actualVcTypeConstraint).isInstanceOf(VcTypeConstraint::class.java)
        assertThat((actualVcTypeConstraint as VcTypeConstraint).vcType).isEqualTo(expectedVcType)
    }

    @Test
    fun constraintMapping_WithMultipleValidSchemaUri_ReturnsGroupConstraint() {
        // Arrange
        val expectedVcTypes = listOf("BusinessCard1", "BusinessCard2")

        // Act
        val actualConstraint = toVcTypeConstraint(expectedVcTypes)

        // Assert
        assertThat(actualConstraint).isInstanceOf(GroupConstraint::class.java)
        assertThat((actualConstraint as GroupConstraint).constraints.size).isEqualTo(2)
        assertThat(actualConstraint.constraintOperator).isEqualTo(GroupConstraintOperator.ANY)
        assertThat(actualConstraint.constraints.filterIsInstance<VcTypeConstraint>().size).isEqualTo(
            2
        )
        assertThat(
            actualConstraint.constraints.filterIsInstance<VcTypeConstraint>()
                .map { it.vcType }).containsAll(expectedVcTypes)
    }

    @Test
    fun constraintMapping_WithEmptySchemaUri_ReturnsNull() {
        // Act
        val actualConstraint = toVcTypeConstraint(emptyList())

        // Assert
        assertThat(actualConstraint).isNull()
    }

    @Test
    fun constraintMapping_WithEmptyFields_ReturnsNull() {
        // Act
        val actualConstraint = toVcPathRegexConstraint(emptyList())

        // Assert
        assertThat(actualConstraint).isNull()
    }

    @Test
    fun constraintMapping_WithSingleValidField_ReturnsClaimConstraint() {
        // Arrange
        val expectedPattern = "did:ion:test"
        val filter = Filter("string", expectedPattern)
        val field = Fields(listOf(".iss"))
        field.filter = filter
        // Act
        val actualConstraint = toVcPathRegexConstraint(listOf(field))

        // Assert
        assertThat(actualConstraint).isInstanceOf(VcPathRegexConstraint::class.java)
        assertThat((actualConstraint as VcPathRegexConstraint).pattern).isEqualTo(expectedPattern)
    }

    @Test
    fun constraintMapping_WithMultipleValidFields_ReturnsGroupConstraint() {
        // Arrange
        val expectedPatterns = arrayListOf("did:ion:test1", "did:ion:test2")
        val filter1 = Filter("string", expectedPatterns[0])
        val field1 = Fields(listOf(".iss"))
        field1.filter = filter1

        val filter2 = Filter("string", expectedPatterns[1])
        val field2 = Fields(listOf(".iss"))
        field2.filter = filter2

        // Act
        val actualConstraint = toVcPathRegexConstraint(listOf(field1, field2))

        // Assert
        assertThat(actualConstraint).isInstanceOf(GroupConstraint::class.java)
        assertThat((actualConstraint as GroupConstraint).constraints.size).isEqualTo(2)
        assertThat(actualConstraint.constraintOperator).isEqualTo(GroupConstraintOperator.ALL)
        assertThat(actualConstraint.constraints.filterIsInstance<VcPathRegexConstraint>().size).isEqualTo(
            2
        )
        assertThat(
            actualConstraint.constraints.filterIsInstance<VcPathRegexConstraint>()
                .map { it.pattern }).containsAll(expectedPatterns)
    }

    @Test
    fun constraintMapping_WithNoConstraints_ReturnsNull() {
        // Arrange
        val credentialPresentationInputDescriptor = CredentialPresentationInputDescriptor(
            expectedId,
            emptyList(),
            expectedInputName,
            expectedInputPurpose,
            emptyList()
        )

        // Act
        val actualConstraint = credentialPresentationInputDescriptor.toConstraint()

        // Assert
        assertThat(actualConstraint).isNull()
    }

    @Test
    fun constraintMapping_WithSingleVcTypeConstraintAndNoClaimConstraint_ReturnsVcTypeConstraint() {
        // Arrange
        val expectedSchema = "schema1"
        val credentialPresentationInputDescriptor = CredentialPresentationInputDescriptor(
            expectedId,
            listOf(Schema(expectedSchema)),
            expectedInputName,
            expectedInputPurpose,
            emptyList()
        )

        // Act
        val actualConstraints = credentialPresentationInputDescriptor.toConstraint()

        // Assert
        assertThat(actualConstraints).isInstanceOf(VcTypeConstraint::class.java)
        assertThat((actualConstraints as VcTypeConstraint).vcType).isEqualTo(expectedSchema)
    }

    @Test
    fun constraintMapping_WithSingleClaimConstraintAndNoVcTypeConstraint_ReturnsClaimConstraint() {
        // Arrange
        val expectedPath = ".iss"
        val expectedConstraint = Constraints(listOf(Fields(listOf(expectedPath))))
        val credentialPresentationInputDescriptor = CredentialPresentationInputDescriptor(
            expectedId,
            emptyList(),
            expectedInputName,
            expectedInputPurpose,
            emptyList(),
            expectedConstraint
        )

        // Act
        val actualConstraint = credentialPresentationInputDescriptor.toConstraint()

        // Assert
        assertThat(actualConstraint).isInstanceOf(VcPathRegexConstraint::class.java)
        assertThat((actualConstraint as VcPathRegexConstraint).path).contains(expectedPath)
    }

    @Test
    fun constraintMapping_WithMultipleClaimConstraintsAndNoVcTypeConstraint_ReturnsGroupConstraint() {
        // Arrange
        val expectedPath = ".iss"
        val expectedConstraint =
            Constraints(listOf(Fields(listOf(expectedPath)), Fields(listOf(expectedPath))))
        val credentialPresentationInputDescriptor = CredentialPresentationInputDescriptor(
            expectedId,
            emptyList(),
            expectedInputName,
            expectedInputPurpose,
            emptyList(),
            expectedConstraint
        )

        // Act
        val actualConstraint = credentialPresentationInputDescriptor.toConstraint()

        // Assert
        assertThat(actualConstraint).isInstanceOf(GroupConstraint::class.java)
        assertThat((actualConstraint as GroupConstraint).constraints.size).isEqualTo(2)
        assertThat(actualConstraint.constraintOperator).isEqualTo(GroupConstraintOperator.ALL)
        assertThat(actualConstraint.constraints.filterIsInstance<VcPathRegexConstraint>().size).isEqualTo(
            2
        )
        assertThat(
            actualConstraint.constraints.filterIsInstance<VcPathRegexConstraint>()
                .map { it.path }).contains(listOf(expectedPath))
    }

    @Test
    fun constraintMapping_WithSingleClaimConstraintAndSingleVcTypeConstraint_ReturnsGroupConstraintWithAllOperator() {
        // Arrange
        val expectedPath = ".iss"
        val expectedSchema = "schema1"
        val expectedConstraint = Constraints(listOf(Fields(listOf(expectedPath))))
        val credentialPresentationInputDescriptor = CredentialPresentationInputDescriptor(
            expectedId,
            listOf(Schema(expectedSchema)),
            expectedInputName,
            expectedInputPurpose,
            emptyList(),
            expectedConstraint
        )

        // Act
        val actualConstraint = credentialPresentationInputDescriptor.toConstraint()

        // Assert
        assertThat(actualConstraint).isInstanceOf(GroupConstraint::class.java)
        assertThat((actualConstraint as GroupConstraint).constraintOperator).isEqualTo(
            GroupConstraintOperator.ALL
        )
        assertThat(actualConstraint.constraints.size).isEqualTo(2)
        assertThat(actualConstraint.constraints.filterIsInstance<GroupConstraint>().size).isEqualTo(
            0
        )
    }

    @Test
    fun constraintMapping_WithMultipleClaimConstraintsAndSingleVcTypeConstraint_ReturnsNestedGroupConstraintWithAllOperator() {
        // Arrange
        val expectedPath = ".iss"
        val expectedSchema = "schema1"
        val expectedConstraint =
            Constraints(listOf(Fields(listOf(expectedPath)), Fields(listOf(expectedPath))))
        val credentialPresentationInputDescriptor = CredentialPresentationInputDescriptor(
            expectedId,
            listOf(Schema(expectedSchema)),
            expectedInputName,
            expectedInputPurpose,
            emptyList(),
            expectedConstraint
        )

        // Act
        val actualConstraint = credentialPresentationInputDescriptor.toConstraint()

        // Assert
        assertThat(actualConstraint).isInstanceOf(GroupConstraint::class.java)
        assertThat((actualConstraint as GroupConstraint).constraintOperator).isEqualTo(
            GroupConstraintOperator.ALL
        )
        assertThat(actualConstraint.constraints.size).isEqualTo(2)
        assertThat(actualConstraint.constraints.filterIsInstance<GroupConstraint>().size).isEqualTo(
            1
        )
        assertThat(
            actualConstraint.constraints.filterIsInstance<GroupConstraint>()
                .first().constraintOperator
        ).isEqualTo(
            GroupConstraintOperator.ALL
        )
        assertThat(actualConstraint.constraints.filterIsInstance<VcTypeConstraint>().size).isEqualTo(
            1
        )
    }

    @Test
    fun constraintMapping_WithSingleClaimConstraintAndMultipleVcTypeConstraints_ReturnsNestedGroupConstraintWithAnyOperator() {
        // Arrange
        val expectedPath = ".iss"
        val expectedSchema = "schema1"
        val expectedConstraint = Constraints(listOf(Fields(listOf(expectedPath))))
        val credentialPresentationInputDescriptor = CredentialPresentationInputDescriptor(
            expectedId,
            listOf(Schema(expectedSchema), Schema(expectedSchema)),
            expectedInputName,
            expectedInputPurpose,
            emptyList(),
            expectedConstraint
        )

        // Act
        val actualConstraint = credentialPresentationInputDescriptor.toConstraint()

        // Assert
        assertThat(actualConstraint).isInstanceOf(GroupConstraint::class.java)
        assertThat((actualConstraint as GroupConstraint).constraintOperator).isEqualTo(
            GroupConstraintOperator.ALL
        )
        assertThat(actualConstraint.constraints.size).isEqualTo(2)
        assertThat(actualConstraint.constraints.filterIsInstance<GroupConstraint>().size).isEqualTo(
            1
        )
        assertThat(
            actualConstraint.constraints.filterIsInstance<GroupConstraint>()
                .first().constraintOperator
        ).isEqualTo(
            GroupConstraintOperator.ANY
        )
        assertThat(actualConstraint.constraints.filterIsInstance<VcPathRegexConstraint>().size).isEqualTo(
            1
        )
    }

    @Test
    fun constraintMapping_WithMultipleClaimConstraintsAndMultipleVcTypeConstraints_ReturnsNestedGroupConstraintsWithAllAndAnyOperator() {
        // Arrange
        val expectedPath = ".iss"
        val expectedSchema = "schema1"
        val expectedConstraint =
            Constraints(listOf(Fields(listOf(expectedPath)), Fields(listOf(expectedPath))))
        val credentialPresentationInputDescriptor = CredentialPresentationInputDescriptor(
            expectedId,
            listOf(Schema(expectedSchema), Schema(expectedSchema)),
            expectedInputName,
            expectedInputPurpose,
            emptyList(),
            expectedConstraint
        )

        // Act
        val actualConstraint = credentialPresentationInputDescriptor.toConstraint()

        // Assert
        assertThat(actualConstraint).isInstanceOf(GroupConstraint::class.java)
        assertThat((actualConstraint as GroupConstraint).constraintOperator).isEqualTo(
            GroupConstraintOperator.ALL
        )
        assertThat(actualConstraint.constraints.size).isEqualTo(2)
        assertThat(actualConstraint.constraints.filterIsInstance<GroupConstraint>().size).isEqualTo(
            2
        )
        assertThat(
            actualConstraint.constraints.filterIsInstance<GroupConstraint>()
                .first().constraintOperator
        ).isEqualTo(
            GroupConstraintOperator.ANY
        )
        assertThat(
            actualConstraint.constraints.filterIsInstance<GroupConstraint>()
                .last().constraintOperator
        ).isEqualTo(
            GroupConstraintOperator.ALL
        )
    }
}