package com.microsoft.walletlibrary.requests.handlers

import android.net.Uri
import com.microsoft.did.sdk.credential.service.IssuanceRequest
import com.microsoft.walletlibrary.requests.ManifestIssuanceRequest
import com.microsoft.walletlibrary.requests.OpenIdPresentationRequest
import com.microsoft.walletlibrary.requests.RootOfTrust
import com.microsoft.walletlibrary.requests.VerifiedIdRequestContent
import com.microsoft.walletlibrary.requests.input.VerifiedIdRequestURL
import com.microsoft.walletlibrary.requests.rawrequests.RawManifest
import com.microsoft.walletlibrary.requests.rawrequests.RawRequest
import com.microsoft.walletlibrary.requests.rawrequests.RequestType
import com.microsoft.walletlibrary.requests.rawrequests.VerifiedIdOpenIdJwtRawRequest
import com.microsoft.walletlibrary.requests.requirements.Requirement
import com.microsoft.walletlibrary.requests.requirements.SelfAttestedClaimRequirement
import com.microsoft.walletlibrary.requests.requirements.VerifiedIdRequirement
import com.microsoft.walletlibrary.requests.styles.OpenIdRequesterStyle
import com.microsoft.walletlibrary.requests.styles.VerifiedIDStyle
import com.microsoft.walletlibrary.util.RequirementCastingException
import com.microsoft.walletlibrary.util.UnSupportedProtocolException
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class OpenIdRequestHandlerTest {
    private lateinit var openIdRequestHandler: RequestHandler
    private lateinit var mockRawRequest: RawRequest
    private lateinit var verifiedIdOpenIdJwtRawRequest: VerifiedIdOpenIdJwtRawRequest
    private val expectedRootOfTrustSource = "test.com"
    private val expectedRequesterName = "Test"
    private val expectedRequirementClaimName = "name"
    private val verifiedIdRequestContent: VerifiedIdRequestContent = mockk()
    private val requesterStyle = OpenIdRequesterStyle(expectedRequesterName, "")
    private val rootOfTrust = RootOfTrust(expectedRootOfTrustSource, true)
    private val selfAttestedClaimRequirement = SelfAttestedClaimRequirement(
        "",
        expectedRequirementClaimName,
        encrypted = false,
        required = false
    )
    private val verifiedIdRequirement: VerifiedIdRequirement = mockk()
    private val issuanceRequest: IssuanceRequest = mockk()
    private val manifestIssuanceRequest: ManifestIssuanceRequest = mockk()
    private val verifiedIdStyle: VerifiedIDStyle = mockk()

    init {
        setupInput(RequestType.PRESENTATION, selfAttestedClaimRequirement)
    }

    private fun setupInput(requestType: RequestType,requirement: Requirement) {
        openIdRequestHandler = spyk(OpenIdRequestHandler(), recordPrivateCalls = true)

        verifiedIdOpenIdJwtRawRequest = mockk()
        if (requestType == RequestType.PRESENTATION) {
            every { verifiedIdOpenIdJwtRawRequest.requestType } returns RequestType.PRESENTATION
            every { verifiedIdOpenIdJwtRawRequest.mapToRequestContent() } returns verifiedIdRequestContent
            mockRequestContent(selfAttestedClaimRequirement)
        } else if (requestType == RequestType.ISSUANCE) {
            mockForIssuanceType(requirement)
        }
    }

    private fun createMockRawRequest() {
        class MockRawRequest(override val requestType: RequestType, override val rawRequest: Any) :
            RawRequest
        mockRawRequest = MockRawRequest(RequestType.ISSUANCE, openIdRequestHandler)
    }

    private fun mockForIssuanceType(verifiedIdRequirement: Requirement) {
        every { verifiedIdOpenIdJwtRawRequest.requestType } returns RequestType.ISSUANCE
        every { verifiedIdOpenIdJwtRawRequest.mapToRequestContent() } returns verifiedIdRequestContent
        mockRequestContent(verifiedIdRequirement)
        mockRequestInput()
        mockManifestIssuanceRequest()
    }

    private fun mockRequestContent(requirement: Requirement) {
        every { verifiedIdRequestContent.requesterStyle } returns requesterStyle
        every { verifiedIdRequestContent.requirement } returns requirement
        every { verifiedIdRequestContent.rootOfTrust } returns rootOfTrust
    }

    private fun mockRequestInput() {
        val verifiedIdRequestURL: VerifiedIdRequestURL = mockk()
        val issuanceOptions = listOf(verifiedIdRequestURL)
        val contractUri: Uri = mockk()
        val contractString = ""
        every { contractUri.toString() } returns contractString
        every { verifiedIdRequirement.issuanceOptions } returns issuanceOptions
        every { verifiedIdRequestURL.url } returns contractUri
        every { openIdRequestHandler["getIssuanceRequest"](contractString) } returns manifestIssuanceRequest
    }

    private fun mockManifestIssuanceRequest() {
        val rawManifest = RawManifest(issuanceRequest)
        every { manifestIssuanceRequest.request } returns rawManifest
        every { manifestIssuanceRequest.requesterStyle } returns requesterStyle
        every { manifestIssuanceRequest.rootOfTrust } returns  rootOfTrust
        every { manifestIssuanceRequest.requirement } returns selfAttestedClaimRequirement
        every { manifestIssuanceRequest.verifiedIdStyle } returns verifiedIdStyle
    }

    @Test
    fun handleRequest_PassUnSupportedRequest_ThrowsException() {
        // Arrange
        createMockRawRequest()
        // Act and Assert
        Assertions.assertThatThrownBy {
            runBlocking {
                openIdRequestHandler.handleRequest(mockRawRequest)
            }
        }.isInstanceOf(UnSupportedProtocolException::class.java)
    }

    @Test
    fun handleRequest_PassOpenIdRawRequestWithTypePresentation_ReturnsOpenIdPresentationRequest() {
        // Act
        val request =
            runBlocking { openIdRequestHandler.handleRequest(verifiedIdOpenIdJwtRawRequest) }

        // Assert
        assertThat(request).isInstanceOf(OpenIdPresentationRequest::class.java)
        assertThat(request.rootOfTrust.verified).isEqualTo(true)
        assertThat(request.rootOfTrust.source).isEqualTo(expectedRootOfTrustSource)
        assertThat(request.requesterStyle.requester).isEqualTo(expectedRequesterName)
        assertThat(request.requirement).isInstanceOf(SelfAttestedClaimRequirement::class.java)
        assertThat((request.requirement as SelfAttestedClaimRequirement).claim).isEqualTo(
            expectedRequirementClaimName
        )
    }

    @Test
    fun handleRequest_PassOpenIdRawRequestWithTypeIssuance_ReturnsManifestIssuanceRequest() {
        //Arrange
        setupInput(RequestType.ISSUANCE, verifiedIdRequirement)

        // Act
        val request = runBlocking { openIdRequestHandler.handleRequest(verifiedIdOpenIdJwtRawRequest) }

        // Assert
        assertThat(request).isInstanceOf(ManifestIssuanceRequest::class.java)
        assertThat(request.requesterStyle).isEqualTo(requesterStyle)
        assertThat(request.rootOfTrust).isEqualTo(rootOfTrust)
        assertThat(request.requirement).isInstanceOf(SelfAttestedClaimRequirement::class.java)
        assertThat((request.requirement as SelfAttestedClaimRequirement).claim).isEqualTo(expectedRequirementClaimName)
    }

    @Test
    fun handleRequest_PassOpenIdRawRequestWithUnSupportedRequirement_ThrowsException() {
        //Arrange
        setupInput(RequestType.ISSUANCE, selfAttestedClaimRequirement)

        // Act and assert
        Assertions.assertThatThrownBy {
            val request = runBlocking { openIdRequestHandler.handleRequest(verifiedIdOpenIdJwtRawRequest) }
        }.isInstanceOf(RequirementCastingException::class.java)
    }
}