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
import com.microsoft.walletlibrary.util.*
import com.microsoft.walletlibrary.verifiedid.VerifiableCredential
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
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
            assertThat(verifiedIdRequest).isInstanceOf(VerifiedIdPresentationRequest::class.java)
        }
    }

    @Test
    fun createRequest_ExceptionFromResolverFactory_ThrowsException() {
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

        // Act and Assert
        Assertions.assertThatThrownBy {
            runBlocking {
                verifiedIdClient.createRequest(verifiedIdRequestURL)
            }
        }.isInstanceOf(ResolverMissingException::class.java)
    }

    @Test
    fun createRequest_ExceptionFromHandlerFactory_ThrowsException() {
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

        // Act and Assert
        Assertions.assertThatThrownBy {
            runBlocking {
                verifiedIdClient.createRequest(verifiedIdRequestURL)
            }
        }.isInstanceOf(HandlerMissingException::class.java)
    }

    @Test
    fun createRequest_ExceptionFromHandler_ThrowsException() {
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

        // Act and Assert
        Assertions.assertThatThrownBy {
            runBlocking {
                verifiedIdClient.createRequest(verifiedIdRequestURL)
            }
        }.isInstanceOf(UnSupportedProtocolException::class.java)
    }

    @Test
    fun createRequest_ExceptionFromResolver_ThrowsException() {
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

        // Act and Assert
        Assertions.assertThatThrownBy {
            runBlocking {
                verifiedIdClient.createRequest(verifiedIdRequestURL)
            }
        }.isInstanceOf(UnSupportedVerifiedIdRequestInputException::class.java)
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
                    card = CardDescriptor("", "", "", "", null, ""),
                    consent = ConsentDescriptor("", ""),
                    claims = mapOf("vc.credentialSubject.claim1" to claimDescriptor1)
                )
            )
        )
        val expectedEncoding = """{"type":"com.microsoft.walletlibrary.verifiedid.VerifiableCredential","raw":{"jti":"123","raw":"raw","contents":{"jti":"456","vc":{"@context":[],"type":["TestVC"],"credentialSubject":{"claim1":"value1"}},"sub":"me","iss":"Test","iat":1234567}},"contract":{"id":"1","input":{"id":"","credentialIssuer":"","issuer":""},"display":{"card":{"title":"","issuedBy":"","backgroundColor":"","textColor":"","description":""},"consent":{"instructions":""},"claims":{"vc.credentialSubject.claim1":{"type":"text","label":"name 1"}}}}}"""

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
                    card = CardDescriptor("", "", "", "", null, ""),
                    consent = ConsentDescriptor("", ""),
                    claims = mapOf("vc.credentialSubject.claim1" to claimDescriptor1)
                )
            )
        )
        val encodedVc = """{"type":"com.microsoft.walletlibrary.verifiedid.VerifiableCredential","raw":{"jti":"123","raw":"raw","contents":{"jti":"456","vc":{"@context":[],"type":["TestVC"],"credentialSubject":{"claim1":"value1"}},"sub":"me","iss":"Test","iat":1234567}},"contract":{"id":"1","input":{"id":"","credentialIssuer":"","issuer":""},"display":{"card":{"title":"","issuedBy":"","backgroundColor":"","textColor":"","description":""},"consent":{"instructions":""},"claims":{"vc.credentialSubject.claim1":{"type":"text","label":"name 1"}}}}}"""

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
    }
}