package com.microsoft.walletlibrary

import com.microsoft.walletlibrary.did.sdk.CorrelationVectorService
import com.microsoft.walletlibrary.did.sdk.VerifiableCredentialSdk
import com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredentialContent
import com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredentialDescriptor
import com.microsoft.walletlibrary.did.sdk.credential.service.IssuanceRequest
import com.microsoft.walletlibrary.did.sdk.credential.service.PresentationRequest
import com.microsoft.walletlibrary.did.sdk.credential.service.models.attestations.ClaimAttestation
import com.microsoft.walletlibrary.did.sdk.credential.service.models.attestations.CredentialAttestations
import com.microsoft.walletlibrary.did.sdk.credential.service.models.attestations.SelfIssuedAttestation
import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.InputContract
import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.VerifiableCredentialContract
import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.display.CardDescriptor
import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.display.ClaimDescriptor
import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.display.ConsentDescriptor
import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.display.DisplayContract
import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.display.Logo
import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainMissing
import com.microsoft.walletlibrary.requests.ManifestIssuanceRequest
import com.microsoft.walletlibrary.requests.OpenIdPresentationRequest
import com.microsoft.walletlibrary.requests.RequestHandlerFactory
import com.microsoft.walletlibrary.requests.RequestResolverFactory
import com.microsoft.walletlibrary.requests.RootOfTrust
import com.microsoft.walletlibrary.requests.VerifiedIdPresentationRequest
import com.microsoft.walletlibrary.requests.handlers.OpenIdRequestHandler
import com.microsoft.walletlibrary.requests.input.VerifiedIdRequestURL
import com.microsoft.walletlibrary.requests.rawrequests.RawManifest
import com.microsoft.walletlibrary.requests.rawrequests.VerifiedIdOpenIdJwtRawRequest
import com.microsoft.walletlibrary.requests.requirements.SelfAttestedClaimRequirement
import com.microsoft.walletlibrary.requests.resolvers.OpenIdURLRequestResolver
import com.microsoft.walletlibrary.requests.styles.BasicVerifiedIdStyle
import com.microsoft.walletlibrary.requests.styles.VerifiedIdManifestIssuerStyle
import com.microsoft.walletlibrary.util.HandlerMissingException
import com.microsoft.walletlibrary.util.MalformedInputException
import com.microsoft.walletlibrary.util.ResolverMissingException
import com.microsoft.walletlibrary.util.UnSupportedProtocolException
import com.microsoft.walletlibrary.util.UnSupportedVerifiedIdRequestInputException
import com.microsoft.walletlibrary.util.UnspecifiedVerifiedIdException
import com.microsoft.walletlibrary.util.VerifiedIdResult
import com.microsoft.walletlibrary.util.WalletLibraryLogger
import com.microsoft.walletlibrary.util.defaultTestSerializer
import com.microsoft.walletlibrary.verifiedid.VerifiableCredential
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class VerifiedIdClientTest {
    private val mockCorrelationVectorService: CorrelationVectorService = mockk()
    private val openIdRequestHandler: OpenIdRequestHandler = mockk()
    private val openIdURLRequestResolver: OpenIdURLRequestResolver = mockk()
    private val presentationRequest: PresentationRequest = mockk()
    private val openIdPresentationRequest: OpenIdPresentationRequest = mockk()
    private val verifiedIdOpenIdJwtRawRequest = VerifiedIdOpenIdJwtRawRequest(presentationRequest)
    private lateinit var requestHandlerFactory: RequestHandlerFactory
    private lateinit var requestResolverFactory: RequestResolverFactory

    init {
        setupInput()
    }

    private fun setupInput() {
        mockkStatic(VerifiableCredentialSdk::class)
        every { VerifiableCredentialSdk.correlationVectorService } returns mockCorrelationVectorService
        every { mockCorrelationVectorService.startNewFlowAndSave() } returns ""
    }

    @Test
    fun createRequest_SuccessFromResolverAndHandler_ReturnsVerifiedIdRequest() {
        // Arrange
        requestHandlerFactory = mockk()
        requestResolverFactory = mockk()
        val verifiedIdClient =
            VerifiedIdClient(
                requestResolverFactory,
                requestHandlerFactory,
                WalletLibraryLogger,
                defaultTestSerializer
            )
        val verifiedIdRequestURL: VerifiedIdRequestURL = mockk()
        every { requestResolverFactory.getResolver(verifiedIdRequestURL) } returns openIdURLRequestResolver
        coEvery { openIdURLRequestResolver.resolve(verifiedIdRequestURL) } returns verifiedIdOpenIdJwtRawRequest
        every { requestHandlerFactory.getHandler(openIdURLRequestResolver) } returns openIdRequestHandler
        coEvery { openIdRequestHandler.handleRequest(verifiedIdOpenIdJwtRawRequest) } returns openIdPresentationRequest

        runBlocking {
            // Act
            val verifiedIdRequest = verifiedIdClient.createRequest(verifiedIdRequestURL)

            // Assert
            assertThat(verifiedIdRequest).isInstanceOf(VerifiedIdResult::class.java)
            assertThat(verifiedIdRequest.isSuccess).isTrue
            assertThat(verifiedIdRequest.getOrNull()).isNotNull
            assertThat(verifiedIdRequest.getOrNull()).isInstanceOf(VerifiedIdPresentationRequest::class.java)
        }
    }

    @Test
    fun createRequest_ExceptionFromResolverFactory_ReturnsFailure() {
        // Arrange
        requestHandlerFactory = mockk()
        requestResolverFactory = RequestResolverFactory()
        val verifiedIdClient = VerifiedIdClient(
            requestResolverFactory,
            requestHandlerFactory,
            WalletLibraryLogger,
            defaultTestSerializer
        )
        val verifiedIdRequestURL: VerifiedIdRequestURL = mockk()
        coEvery { openIdURLRequestResolver.resolve(verifiedIdRequestURL) } returns verifiedIdOpenIdJwtRawRequest
        every { requestHandlerFactory.getHandler(openIdURLRequestResolver) } returns openIdRequestHandler
        coEvery { openIdRequestHandler.handleRequest(verifiedIdOpenIdJwtRawRequest) } returns openIdPresentationRequest

        runBlocking {
            // Act
            val actualResult = verifiedIdClient.createRequest(verifiedIdRequestURL)
            val actualException = actualResult.exceptionOrNull()

            // Assert
            assertThat(actualResult).isInstanceOf(VerifiedIdResult::class.java)
            assertThat(actualResult.isFailure).isTrue
            assertThat(actualException).isNotNull
            assertThat(actualException).isInstanceOf(UnspecifiedVerifiedIdException::class.java)
            assertThat((actualException as UnspecifiedVerifiedIdException).code).isEqualTo("unspecified_error")
            assertThat(actualException.correlationId).isNull()
            assertThat(actualException.innerError).isInstanceOf(ResolverMissingException::class.java)
        }
    }

    @Test
    fun createRequest_ExceptionFromHandlerFactory_ReturnsFailure() {
        // Arrange
        requestHandlerFactory = RequestHandlerFactory()
        requestResolverFactory = mockk()
        val verifiedIdClient =
            VerifiedIdClient(
                requestResolverFactory,
                requestHandlerFactory,
                WalletLibraryLogger,
                defaultTestSerializer
            )
        val verifiedIdRequestURL: VerifiedIdRequestURL = mockk()
        every { requestResolverFactory.getResolver(verifiedIdRequestURL) } returns openIdURLRequestResolver
        coEvery { openIdURLRequestResolver.resolve(verifiedIdRequestURL) } returns verifiedIdOpenIdJwtRawRequest
        coEvery { openIdRequestHandler.handleRequest(verifiedIdOpenIdJwtRawRequest) } returns openIdPresentationRequest

        runBlocking {
            // Act
            val actualResult = verifiedIdClient.createRequest(verifiedIdRequestURL)
            val actualException = actualResult.exceptionOrNull()

            // Assert
            assertThat(actualResult).isInstanceOf(VerifiedIdResult::class.java)
            assertThat(actualResult.isFailure).isTrue
            assertThat(actualException).isNotNull
            assertThat(actualException).isInstanceOf(UnspecifiedVerifiedIdException::class.java)
            assertThat((actualException as UnspecifiedVerifiedIdException).code).isEqualTo("unspecified_error")
            assertThat(actualException.correlationId).isNull()
            assertThat(actualException.innerError).isInstanceOf(HandlerMissingException::class.java)
        }
    }

    @Test
    fun createRequest_ExceptionFromHandler_ReturnsFailure() {
        // Arrange
        requestHandlerFactory = mockk()
        requestResolverFactory = mockk()
        val verifiedIdClient =
            VerifiedIdClient(
                requestResolverFactory,
                requestHandlerFactory,
                WalletLibraryLogger,
                defaultTestSerializer
            )
        val verifiedIdRequestURL: VerifiedIdRequestURL = mockk()
        every { requestResolverFactory.getResolver(verifiedIdRequestURL) } returns openIdURLRequestResolver
        coEvery { openIdURLRequestResolver.resolve(verifiedIdRequestURL) } returns verifiedIdOpenIdJwtRawRequest
        every { requestHandlerFactory.getHandler(openIdURLRequestResolver) } returns openIdRequestHandler
        coEvery { openIdRequestHandler.handleRequest(verifiedIdOpenIdJwtRawRequest) }.throws(
            UnSupportedProtocolException()
        )

        runBlocking {
            // Act
            val actualResult = verifiedIdClient.createRequest(verifiedIdRequestURL)
            val actualException = actualResult.exceptionOrNull()

            // Assert
            assertThat(actualResult).isInstanceOf(VerifiedIdResult::class.java)
            assertThat(actualResult.isFailure).isTrue
            assertThat(actualException).isNotNull
            assertThat(actualException).isInstanceOf(UnspecifiedVerifiedIdException::class.java)
            assertThat((actualException as UnspecifiedVerifiedIdException).code).isEqualTo("unspecified_error")
            assertThat(actualException.correlationId).isNull()
            assertThat(actualException.innerError).isInstanceOf(UnSupportedProtocolException::class.java)
        }
    }

    @Test
    fun createRequest_ExceptionFromResolver_ReturnsFailure() {
        // Arrange
        requestHandlerFactory = mockk()
        requestResolverFactory = mockk()
        val verifiedIdClient =
            VerifiedIdClient(
                requestResolverFactory,
                requestHandlerFactory,
                WalletLibraryLogger,
                defaultTestSerializer
            )
        val verifiedIdRequestURL: VerifiedIdRequestURL = mockk()
        every { requestResolverFactory.getResolver(verifiedIdRequestURL) } returns openIdURLRequestResolver
        coEvery { openIdURLRequestResolver.resolve(verifiedIdRequestURL) }.throws(
            UnSupportedVerifiedIdRequestInputException()
        )
        every { requestHandlerFactory.getHandler(openIdURLRequestResolver) } returns openIdRequestHandler
        coEvery { openIdRequestHandler.handleRequest(verifiedIdOpenIdJwtRawRequest) } returns openIdPresentationRequest

        runBlocking {
            // Act
            val actualResult = verifiedIdClient.createRequest(verifiedIdRequestURL)
            val actualException = actualResult.exceptionOrNull()

            // Assert
            assertThat(actualResult).isInstanceOf(VerifiedIdResult::class.java)
            assertThat(actualResult.isFailure).isTrue
            assertThat(actualException).isNotNull
            assertThat(actualException).isInstanceOf(UnspecifiedVerifiedIdException::class.java)
            assertThat((actualException as UnspecifiedVerifiedIdException).code).isEqualTo("unspecified_error")
            assertThat(actualException.correlationId).isNull()
            assertThat(actualException.innerError).isInstanceOf(
                UnSupportedVerifiedIdRequestInputException::class.java
            )
        }
    }

    @Test
    fun encode_ProvideVerifiableCredential_ReturnsEncodedString() {
        // Arrange
        requestHandlerFactory = mockk()
        requestResolverFactory = mockk()
        val verifiedIdClient =
            VerifiedIdClient(
                requestResolverFactory,
                requestHandlerFactory,
                WalletLibraryLogger,
                defaultTestSerializer
            )
        val claimDescriptor1 = ClaimDescriptor("text", "name 1")
        val vc = VerifiableCredential(
            com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredential(
                "123",
                "raw",
                VerifiableCredentialContent(
                    "456",
                    VerifiableCredentialDescriptor(
                        emptyList(),
                        listOf("TestVC"),
                        mapOf("claim1" to "value1")
                    ),
                    "me",
                    "Test",
                    1234567L,
                    null
                )
            ),
            VerifiableCredentialContract(
                "1",
                InputContract("", "", ""),
                DisplayContract(
                    card = CardDescriptor(
                        "Test VC",
                        "Test Issuer",
                        "#000000",
                        "#ffffff",
                        Logo("testlogo.com", null, "test logo"),
                        ""
                    ),
                    consent = ConsentDescriptor("", ""),
                    claims = mapOf("vc.credentialSubject.claim1" to claimDescriptor1)
                )
            )
        )
        val expectedEncoding =
            """{"type":"com.microsoft.walletlibrary.verifiedid.VerifiableCredential","raw":{"jti":"123","raw":"raw","contents":{"jti":"456","vc":{"@context":[],"type":["TestVC"],"credentialSubject":{"claim1":"value1"}},"sub":"me","iss":"Test","iat":1234567}},"contract":{"id":"1","input":{"id":"","credentialIssuer":"","issuer":""},"display":{"card":{"title":"Test VC","issuedBy":"Test Issuer","backgroundColor":"#000000","textColor":"#ffffff","logo":{"uri":"testlogo.com","description":"test logo"},"description":""},"consent":{"instructions":""},"claims":{"vc.credentialSubject.claim1":{"type":"text","label":"name 1"}}}},"style":{"type":"com.microsoft.walletlibrary.requests.styles.BasicVerifiedIdStyle","name":"Test VC","issuer":"Test Issuer","backgroundColor":"#000000","textColor":"#ffffff","description":"","logo":{"url":"testlogo.com","altText":"test logo"}}}"""

        // Act
        val actualEncodedVc = verifiedIdClient.encode(vc)

        // Assert
        assertThat(actualEncodedVc).isInstanceOf(VerifiedIdResult::class.java)
        assertThat(actualEncodedVc.isSuccess).isTrue
        assertThat(actualEncodedVc.getOrNull()).isNotNull
        assertThat(actualEncodedVc.getOrNull()).isEqualTo(expectedEncoding)
    }

    @Test
    fun decode_ProvideEncodedVerifiableCredential_ReturnsVerifiableCredentialObject() {
        // Arrange
        requestHandlerFactory = mockk()
        requestResolverFactory = mockk()
        val verifiedIdClient =
            VerifiedIdClient(
                requestResolverFactory,
                requestHandlerFactory,
                WalletLibraryLogger,
                defaultTestSerializer
            )
        val claimDescriptor1 = ClaimDescriptor("text", "name 1")
        val expectedVc = VerifiableCredential(
            com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredential(
                "123",
                "raw",
                VerifiableCredentialContent(
                    "456",
                    VerifiableCredentialDescriptor(emptyList(), listOf("TestVC"), emptyMap()),
                    "me",
                    "Test",
                    1234567L,
                    null
                )
            ),
            VerifiableCredentialContract(
                "1",
                InputContract("", "", ""),
                DisplayContract(
                    card = CardDescriptor("Test VC", "Test Issuer", "#000000", "#ffffff", null, ""),
                    consent = ConsentDescriptor("", ""),
                    claims = mapOf("vc.credentialSubject.claim1" to claimDescriptor1)
                )
            )
        )
        val encodedVc =
            """{"type":"com.microsoft.walletlibrary.verifiedid.VerifiableCredential","raw":{"jti":"123","raw":"raw","contents":{"jti":"456","vc":{"@context":[],"type":["TestVC"],"credentialSubject":{"claim1":"value1"}},"sub":"me","iss":"Test","iat":1234567}},"contract":{"id":"1","input":{"id":"","credentialIssuer":"","issuer":""},"display":{"card":{"title":"Test VC","issuedBy":"Test Issuer","backgroundColor":"#000000","textColor":"#ffffff","description":""},"consent":{"instructions":""},"claims":{"vc.credentialSubject.claim1":{"type":"text","label":"name 1"}}}},"style":{"type":"com.microsoft.walletlibrary.requests.styles.BasicVerifiedIdStyle","name":"Test VC","issuer":"Test Issuer","backgroundColor":"#000000","textColor":"#ffffff","description":""}}"""

        // Act
        val actualDecodedVc = verifiedIdClient.decodeVerifiedId(encodedVc)
        val actualVc = actualDecodedVc.getOrNull()

        // Assert
        assertThat(actualDecodedVc).isInstanceOf(VerifiedIdResult::class.java)
        assertThat(actualDecodedVc.isSuccess).isTrue
        assertThat(actualVc).isNotNull
        assertThat(actualVc).isInstanceOf(VerifiableCredential::class.java)
        assertThat((actualVc as VerifiableCredential).getClaims().size).isEqualTo(1)
        assertThat(actualVc.getClaims().first().id).isEqualTo("name 1")
        assertThat(actualVc.getClaims().first().value).isEqualTo("\"value1\"")
        assertThat(actualVc.style).isInstanceOf(BasicVerifiedIdStyle::class.java)
        assertThat(actualVc.style).isNotNull
        assertThat(actualVc.style?.name).isEqualTo("Test VC")
        assertThat((actualVc.style as BasicVerifiedIdStyle).backgroundColor).isEqualTo("#000000")
        assertThat((actualVc.style as BasicVerifiedIdStyle).textColor).isEqualTo("#ffffff")
        assertThat((actualVc.style as BasicVerifiedIdStyle).issuer).isEqualTo("Test Issuer")
    }

    @Test
    fun encodeVerifiedIdRequest_ProvideManifestIssuanceRequest_ReturnsEncodedString() {
        // Arrange
        val expectedClaimName = "name"
        val expectedClaimType = "string"
        val claimAttestation = ClaimAttestation(expectedClaimName, true, expectedClaimType)
        val expectedIssuer = "issuer"
        val expectedCardDescription = "card description"
        val expectedTextColor = "#000000"
        val expectedBackgroundColor = "#FFFFFF"
        val expectedIssuerInCard = "Test Issuer"
        val expectedCardTitle = "Card Title"
        val expectedConsentTitle = "Consent Title"
        val expectedConsentInstructions = "Consent Instructions"
        val expectedContractUrl = "test.com"
        val selfIssuedAttestation = SelfIssuedAttestation(required = true, claims = listOf(claimAttestation))
        val inputContract = InputContract(
            "",
            "",
            expectedIssuer,
            CredentialAttestations(selfIssued = selfIssuedAttestation)
        )
        val cardDescriptor = CardDescriptor(
            expectedCardTitle,
            expectedIssuerInCard,
            expectedBackgroundColor,
            expectedTextColor,
            null,
            expectedCardDescription
        )
        val consentDescriptor =
            ConsentDescriptor(expectedConsentTitle, expectedConsentInstructions)
        val displayContract =
            DisplayContract("", "", "", cardDescriptor, consentDescriptor, emptyMap())
        val contract = VerifiableCredentialContract("", inputContract, displayContract)
        val mockIssuanceRequest =
            IssuanceRequest(contract, expectedContractUrl, LinkedDomainMissing)
        val manifestIssuanceRequest = ManifestIssuanceRequest(
            VerifiedIdManifestIssuerStyle("name", "title", "instructions"),
            SelfAttestedClaimRequirement("id", "claim"),
            RootOfTrust("source"),
            BasicVerifiedIdStyle("name", "issuer", "backgroundColor", "textColor", "description"),
            RawManifest(mockIssuanceRequest)
        )
        requestHandlerFactory = mockk()
        requestResolverFactory = mockk()
        val verifiedIdClient =
            VerifiedIdClient(
                requestResolverFactory,
                requestHandlerFactory,
                WalletLibraryLogger,
                defaultTestSerializer
            )

        // Act
        val encodedRequest = verifiedIdClient.encodeRequest(manifestIssuanceRequest)

        // Assert
        assertThat(encodedRequest).isInstanceOf(Result::class.java)
        assertThat(encodedRequest.isSuccess).isTrue
        assertThat(encodedRequest.getOrNull()).isNotNull
    }

    @Test
    fun decodeRequest_ProvideEncodedVerifiedIdRequest_ReturnsVerifiedIdRequestObject() {
        // Arrange
        val encodedVerifiedIdRequest =
            """{"requesterStyle":{"type":"com.microsoft.walletlibrary.requests.styles.VerifiedIdManifestIssuerStyle","name":"name","requestTitle":"title","requestInstructions":"instructions"},"requirement":{"type":"com.microsoft.walletlibrary.requests.requirements.SelfAttestedClaimRequirement","id":"id","claim":"claim"},"rootOfTrust":{"source":"source"},"verifiedIdStyle":{"type":"com.microsoft.walletlibrary.requests.styles.BasicVerifiedIdStyle","name":"name","issuer":"issuer","backgroundColor":"backgroundColor","textColor":"textColor","description":"description"},"request":{"rawRequest":{"entityName":"Test Issuer","entityIdentifier":"issuer","contract":{"id":"","input":{"id":"","credentialIssuer":"","issuer":"issuer","attestations":{"selfIssued":{"claims":[{"claim":"name","required":true,"type":"string"}],"required":true}}},"display":{"id":"","card":{"title":"Card Title","issuedBy":"Test Issuer","backgroundColor":"#FFFFFF","textColor":"#000000","description":"card description"},"consent":{"title":"Consent Title","instructions":"Consent Instructions"},"claims":{}}},"contractUrl":"test.com","linkedDomainResult":{"type":"LinkedDomainMissing"}}}}"""
        requestHandlerFactory = mockk()
        requestResolverFactory = mockk()
        val verifiedIdClient =
            VerifiedIdClient(
                requestResolverFactory,
                requestHandlerFactory,
                WalletLibraryLogger,
                defaultTestSerializer
            )

        // Act
        val decodedRequest = verifiedIdClient.decodeRequest(encodedVerifiedIdRequest)

        // Assert
        assertThat(decodedRequest).isInstanceOf(Result::class.java)
        assertThat(decodedRequest.isSuccess).isTrue
        assertThat(decodedRequest.getOrNull()).isNotNull
        assertThat(decodedRequest.getOrNull()).isInstanceOf(ManifestIssuanceRequest::class.java)
    }

    @Test
    fun encode_ProvideInvalidVerifiableCredential_ThrowsException() {
        // Arrange
        requestHandlerFactory = mockk()
        requestResolverFactory = mockk()
        val serializer = mockk<Json>()
        val verifiedIdClient =
            VerifiedIdClient(
                requestResolverFactory,
                requestHandlerFactory,
                WalletLibraryLogger,
                serializer
            )
        val claimDescriptor1 = ClaimDescriptor("text", "name 1")
        val vc = VerifiableCredential(
            com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredential(
                "123",
                "raw",
                VerifiableCredentialContent(
                    "456",
                    VerifiableCredentialDescriptor(emptyList(), listOf("TestVC"), mapOf("claim1" to "value1")),
                    "me",
                    "Test",
                    1234567L,
                    null
                )
            ),
            VerifiableCredentialContract(
                "1",
                InputContract("", "", ""),
                DisplayContract(
                    card = CardDescriptor("Test VC", "Test Issuer", "#000000", "#ffffff", Logo("testlogo.com", null, "test logo"), ""),
                    consent = ConsentDescriptor("", ""),
                    claims = mapOf("vc.credentialSubject.claim1" to claimDescriptor1)
                )
            )
        )
        every { serializer.encodeToString(vc) } throws Exception()

        // Act
        val actualEncodedVc = verifiedIdClient.encode(vc)

        // Assert
        assertThat(actualEncodedVc).isInstanceOf(VerifiedIdResult::class.java)
        assertThat(actualEncodedVc.isSuccess).isFalse
        assertThat(actualEncodedVc.exceptionOrNull()).isNotNull
        assertThat(actualEncodedVc.exceptionOrNull()).isInstanceOf(MalformedInputException::class.java)
    }

    @Test
    fun decode_ProvideInvalidEncodedVerifiableCredential_ThrowsException() {
        // Arrange
        requestHandlerFactory = mockk()
        requestResolverFactory = mockk()
        val serializer = mockk<Json>()
        val verifiedIdClient =
            VerifiedIdClient(
                requestResolverFactory,
                requestHandlerFactory,
                WalletLibraryLogger,
                serializer
            )
        val claimDescriptor1 = ClaimDescriptor("text", "name 1")
        val expectedVc = VerifiableCredential(
            com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredential(
                "123",
                "raw",
                VerifiableCredentialContent(
                    "456",
                    VerifiableCredentialDescriptor(emptyList(), listOf("TestVC"), emptyMap()),
                    "me",
                    "Test",
                    1234567L,
                    null
                )
            ),
            VerifiableCredentialContract(
                "1",
                InputContract("", "", ""),
                DisplayContract(
                    card = CardDescriptor("Test VC", "Test Issuer", "#000000", "#ffffff", null, ""),
                    consent = ConsentDescriptor("", ""),
                    claims = mapOf("vc.credentialSubject.claim1" to claimDescriptor1)
                )
            )
        )
        val encodedVc = """{"type":"com.microsoft.walletlibrary.verifiedid.VerifiableCredential","raw":{"jti":"123","raw":"raw","contents":{"jti":"456","vc":{"@context":[],"type":["TestVC"],"credentialSubject":{"claim1":"value1"}},"sub":"me","iss":"Test","iat":1234567}},"contract":{"id":"1","input":{"id":"","credentialIssuer":"","issuer":""},"display":{"card":{"title":"Test VC","issuedBy":"Test Issuer","backgroundColor":"#000000","textColor":"#ffffff","description":""},"consent":{"instructions":""},"claims":{"vc.credentialSubject.claim1":{"type":"text","label":"name 1"}}}},"style":{"type":"com.microsoft.walletlibrary.requests.styles.BasicVerifiedIdStyle","name":"Test VC","issuer":"Test Issuer","backgroundColor":"#000000","textColor":"#ffffff","description":""}}"""
        every { serializer.decodeFromString(VerifiableCredential.serializer(), encodedVc) } throws Exception()

        // Act
        val actualDecodedVc = verifiedIdClient.decodeVerifiedId(encodedVc)
        val actualVc = actualDecodedVc.exceptionOrNull()

        // Assert
        assertThat(actualDecodedVc).isInstanceOf(VerifiedIdResult::class.java)
        assertThat(actualDecodedVc.isSuccess).isFalse
        assertThat(actualVc).isNotNull
        assertThat(actualVc).isInstanceOf(MalformedInputException::class.java)
    }
}