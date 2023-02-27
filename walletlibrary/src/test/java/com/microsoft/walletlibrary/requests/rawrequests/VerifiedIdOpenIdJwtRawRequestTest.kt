package com.microsoft.walletlibrary.requests.rawrequests

import com.microsoft.did.sdk.credential.service.PresentationRequest
import com.microsoft.did.sdk.credential.service.models.linkedDomains.LinkedDomainVerified
import com.microsoft.did.sdk.credential.service.models.oidc.PresentationRequestContent
import com.microsoft.did.sdk.credential.service.models.oidc.Registration
import com.microsoft.did.sdk.credential.service.models.presentationexchange.CredentialPresentationInputDescriptor
import com.microsoft.did.sdk.credential.service.models.presentationexchange.PresentationDefinition
import com.microsoft.did.sdk.credential.service.models.presentationexchange.Schema
import com.microsoft.walletlibrary.requests.VerifiedIdRequestContent
import com.microsoft.walletlibrary.requests.requirements.VerifiedIdRequirement
import com.microsoft.walletlibrary.requests.styles.OpenIdRequesterStyle
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class VerifiedIdOpenIdJwtRawRequestTest {
    private lateinit var verifiedIdOpenIdJwtRawRequest: VerifiedIdOpenIdJwtRawRequest
    private val mockPresentationRequest: PresentationRequest = mockk()
    private val mockPresentationRequestContent: PresentationRequestContent = mockk()
    private val mockRegistration: Registration = mockk()
    private val expectedEntityName = "testIssuer"
    private val expectedLinkedDomainSource = "https://test.com"
    private val mockPresentationDefinition: PresentationDefinition = mockk()
    private val mockInputDescriptor: CredentialPresentationInputDescriptor = mockk()
    private val expectedInputDescriptorId = "testInputDescriptorId"
    private val expectedPurpose = "testPurpose"
    private val mockSchema: Schema = mockk()
    private val expectedSchemaUri = "testSchemaUri"
    private val expectedLogoUri = "testLogoUri"
    private val expectedLogoImage = "testLogoImage"
    private val expectedPromptForIssuance = "create"

    init {
        setupInput(listOf(mockInputDescriptor), true)
    }

    private fun setupInput(
        inputDescriptors: List<CredentialPresentationInputDescriptor>,
        logoPresent: Boolean
    ) {
        every { mockPresentationRequest.content } returns mockPresentationRequestContent
        every { mockPresentationRequest.getPresentationDefinition() } returns mockPresentationDefinition
        every { mockPresentationRequest.entityName } returns expectedEntityName
        every { mockPresentationRequest.linkedDomainResult } returns LinkedDomainVerified(
            expectedLinkedDomainSource
        )
        setupPresentationContent()
        setupInputDescriptors(inputDescriptors)
        setupLogo(logoPresent)
        every { mockPresentationRequest.content.prompt } returns expectedPromptForIssuance
        verifiedIdOpenIdJwtRawRequest =
            VerifiedIdOpenIdJwtRawRequest(RequestType.PRESENTATION, mockPresentationRequest)
    }

    private fun setupPresentationContent() {
        every { mockPresentationRequest.content.registration } returns mockRegistration
    }

    private fun setupInputDescriptors(inputDescriptors: List<CredentialPresentationInputDescriptor>) {
        every { mockPresentationRequest.getPresentationDefinition().credentialPresentationInputDescriptors } returns inputDescriptors
        for (inputDescriptor in inputDescriptors) {
            every { inputDescriptor.schemas } returns listOf(mockSchema)
            every { inputDescriptor.id } returns expectedInputDescriptorId
            every { inputDescriptor.purpose } returns expectedPurpose
            every { inputDescriptor.issuanceMetadataList } returns emptyList()
            setupSchema()
        }
    }

    private fun setupSchema() {
        every { mockSchema.uri } returns expectedSchemaUri
    }

    private fun setupLogo(logoPresent: Boolean) {
        if (logoPresent) {
            every { mockPresentationRequest.content.registration.logoData } returns expectedLogoImage
            every { mockPresentationRequest.content.registration.logoUri } returns expectedLogoUri
        } else {
            every { mockPresentationRequest.content.registration.logoData } returns null
            every { mockPresentationRequest.content.registration.logoUri } returns ""
        }
    }

    @Test
    fun mapOpenIdJwtRawRequest_mapPresentationRequestWithLogoToRequestContent_ReturnsRequestContent() {
        // Act
        val actualResult = verifiedIdOpenIdJwtRawRequest.mapToRequestContent()

        // Assert
        assertThat(actualResult).isInstanceOf(VerifiedIdRequestContent::class.java)
        assertThat(actualResult.requesterStyle.requester).isEqualTo(expectedEntityName)
        assertThat(actualResult.requesterStyle).isInstanceOf(OpenIdRequesterStyle::class.java)
        assertThat((actualResult.requesterStyle as OpenIdRequesterStyle).logo).isNotNull
        assertThat(actualResult.requesterStyle.logo?.uri).isEqualTo(expectedLogoUri)
        assertThat(actualResult.requesterStyle.logo?.image).isEqualTo(expectedLogoImage)
        assertThat(actualResult.requirement).isInstanceOf(VerifiedIdRequirement::class.java)
        assertThat(actualResult.rootOfTrust.verified).isEqualTo(true)
        assertThat(actualResult.rootOfTrust.source).isEqualTo(expectedLinkedDomainSource)
    }

    @Test
    fun mapOpenIdJwtRawRequest_mapPresentationRequestWithNoLogoToRequestContent_ReturnsRequestContent() {
        // Arrange
        setupInput(listOf(mockInputDescriptor), false)

        // Act
        val actualResult = verifiedIdOpenIdJwtRawRequest.mapToRequestContent()

        // Assert
        assertThat(actualResult).isInstanceOf(VerifiedIdRequestContent::class.java)
        assertThat(actualResult.requesterStyle.requester).isEqualTo(expectedEntityName)
        assertThat(actualResult.requesterStyle).isInstanceOf(OpenIdRequesterStyle::class.java)
        assertThat((actualResult.requesterStyle as OpenIdRequesterStyle).logo).isNotNull
        assertThat(actualResult.requesterStyle.logo?.uri).isEqualTo("")
        assertThat(actualResult.requesterStyle.logo?.image).isNull()
        assertThat(actualResult.requirement).isInstanceOf(VerifiedIdRequirement::class.java)
        assertThat(actualResult.rootOfTrust.verified).isEqualTo(true)
        assertThat(actualResult.rootOfTrust.source).isEqualTo(expectedLinkedDomainSource)
    }
}