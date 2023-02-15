package com.microsoft.walletlibrary.mappings

import com.microsoft.did.sdk.credential.service.PresentationRequest
import com.microsoft.did.sdk.credential.service.models.linkedDomains.LinkedDomainVerified
import com.microsoft.did.sdk.credential.service.models.oidc.PresentationRequestContent
import com.microsoft.did.sdk.credential.service.models.oidc.Registration
import com.microsoft.did.sdk.credential.service.models.presentationexchange.CredentialPresentationInputDescriptor
import com.microsoft.did.sdk.credential.service.models.presentationexchange.PresentationDefinition
import com.microsoft.did.sdk.credential.service.models.presentationexchange.Schema
import com.microsoft.walletlibrary.requests.requirements.GroupRequirement
import com.microsoft.walletlibrary.requests.requirements.GroupRequirementOperator
import com.microsoft.walletlibrary.requests.requirements.VerifiedIdRequirement
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
    fun presentationRequestMapping_OneRequirement_ReturnsVerifiedIdRequirementInOpenIdRequest() {
        // Act
        val actualOpenIdRequest = presentationRequest.toOpenIdPresentationRequest()

        // Assert
        assertThat(actualOpenIdRequest.requirement)
            .isInstanceOf(VerifiedIdRequirement::class.java)
        assertThat(actualOpenIdRequest.requirement.required).isEqualTo(true)
        assertThat(actualOpenIdRequest.requesterStyle.requester).isEqualTo(expectedEntityName)
    }

    @Test
    fun presentationRequestMapping_MultipleRequirements_ReturnsGroupRequirementInOpenIdRequest() {
        // Arrange
        val inputDescriptor2: CredentialPresentationInputDescriptor = mockk()
        setupInput(listOf(inputDescriptor, inputDescriptor2), true)
        // Act
        val actualOpenIdRequest = presentationRequest.toOpenIdPresentationRequest()

        // Assert
        assertThat(actualOpenIdRequest.requirement).isInstanceOf(GroupRequirement::class.java)
        assertThat((actualOpenIdRequest.requirement as GroupRequirement).requirementOperator).isEqualTo(
            GroupRequirementOperator.ANY
        )
        assertThat(actualOpenIdRequest.requirement.required).isEqualTo(true)
        assertThat(actualOpenIdRequest.requesterStyle.requester).isEqualTo(expectedEntityName)
    }

    @Test
    fun presentationRequestMapping_NoLogoImageInRequesterStyle_ReturnsSuccessfulRequestWithNoLogoImage() {
        // Arrange
        setupInput(listOf(inputDescriptor), false)

        // Act
        val actualOpenIdRequest = presentationRequest.toOpenIdPresentationRequest()

        // Assert
        assertThat((actualOpenIdRequest.requesterStyle as OpenIdRequesterStyle).logo).isNotNull
        assertThat((actualOpenIdRequest.requesterStyle as OpenIdRequesterStyle).logo?.image).isNull()
    }

    @Test
    fun presentationRequestMapping_LogoPresentInRequesterStyle_ReturnsSuccessfulRequestWithLogo() {
        // Arrange
        setupInput(listOf(inputDescriptor), true)

        // Act
        val actualOpenIdRequest = presentationRequest.toOpenIdPresentationRequest()

        // Assert
        assertThat((actualOpenIdRequest.requesterStyle as OpenIdRequesterStyle).logo).isNotNull
        assertThat((actualOpenIdRequest.requesterStyle as OpenIdRequesterStyle).logo?.uri).isEqualTo(expectedLogoUri)
        assertThat((actualOpenIdRequest.requesterStyle as OpenIdRequesterStyle).logo?.image).isEqualTo(expectedLogoImage)
    }

    @Test
    fun presentationRequestMapping_mapRootOfTrust_ReturnsRequest() {
        // Arrange
        setupInput(listOf(inputDescriptor), true)

        // Act
        val actualOpenIdRequest = presentationRequest.toOpenIdPresentationRequest()

        // Assert
        assertThat(actualOpenIdRequest.rootOfTrust.verified).isEqualTo(true)
        assertThat(actualOpenIdRequest.rootOfTrust.source).isEqualTo(expectedLinkedDomainSource)
    }

    @Test
    fun presentationRequestMapping_mapRequesterStyle_ReturnsRequesterStyle() {
        // Act
        val actualRequesterStyle = presentationRequest.toRequesterStyle()

        // Assert
        assertThat(actualRequesterStyle.requester).isEqualTo(expectedEntityName)
        assertThat(actualRequesterStyle.logo).isNotNull
        assertThat(actualRequesterStyle.locale).isEqualTo("")
    }
}