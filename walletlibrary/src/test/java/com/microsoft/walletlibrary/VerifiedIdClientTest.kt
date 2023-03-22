package com.microsoft.walletlibrary

import com.microsoft.did.sdk.credential.service.PresentationRequest
import com.microsoft.walletlibrary.requests.OpenIdPresentationRequest
import com.microsoft.walletlibrary.requests.RequestHandlerFactory
import com.microsoft.walletlibrary.requests.RequestResolverFactory
import com.microsoft.walletlibrary.requests.VerifiedIdPresentationRequest
import com.microsoft.walletlibrary.requests.handlers.OpenIdRequestHandler
import com.microsoft.walletlibrary.requests.input.VerifiedIdRequestURL
import com.microsoft.walletlibrary.requests.rawrequests.VerifiedIdOpenIdJwtRawRequest
import com.microsoft.walletlibrary.requests.resolvers.OpenIdURLRequestResolver
import com.microsoft.walletlibrary.util.*
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
            VerifiedIdClient(requestResolverFactory, requestHandlerFactory, WalletLibraryLogger)
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
        val verifiedIdClient = VerifiedIdClient(requestResolverFactory, requestHandlerFactory, WalletLibraryLogger)
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
            VerifiedIdClient(requestResolverFactory, requestHandlerFactory, WalletLibraryLogger)
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
            VerifiedIdClient(requestResolverFactory, requestHandlerFactory, WalletLibraryLogger)
        val verifiedIdRequestURL: VerifiedIdRequestURL = mockk()
        every { requestResolverFactory.getResolver(verifiedIdRequestURL) } returns openIdURLRequestResolver
        coEvery { openIdURLRequestResolver.resolve(verifiedIdRequestURL) } returns verifiedIdOpenIdJwtRawRequest
        every { requestHandlerFactory.getHandler(openIdURLRequestResolver) } returns openIdRequestHandler
        coEvery { openIdRequestHandler.handleRequest(verifiedIdOpenIdJwtRawRequest) }.throws(UnSupportedProtocolException())

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
            VerifiedIdClient(requestResolverFactory, requestHandlerFactory, WalletLibraryLogger)
        val verifiedIdRequestURL: VerifiedIdRequestURL = mockk()
        every { requestResolverFactory.getResolver(verifiedIdRequestURL) } returns openIdURLRequestResolver
        coEvery { openIdURLRequestResolver.resolve(verifiedIdRequestURL) }.throws(UnSupportedVerifiedIdRequestInputException())
        every { requestHandlerFactory.getHandler(openIdURLRequestResolver) } returns openIdRequestHandler
        coEvery { openIdRequestHandler.handleRequest(verifiedIdOpenIdJwtRawRequest) } returns openIdPresentationRequest

        // Act and Assert
        Assertions.assertThatThrownBy {
            runBlocking {
                verifiedIdClient.createRequest(verifiedIdRequestURL)
            }
        }.isInstanceOf(UnSupportedVerifiedIdRequestInputException::class.java)
    }
}