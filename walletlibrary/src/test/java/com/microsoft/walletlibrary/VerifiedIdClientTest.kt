package com.microsoft.walletlibrary

import com.microsoft.did.sdk.credential.models.VerifiableCredentialContent
import com.microsoft.did.sdk.credential.models.VerifiableCredentialDescriptor
import com.microsoft.did.sdk.credential.service.PresentationRequest
import com.microsoft.did.sdk.credential.service.models.contracts.InputContract
import com.microsoft.did.sdk.credential.service.models.contracts.VerifiableCredentialContract
import com.microsoft.did.sdk.credential.service.models.contracts.display.CardDescriptor
import com.microsoft.did.sdk.credential.service.models.contracts.display.ClaimDescriptor
import com.microsoft.did.sdk.credential.service.models.contracts.display.ConsentDescriptor
import com.microsoft.did.sdk.credential.service.models.contracts.display.DisplayContract
import com.microsoft.walletlibrary.requests.OpenIdPresentationRequest
import com.microsoft.walletlibrary.requests.RequestHandlerFactory
import com.microsoft.walletlibrary.requests.RequestResolverFactory
import com.microsoft.walletlibrary.requests.VerifiedIdPresentationRequest
import com.microsoft.walletlibrary.requests.handlers.OpenIdRequestHandler
import com.microsoft.walletlibrary.requests.input.VerifiedIdRequestURL
import com.microsoft.walletlibrary.requests.rawrequests.VerifiedIdOpenIdJwtRawRequest
import com.microsoft.walletlibrary.requests.resolvers.OpenIdURLRequestResolver
import com.microsoft.walletlibrary.requests.styles.BasicVerifiedIdStyle
import com.microsoft.walletlibrary.util.*
import com.microsoft.walletlibrary.verifiedid.VerifiableCredential
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class VerifiedIdClientTest {
    private val openIdRequestHandler: OpenIdRequestHandler = mockk()
    private val openIdURLRequestResolver: OpenIdURLRequestResolver = mockk()
    private val presentationRequest: PresentationRequest = mockk()
    private val openIdPresentationRequest: OpenIdPresentationRequest = mockk()
    private val verifiedIdOpenIdJwtRawRequest = VerifiedIdOpenIdJwtRawRequest(presentationRequest)
    private lateinit var requestHandlerFactory: RequestHandlerFactory
    private lateinit var requestResolverFactory: RequestResolverFactory

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
            assertThat(verifiedIdRequest).isInstanceOf(Result::class.java)
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

            // Assert
            assertThat(actualResult).isInstanceOf(Result::class.java)
            assertThat(actualResult.isFailure).isTrue
            assertThat(actualResult.exceptionOrNull()).isNotNull
            assertThat(actualResult.exceptionOrNull()).isInstanceOf(ResolverMissingException::class.java)
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

            // Assert
            assertThat(actualResult).isInstanceOf(Result::class.java)
            assertThat(actualResult.isFailure).isTrue
            assertThat(actualResult.exceptionOrNull()).isNotNull
            assertThat(actualResult.exceptionOrNull()).isInstanceOf(HandlerMissingException::class.java)
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

            // Assert
            assertThat(actualResult).isInstanceOf(Result::class.java)
            assertThat(actualResult.isFailure).isTrue
            assertThat(actualResult.exceptionOrNull()).isNotNull
            assertThat(actualResult.exceptionOrNull()).isInstanceOf(UnSupportedProtocolException::class.java)
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

            // Assert
            assertThat(actualResult).isInstanceOf(Result::class.java)
            assertThat(actualResult.isFailure).isTrue
            assertThat(actualResult.exceptionOrNull()).isNotNull
            assertThat(actualResult.exceptionOrNull()).isInstanceOf(UnSupportedVerifiedIdRequestInputException::class.java)
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
            com.microsoft.did.sdk.credential.models.VerifiableCredential(
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
                    card = CardDescriptor("Test VC", "Test Issuer", "#000000", "#ffffff", null, ""),
                    consent = ConsentDescriptor("", ""),
                    claims = mapOf("vc.credentialSubject.claim1" to claimDescriptor1)
                )
            )
        )
        val expectedEncoding = """{"type":"com.microsoft.walletlibrary.verifiedid.VerifiableCredential","raw":{"jti":"123","raw":"raw","contents":{"jti":"456","vc":{"@context":[],"type":["TestVC"],"credentialSubject":{"claim1":"value1"}},"sub":"me","iss":"Test","iat":1234567}},"contract":{"id":"1","input":{"id":"","credentialIssuer":"","issuer":""},"display":{"card":{"title":"Test VC","issuedBy":"Test Issuer","backgroundColor":"#000000","textColor":"#ffffff","description":""},"consent":{"instructions":""},"claims":{"vc.credentialSubject.claim1":{"type":"text","label":"name 1"}}}},"style":{"type":"com.microsoft.walletlibrary.requests.styles.BasicVerifiedIdStyle","name":"Test VC","issuer":"Test Issuer","backgroundColor":"#000000","textColor":"#ffffff","description":""}}"""

        // Act
        val actualEncodedVc = verifiedIdClient.encode(vc)

        // Assert
        assertThat(actualEncodedVc).isInstanceOf(Result::class.java)
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
            com.microsoft.did.sdk.credential.models.VerifiableCredential(
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

        // Act
        val actualDecodedVc = verifiedIdClient.decodeVerifiedId(encodedVc)

        // Assert
        assertThat(actualDecodedVc).isInstanceOf(Result::class.java)
        assertThat(actualDecodedVc.isSuccess).isTrue
        assertThat(actualDecodedVc.getOrNull()).isNotNull
        assertThat(actualDecodedVc.getOrNull()).isInstanceOf(VerifiableCredential::class.java)
        assertThat((actualDecodedVc.getOrNull() as VerifiableCredential).getClaims().size).isEqualTo(1)
        assertThat((actualDecodedVc.getOrNull() as VerifiableCredential).getClaims().first().id).isEqualTo("name 1")
        assertThat((actualDecodedVc.getOrNull() as VerifiableCredential).getClaims().first().value).isEqualTo("\"value1\"")
        assertThat((actualDecodedVc.getOrNull() as VerifiableCredential).style).isInstanceOf(BasicVerifiedIdStyle::class.java)
        assertThat((actualDecodedVc.getOrNull() as VerifiableCredential).style.name).isEqualTo("Test VC")
        assertThat(((actualDecodedVc.getOrNull() as VerifiableCredential).style as BasicVerifiedIdStyle).backgroundColor).isEqualTo("#000000")
        assertThat(((actualDecodedVc.getOrNull() as VerifiableCredential).style as BasicVerifiedIdStyle).textColor).isEqualTo("#ffffff")
        assertThat(((actualDecodedVc.getOrNull() as VerifiableCredential).style as BasicVerifiedIdStyle).issuer).isEqualTo("Test Issuer")
    }
}