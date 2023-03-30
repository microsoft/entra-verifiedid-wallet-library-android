package com.microsoft.walletlibrary.mappings.presentation

import com.microsoft.did.sdk.credential.service.PresentationRequest
import com.microsoft.did.sdk.credential.service.models.linkedDomains.LinkedDomainVerified
import com.microsoft.did.sdk.credential.service.models.oidc.PresentationRequestContent
import com.microsoft.did.sdk.credential.service.models.oidc.Registration
import com.microsoft.did.sdk.credential.service.models.presentationexchange.CredentialPresentationInputDescriptor
import com.microsoft.did.sdk.credential.service.models.presentationexchange.PresentationDefinition
import com.microsoft.did.sdk.credential.service.models.presentationexchange.Schema
import com.microsoft.walletlibrary.requests.styles.OpenIdVerifierStyle
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

    init {
        setupInput(listOf(inputDescriptor))
    }

    private fun setupInput(inputDescriptors: List<CredentialPresentationInputDescriptor>) {
        presentationRequest = mockk()
        every { presentationRequest.content } returns presentationRequestContent
        every { presentationRequest.getPresentationDefinition() } returns presentationDefinition
        every { presentationRequest.entityName } returns expectedEntityName
        every { presentationRequest.linkedDomainResult } returns LinkedDomainVerified(
            expectedLinkedDomainSource
        )
        setupPresentationContent()
        setupInputDescriptors(inputDescriptors)
        setupLogo()
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

    private fun setupLogo() {
        every { presentationRequest.content.registration.logoUri } returns expectedLogoUri
    }

    @Test
    fun presentationRequestMapping_MapRequesterStyle_ReturnsOpenIdRequesterStyle() {
        // Act
        val actualRequesterStyle = presentationRequest.getRequesterStyle()

        // Assert
        assertThat(actualRequesterStyle).isInstanceOf(OpenIdVerifierStyle::class.java)
        assertThat(actualRequesterStyle.requester).isEqualTo(expectedEntityName)
        assertThat(actualRequesterStyle).isInstanceOf(OpenIdVerifierStyle::class.java)
        assertThat((actualRequesterStyle as OpenIdVerifierStyle).verifierLogo).isNotNull
        assertThat(actualRequesterStyle.verifierLogo?.uri).isEqualTo(expectedLogoUri)
    }
}