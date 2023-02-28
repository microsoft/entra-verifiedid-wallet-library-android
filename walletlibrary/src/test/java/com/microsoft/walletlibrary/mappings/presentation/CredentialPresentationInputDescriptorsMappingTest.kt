package com.microsoft.walletlibrary.mappings.presentation

import com.microsoft.did.sdk.credential.service.models.presentationexchange.CredentialPresentationInputDescriptor
import com.microsoft.did.sdk.credential.service.models.presentationexchange.Schema
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
        setupInput(listOf( expectedSchema))
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
    fun inputDescriptorMapping_EmptySchema_ThrowsException  () {
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
}