package com.microsoft.walletlibrary.requests.handlers

import android.net.Uri
import com.microsoft.did.sdk.credential.service.IssuanceRequest
import com.microsoft.did.sdk.credential.service.models.attestations.ClaimAttestation
import com.microsoft.did.sdk.credential.service.models.attestations.CredentialAttestations
import com.microsoft.did.sdk.credential.service.models.attestations.SelfIssuedAttestation
import com.microsoft.did.sdk.credential.service.models.contracts.InputContract
import com.microsoft.did.sdk.credential.service.models.contracts.VerifiableCredentialContract
import com.microsoft.did.sdk.credential.service.models.contracts.display.*
import com.microsoft.did.sdk.credential.service.models.linkedDomains.LinkedDomainVerified
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
    private val verifiedIdStyle: VerifiedIDStyle = mockk()

    // Issuance request test variables
    private val issuanceRequest: IssuanceRequest = mockk()
    private val manifestIssuanceRequest: ManifestIssuanceRequest = mockk()
    private val rawManifest = RawManifest(issuanceRequest)
    private val credentialAttestations: CredentialAttestations = mockk()
    private val claimAttestations =
        mutableListOf(ClaimAttestation(expectedRequirementClaimName, true, "string"))
    private lateinit var selfIssuedAttestation: SelfIssuedAttestation

    // Logo test values
    private val expectedLogoUri = "testLogoUri"
    private val expectedLogoImage = "testLogoImage"
    private val expectedLogoDescription = "testLogoDescription"

    // Display contract test values
    private val expectedLocale = ""
    private val expectedBackgroundColor = "#ffffff"
    private val expectedTextColor = "#000000"
    private val expectedCardTitle = "Test Card"
    private val expectedCardDescription = "cardDescription"
    private val expectedCardIssuer = "testIssuer"
    private val expectedConsentTitle = "Consent Title"
    private val expectedConsentInstructions = "Consent Instructions"
    private val inputContract = InputContract(
        "",
        "",
        expectedCardIssuer,
        credentialAttestations
    )
    private lateinit var cardDescriptor: CardDescriptor
    private val consentDescriptor =
        ConsentDescriptor(expectedConsentTitle, expectedConsentInstructions)
    private lateinit var displayContract: DisplayContract
    private lateinit var verifiableCredentialContract: VerifiableCredentialContract

    init {
        setupInput(
            RequestType.PRESENTATION,
            selfAttestedClaimRequirement,
            logoPresent = true,
            emptyClaims = false
        )
    }

    private fun setupInput(
        requestType: RequestType,
        requirement: Requirement,
        logoPresent: Boolean,
        emptyClaims: Boolean
    ) {
        openIdRequestHandler = spyk(OpenIdRequestHandler(), recordPrivateCalls = true)

        verifiedIdOpenIdJwtRawRequest = mockk()
        if (requestType == RequestType.PRESENTATION) {
            every { verifiedIdOpenIdJwtRawRequest.requestType } returns RequestType.PRESENTATION
            every { verifiedIdOpenIdJwtRawRequest.mapToPresentationRequestContent() } returns verifiedIdRequestContent
            mockRequestContent(selfAttestedClaimRequirement)
        } else if (requestType == RequestType.ISSUANCE) {
            mockForIssuanceType(requirement, logoPresent, emptyClaims)
        }
    }

    private fun createMockRawRequest() {
        class MockRawRequest(override val requestType: RequestType, override val rawRequest: Any):
            RawRequest
        mockRawRequest = MockRawRequest(RequestType.ISSUANCE, openIdRequestHandler)
    }

    private fun mockForIssuanceType(
        verifiedIdRequirement: Requirement,
        logoPresent: Boolean,
        emptyClaims: Boolean
    ) {
        every { verifiedIdOpenIdJwtRawRequest.requestType } returns RequestType.ISSUANCE
        every { verifiedIdOpenIdJwtRawRequest.mapToPresentationRequestContent() } returns verifiedIdRequestContent
        mockRequestContent(verifiedIdRequirement)
        mockRequestInput()
        mockManifestIssuanceRequest(logoPresent, emptyClaims)
    }

    private fun mockRequestContent(requirement: Requirement) {
        every { verifiedIdRequestContent.requesterStyle } returns requesterStyle
        every { verifiedIdRequestContent.requirement } returns requirement
        every { verifiedIdRequestContent.rootOfTrust } returns rootOfTrust
        every { verifiedIdRequestContent.injectedIdToken } returns null
    }

    private fun mockRequestInput() {
        val verifiedIdRequestURL: VerifiedIdRequestURL = mockk()
        val issuanceOptions = listOf(verifiedIdRequestURL)
        val contractUri: Uri = mockk()
        val contractString = ""
        every { contractUri.toString() } returns contractString
        every { verifiedIdRequirement.issuanceOptions } returns issuanceOptions
        every { verifiedIdRequestURL.url } returns contractUri
        every { openIdRequestHandler["getIssuanceRequest"](contractString) } returns rawManifest
    }

    private fun mockManifestIssuanceRequest(logoPresent: Boolean, emptyClaims: Boolean) {
        mockIssuanceRequest(logoPresent, emptyClaims)
        every { manifestIssuanceRequest.requesterStyle } returns requesterStyle
        every { manifestIssuanceRequest.rootOfTrust } returns rootOfTrust
        every { manifestIssuanceRequest.requirement } returns selfAttestedClaimRequirement
        every { manifestIssuanceRequest.verifiedIdStyle } returns verifiedIdStyle
    }

    private fun mockIssuanceRequest(logoPresent: Boolean, emptyClaims: Boolean) {
        setupSelfIssuedAttestation()
        val logo = if (logoPresent) Logo(
            expectedLogoUri,
            expectedLogoImage,
            expectedLogoDescription
        ) else null
        cardDescriptor = CardDescriptor(expectedCardTitle, expectedCardIssuer, expectedBackgroundColor, expectedTextColor, logo, expectedCardDescription)
        val claimDescriptor = ClaimDescriptor("type", "label")
        displayContract = if (emptyClaims)
            DisplayContract("", expectedLocale, "", cardDescriptor, consentDescriptor, emptyMap())
        else
            DisplayContract("", expectedLocale, "", cardDescriptor, consentDescriptor, mutableMapOf("" to claimDescriptor))
        verifiableCredentialContract =
            VerifiableCredentialContract("", inputContract, displayContract)
        every { manifestIssuanceRequest.request } returns rawManifest
        every { issuanceRequest.contract } returns verifiableCredentialContract
        every { rawManifest.rawRequest.entityName } returns expectedRequesterName
        every { rawManifest.rawRequest.getAttestations() } returns credentialAttestations
        mockAttestations()
        every { rawManifest.rawRequest.linkedDomainResult } returns LinkedDomainVerified(
            expectedRootOfTrustSource
        )
    }

    private fun mockAttestations() {
        every { credentialAttestations.selfIssued } returns selfIssuedAttestation
        every { credentialAttestations.idTokens } returns emptyList()
        every { credentialAttestations.accessTokens } returns emptyList()
        every { credentialAttestations.presentations } returns emptyList()
    }

    private fun setupSelfIssuedAttestation() {
        selfIssuedAttestation = SelfIssuedAttestation(
            claimAttestations,
            required = false,
            encrypted = false
        )
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
        setupInput(
            RequestType.ISSUANCE,
            verifiedIdRequirement,
            logoPresent = true,
            emptyClaims = false
        )

        // Act
        val request =
            runBlocking { openIdRequestHandler.handleRequest(verifiedIdOpenIdJwtRawRequest) }

        // Assert
        assertThat(request).isInstanceOf(ManifestIssuanceRequest::class.java)
        assertThat(request.requesterStyle).isEqualTo(requesterStyle)
        assertThat(request.rootOfTrust).isEqualTo(rootOfTrust)
        assertThat(request.requirement).isInstanceOf(SelfAttestedClaimRequirement::class.java)
        assertThat((request.requirement as SelfAttestedClaimRequirement).claim).isEqualTo(
            expectedRequirementClaimName
        )
    }

    @Test
    fun handleRequest_PassOpenIdRawRequestWithUnSupportedRequirement_ThrowsException() {
        //Arrange
        setupInput(
            RequestType.ISSUANCE,
            selfAttestedClaimRequirement,
            logoPresent = true,
            emptyClaims = false
        )

        // Act and assert
        Assertions.assertThatThrownBy {
            runBlocking { openIdRequestHandler.handleRequest(verifiedIdOpenIdJwtRawRequest) }
        }.isInstanceOf(RequirementCastingException::class.java)
    }

    @Test
    fun handleRequest_LogoPresentInVerifiedIdStyle_ReturnsSuccessfulRequestWithLogo() {
        // Arrange
        setupInput(
            RequestType.ISSUANCE,
            verifiedIdRequirement,
            logoPresent = true,
            emptyClaims = false
        )

        // Act
        val actualOpenIdRequest =
            runBlocking { openIdRequestHandler.handleRequest(verifiedIdOpenIdJwtRawRequest) }

        // Assert
        assertThat((actualOpenIdRequest as ManifestIssuanceRequest).verifiedIdStyle.logo).isNotNull
        assertThat(actualOpenIdRequest.verifiedIdStyle.logo?.uri).isEqualTo(expectedLogoUri)
        assertThat(actualOpenIdRequest.verifiedIdStyle.logo?.image).isEqualTo(expectedLogoImage)
        assertThat(actualOpenIdRequest.verifiedIdStyle.logo?.description).isEqualTo(
            expectedLogoDescription
        )
    }

    @Test
    fun handleRequest_NoLogoInVerifiedIdStyle_ReturnsSuccessfulRequestWithNoLogo() {
        // Arrange
        setupInput(
            RequestType.ISSUANCE,
            verifiedIdRequirement,
            logoPresent = false,
            emptyClaims = false
        )

        // Act
        val actualOpenIdRequest =
            runBlocking { openIdRequestHandler.handleRequest(verifiedIdOpenIdJwtRawRequest) }

        // Assert
        assertThat((actualOpenIdRequest as ManifestIssuanceRequest).verifiedIdStyle.logo).isNull()
    }

    @Test
    fun handleRequest_MapDisplayContract_ReturnsVerifiedIdStyleInRequest() {
        // Arrange
        setupInput(
            RequestType.ISSUANCE,
            verifiedIdRequirement,
            logoPresent = false,
            emptyClaims = false
        )

        // Act
        val actualOpenIdRequest =
            runBlocking { openIdRequestHandler.handleRequest(verifiedIdOpenIdJwtRawRequest) }

        // Assert
        assertThat((actualOpenIdRequest as ManifestIssuanceRequest).verifiedIdStyle.locale).isEqualTo(
            expectedLocale
        )
        assertThat(actualOpenIdRequest.verifiedIdStyle.backgroundColor).isEqualTo(
            expectedBackgroundColor
        )
        assertThat(actualOpenIdRequest.verifiedIdStyle.textColor).isEqualTo(expectedTextColor)
        assertThat(actualOpenIdRequest.verifiedIdStyle.title).isEqualTo(expectedCardTitle)
        assertThat(actualOpenIdRequest.verifiedIdStyle.description).isEqualTo(
            expectedCardDescription
        )
        assertThat(actualOpenIdRequest.verifiedIdStyle.issuer).isEqualTo(expectedCardIssuer)
        assertThat(actualOpenIdRequest.verifiedIdStyle.logo).isNull()
        assertThat(actualOpenIdRequest.verifiedIdStyle.claimAttributes.size).isEqualTo(1)
    }

    @Test
    fun handleRequest_MapDisplayContractWithNoClaims_ReturnsVerifiedIdStyleWithNoClaimsInRequest() {
        // Arrange
        setupInput(
            RequestType.ISSUANCE,
            verifiedIdRequirement,
            logoPresent = false,
            emptyClaims = true
        )

        // Act
        val actualOpenIdRequest =
            runBlocking { openIdRequestHandler.handleRequest(verifiedIdOpenIdJwtRawRequest) }

        // Assert
        assertThat((actualOpenIdRequest as ManifestIssuanceRequest).verifiedIdStyle.locale).isEqualTo(
            expectedLocale
        )
        assertThat(actualOpenIdRequest.verifiedIdStyle.backgroundColor).isEqualTo(
            expectedBackgroundColor
        )
        assertThat(actualOpenIdRequest.verifiedIdStyle.textColor).isEqualTo(expectedTextColor)
        assertThat(actualOpenIdRequest.verifiedIdStyle.title).isEqualTo(expectedCardTitle)
        assertThat(actualOpenIdRequest.verifiedIdStyle.description).isEqualTo(
            expectedCardDescription
        )
        assertThat(actualOpenIdRequest.verifiedIdStyle.issuer).isEqualTo(expectedCardIssuer)
        assertThat(actualOpenIdRequest.verifiedIdStyle.logo).isNull()
        assertThat(actualOpenIdRequest.verifiedIdStyle.claimAttributes.size).isEqualTo(0)
    }
}