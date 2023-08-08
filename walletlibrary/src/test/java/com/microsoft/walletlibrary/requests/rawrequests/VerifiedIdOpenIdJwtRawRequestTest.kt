package com.microsoft.walletlibrary.requests.rawrequests

import com.microsoft.walletlibrary.did.sdk.credential.service.PresentationRequest
import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainVerified
import com.microsoft.walletlibrary.did.sdk.credential.service.models.oidc.PresentationRequestContent
import com.microsoft.walletlibrary.did.sdk.credential.service.models.oidc.Registration
import com.microsoft.walletlibrary.did.sdk.credential.service.models.presentationexchange.CredentialPresentationInputDescriptor
import com.microsoft.walletlibrary.did.sdk.credential.service.models.presentationexchange.PresentationDefinition
import com.microsoft.walletlibrary.did.sdk.credential.service.models.presentationexchange.Schema
import com.microsoft.walletlibrary.requests.requirements.VerifiedIdRequirement
import com.microsoft.walletlibrary.requests.styles.OpenIdVerifierStyle
import com.microsoft.walletlibrary.util.MalformedInputException
import com.microsoft.walletlibrary.util.MissingCallbackUrlException
import com.microsoft.walletlibrary.util.MissingRequestStateException
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions
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
    private val expectedCallbackUrl = "test.com"
    private val expectedRequestState = "test state"

    init {
        setupInput(listOf(mockInputDescriptor), logoPresent = true, isSchemaEmpty = false)
    }

    private fun setupInput(
        inputDescriptors: List<CredentialPresentationInputDescriptor>,
        logoPresent: Boolean,
        isSchemaEmpty: Boolean
    ) {
        every { mockPresentationRequest.content } returns mockPresentationRequestContent
        every { mockPresentationRequest.getPresentationDefinition() } returns mockPresentationDefinition
        every { mockPresentationRequest.entityName } returns expectedEntityName
        every { mockPresentationRequest.linkedDomainResult } returns LinkedDomainVerified(
            expectedLinkedDomainSource
        )
        setupPresentationContent()
        setupInputDescriptors(inputDescriptors, isSchemaEmpty)
        setupLogo(logoPresent)
        every { mockPresentationRequest.content.prompt } returns expectedPromptForIssuance
        verifiedIdOpenIdJwtRawRequest =
            VerifiedIdOpenIdJwtRawRequest(mockPresentationRequest)
    }

    private fun setupPresentationContent() {
        every { mockPresentationRequest.content.registration } returns mockRegistration
        every { mockPresentationRequest.content.idTokenHint } returns null
    }

    private fun setupInputDescriptors(
        inputDescriptors: List<CredentialPresentationInputDescriptor>,
        isSchemaEmpty: Boolean
    ) {
        every { mockPresentationRequest.getPresentationDefinition().credentialPresentationInputDescriptors } returns inputDescriptors
        for (inputDescriptor in inputDescriptors) {
            every { inputDescriptor.id } returns expectedInputDescriptorId
            every { inputDescriptor.purpose } returns expectedPurpose
            every { inputDescriptor.issuanceMetadataList } returns emptyList()
            every { inputDescriptor.constraints } returns null
            setupSchema(inputDescriptor, isSchemaEmpty)
        }
    }

    private fun setupSchema(
        inputDescriptor: CredentialPresentationInputDescriptor,
        isEmpty: Boolean
    ) {
        if (!isEmpty) {
            every { inputDescriptor.schemas } returns listOf(mockSchema)
            every { mockSchema.uri } returns expectedSchemaUri
        } else
            every { inputDescriptor.schemas } returns emptyList()
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
        // Arrange
        every { mockPresentationRequest.content.redirectUrl } returns expectedCallbackUrl
        every { mockPresentationRequest.content.state } returns expectedRequestState

        // Act
        val actualResult = verifiedIdOpenIdJwtRawRequest.mapToPresentationRequestContent()

        // Assert
        assertThat(actualResult).isInstanceOf(com.microsoft.walletlibrary.requests.PresentationRequestContent::class.java)
        assertThat(actualResult.requesterStyle.name).isEqualTo(expectedEntityName)
        assertThat(actualResult.requesterStyle).isInstanceOf(OpenIdVerifierStyle::class.java)
        assertThat((actualResult.requesterStyle as OpenIdVerifierStyle).verifierLogo).isNotNull
        assertThat(actualResult.requesterStyle.verifierLogo?.url).isEqualTo(expectedLogoUri)
        assertThat(actualResult.requirement).isInstanceOf(VerifiedIdRequirement::class.java)
        assertThat(actualResult.rootOfTrust.verified).isEqualTo(true)
        assertThat(actualResult.rootOfTrust.source).isEqualTo(expectedLinkedDomainSource)
    }

    @Test
    fun mapOpenIdJwtRawRequest_mapPresentationRequestWithNoLogoToRequestContent_ReturnsRequestContent() {
        // Arrange
        setupInput(listOf(mockInputDescriptor), logoPresent = false, isSchemaEmpty = false)
        every { mockPresentationRequest.content.redirectUrl } returns expectedCallbackUrl
        every { mockPresentationRequest.content.state } returns expectedRequestState

        // Act
        val actualResult = verifiedIdOpenIdJwtRawRequest.mapToPresentationRequestContent()

        // Assert
        assertThat(actualResult).isInstanceOf(com.microsoft.walletlibrary.requests.PresentationRequestContent::class.java)
        assertThat(actualResult.requesterStyle.name).isEqualTo(expectedEntityName)
        assertThat(actualResult.requesterStyle).isInstanceOf(OpenIdVerifierStyle::class.java)
        assertThat((actualResult.requesterStyle as OpenIdVerifierStyle).verifierLogo).isNotNull
        assertThat(actualResult.requesterStyle.verifierLogo?.url).isEqualTo("")
        assertThat(actualResult.requirement).isInstanceOf(VerifiedIdRequirement::class.java)
        assertThat(actualResult.rootOfTrust.verified).isEqualTo(true)
        assertThat(actualResult.rootOfTrust.source).isEqualTo(expectedLinkedDomainSource)
    }

    @Test
    fun mapOpenIdJwtRawRequest_mapPresentationRequestWithNoSchemaToRequestContent_ThrowsException() {
        // Arrange
        setupInput(listOf(mockInputDescriptor), logoPresent = false, isSchemaEmpty = true)
        every { mockPresentationRequest.content.redirectUrl } returns expectedCallbackUrl
        every { mockPresentationRequest.content.state } returns expectedRequestState

        // Act and Assert
        Assertions.assertThatThrownBy {
            verifiedIdOpenIdJwtRawRequest.mapToPresentationRequestContent()
        }.isInstanceOf(MalformedInputException::class.java)
    }

    @Test
    fun mapOpenIdJwtRawRequest_mapPresentationRequestWithEmptyRequestState_ThrowsException() {
        // Arrange
        every { mockPresentationRequest.content.redirectUrl } returns expectedCallbackUrl
        every { mockPresentationRequest.content.state } returns ""

        // Act and Assert
        Assertions.assertThatThrownBy {
            verifiedIdOpenIdJwtRawRequest.mapToPresentationRequestContent()
        }.isInstanceOf(MissingRequestStateException::class.java)
    }

    @Test
    fun mapOpenIdJwtRawRequest_mapPresentationRequestWithEmptyCallbackUrl_ThrowsException() {
        // Arrange
        every { mockPresentationRequest.content.redirectUrl } returns ""
        every { mockPresentationRequest.content.state } returns expectedRequestState

        // Act and Assert
        Assertions.assertThatThrownBy {
            verifiedIdOpenIdJwtRawRequest.mapToPresentationRequestContent()
        }.isInstanceOf(MissingCallbackUrlException::class.java)
    }

    @Test
    fun mapOpenIdJwtRawRequest_mapPresentationRequestWithValidStateAndUrlToRequestContent_ReturnsRequestContent() {
        // Arrange
        setupInput(listOf(mockInputDescriptor), logoPresent = false, isSchemaEmpty = false)
        every { mockPresentationRequest.content.redirectUrl } returns expectedCallbackUrl
        every { mockPresentationRequest.content.state } returns expectedRequestState

        // Act
        val actualResult = verifiedIdOpenIdJwtRawRequest.mapToPresentationRequestContent()

        // Assert
        assertThat(actualResult).isInstanceOf(com.microsoft.walletlibrary.requests.PresentationRequestContent::class.java)
        assertThat(actualResult.requesterStyle.name).isEqualTo(expectedEntityName)
        assertThat(actualResult.requesterStyle).isInstanceOf(OpenIdVerifierStyle::class.java)
        assertThat((actualResult.requesterStyle as OpenIdVerifierStyle).verifierLogo).isNotNull
        assertThat(actualResult.requesterStyle.verifierLogo?.url).isEqualTo("")
        assertThat(actualResult.requirement).isInstanceOf(VerifiedIdRequirement::class.java)
        assertThat(actualResult.rootOfTrust.verified).isEqualTo(true)
        assertThat(actualResult.rootOfTrust.source).isEqualTo(expectedLinkedDomainSource)
        assertThat(actualResult.requestState).isEqualTo(expectedRequestState)
        assertThat(actualResult.issuanceCallbackUrl).isNotNull
        assertThat(actualResult.issuanceCallbackUrl).isEqualTo(expectedCallbackUrl)
    }
}