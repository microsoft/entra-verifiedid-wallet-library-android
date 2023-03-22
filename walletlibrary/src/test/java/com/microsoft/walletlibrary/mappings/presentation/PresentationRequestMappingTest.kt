package com.microsoft.walletlibrary.mappings.presentation

import com.microsoft.did.sdk.credential.service.PresentationRequest
import com.microsoft.did.sdk.credential.service.models.linkedDomains.LinkedDomainVerified
import com.microsoft.did.sdk.credential.service.models.oidc.PresentationRequestContent
import com.microsoft.did.sdk.credential.service.models.oidc.Registration
import com.microsoft.did.sdk.credential.service.models.presentationexchange.CredentialPresentationInputDescriptor
import com.microsoft.did.sdk.credential.service.models.presentationexchange.PresentationDefinition
import com.microsoft.did.sdk.credential.service.models.presentationexchange.Schema
import com.microsoft.walletlibrary.requests.styles.OpenIdRequesterStyle
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PresentationRequestMappingTest {
    private lateinit var presentationRequest: PresentationRequest
    private val presentationRequestContent: PresentationRequestContent = mockk()
    private val registration: Registration = mockk()
    private val expectedEntityName = "testIssuer"
    private val expectedLinkedDomainSource = "https://test.com"
    private val presentationDefinition: PresentationDefinition = mockk()
    private val inputDescriptor: CredentialPresentationInputDescriptor = mockk()
    private val expectedInputDescriptorId = "testInputDescriptorId"
    private val expectedPurpose = "testPurpose"
    private val schema: Schema = mockk()
    private val expectedSchemaUri = "testSchemaUri"
    private val expectedLogoUri = "testLogoUri"
    private val expectedLogoImage = "testLogoImage"

    init {
        setupInput(listOf(inputDescriptor), true)
    }

    private fun setupInput(inputDescriptors: List<CredentialPresentationInputDescriptor>, logoPresent: Boolean) {
        presentationRequest = mockk()
        every { presentationRequest.content } returns presentationRequestContent
        every { presentationRequest.getPresentationDefinition() } returns presentationDefinition
        every { presentationRequest.entityName } returns expectedEntityName
        every { presentationRequest.linkedDomainResult } returns LinkedDomainVerified(
            expectedLinkedDomainSource
        )
        setupPresentationContent()
        setupInputDescriptors(inputDescriptors)
        setupLogo(logoPresent)
    }

    private fun setupPresentationContent() {
        every { presentationRequest.content.registration } returns registration
    }

    private fun setupInputDescriptors(inputDescriptors: List<CredentialPresentationInputDescriptor>) {
        every { presentationRequest.getPresentationDefinition().credentialPresentationInputDescriptors } returns inputDescriptors
        for (inputDescriptor in inputDescriptors) {
            every { inputDescriptor.schemas } returns listOf(schema)
            every { inputDescriptor.id } returns expectedInputDescriptorId
            every { inputDescriptor.purpose } returns expectedPurpose
            every { inputDescriptor.issuanceMetadataList } returns emptyList()
            setupSchema()
        }
    }

    private fun setupSchema() {
        every { schema.uri } returns expectedSchemaUri
    }

    private fun setupLogo(logoPresent: Boolean) {
        every { presentationRequest.content.registration.logoUri } returns expectedLogoUri
        if (logoPresent)
            every { presentationRequest.content.registration.logoData } returns expectedLogoImage
        else
            every { presentationRequest.content.registration.logoData } returns null
    }

    @Test
    fun presentationRequestMapping_MapRequesterStyle_ReturnsOpenIdRequesterStyle() {
        // Act
        val actualRequesterStyle = presentationRequest.getRequesterStyle()

        // Assert
        assertThat(actualRequesterStyle).isInstanceOf(OpenIdRequesterStyle::class.java)
        assertThat(actualRequesterStyle.requester).isEqualTo(expectedEntityName)
        assertThat(actualRequesterStyle.logo).isNotNull
        assertThat(actualRequesterStyle.logo?.uri).isEqualTo(expectedLogoUri)
        assertThat(actualRequesterStyle.logo?.image).isEqualTo(expectedLogoImage)
    }
}