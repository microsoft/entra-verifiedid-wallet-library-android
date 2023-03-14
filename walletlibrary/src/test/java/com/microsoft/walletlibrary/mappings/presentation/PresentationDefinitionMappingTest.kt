package com.microsoft.walletlibrary.mappings.presentation

import com.microsoft.did.sdk.credential.service.models.presentationexchange.CredentialPresentationInputDescriptor
import com.microsoft.did.sdk.credential.service.models.presentationexchange.PresentationDefinition
import com.microsoft.did.sdk.credential.service.models.presentationexchange.Schema
import com.microsoft.walletlibrary.requests.requirements.GroupRequirement
import com.microsoft.walletlibrary.requests.requirements.GroupRequirementOperator
import com.microsoft.walletlibrary.requests.requirements.VerifiedIdRequirement
import com.microsoft.walletlibrary.util.MissingInputDescriptorException
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

class PresentationDefinitionMappingTest {
    private lateinit var presentationDefinition: PresentationDefinition
    private val expectedId = "TestDefinitionId"
    private val expectedInputDescriptor = mockk<CredentialPresentationInputDescriptor>()

    init {
        setupInput(listOf(expectedInputDescriptor))
    }

    private fun setupInput(expectedInputDescriptors: List<CredentialPresentationInputDescriptor>) {
        for (inputDescriptor in expectedInputDescriptors) {
            val expectedSchema = mockk<Schema>()
            every { inputDescriptor.schemas } returns listOf(expectedSchema)
            every { expectedSchema.uri } returns ""
            every { inputDescriptor.id } returns ""
            every { inputDescriptor.purpose } returns ""
            every { inputDescriptor.issuanceMetadataList } returns emptyList()
        }
        presentationDefinition = PresentationDefinition(expectedId, expectedInputDescriptors)
    }

    @Test
    fun presentationDefinitionMapping_WithOneInputDescriptor_ReturnsVerifiedIdRequirement() {
        // Act
        val actualRequirement = presentationDefinition.toRequirement()

        // Assert
        assertThat(actualRequirement).isInstanceOf(VerifiedIdRequirement::class.java)
        assertThat(actualRequirement.required).isEqualTo(true)
    }

    @Test
    fun presentationDefinitionMapping_WithMultipleInputDescriptors_ReturnsGroupRequirement() {
        // Arrange
        val expectedInputDescriptor1 = mockk<CredentialPresentationInputDescriptor>()
        val expectedInputDescriptor2 = mockk<CredentialPresentationInputDescriptor>()
        setupInput(listOf(expectedInputDescriptor1, expectedInputDescriptor2))

        // Act
        val actualRequirement = presentationDefinition.toRequirement()

        // Assert
        assertThat(actualRequirement).isInstanceOf(GroupRequirement::class.java)
        assertThat(actualRequirement.required).isEqualTo(true)
        assertThat((actualRequirement as GroupRequirement).requirementOperator).isEqualTo(GroupRequirementOperator.ANY)
    }

    @Test
    fun presentationDefinitionMapping_WithNoInputDescriptors_ThrowsException() {
        // Arrange
        setupInput(emptyList())

        // Act and Assert
        assertThatThrownBy {
            presentationDefinition.toRequirement()
        }.isInstanceOf(MissingInputDescriptorException::class.java)
    }
}