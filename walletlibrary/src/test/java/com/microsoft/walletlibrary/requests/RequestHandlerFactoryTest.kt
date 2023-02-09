package com.microsoft.walletlibrary.requests

import com.microsoft.walletlibrary.requests.handlers.RequestHandler
import com.microsoft.walletlibrary.requests.resolvers.RequestResolver
import com.microsoft.walletlibrary.util.HandlerMissingException
import com.microsoft.walletlibrary.util.UnSupportedResolverException
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

class RequestHandlerFactoryTest {
    private val firstMockRequestHandler: RequestHandler = mockk()
    private val mockRequestResolver: RequestResolver = mockk()
    private val requestHandlerFactory = RequestHandlerFactory()

    private fun hasCompatibleResolver() {
        every { mockRequestResolver.canResolve(firstMockRequestHandler) } returns true
    }

    private fun doesNotHaveCompatibleResolver(mockRequestHandler: RequestHandler) {
        every { mockRequestResolver.canResolve(mockRequestHandler) } returns false
    }

    @Test
    fun handler_RegisterOneHandler_Succeeds() {
        // Arrange
        requestHandlerFactory.requestHandlers.add(firstMockRequestHandler)
        hasCompatibleResolver()

        // Act
        val actualResult = requestHandlerFactory.getHandler(mockRequestResolver)

        // Assert
        assertThat(actualResult).isEqualTo(firstMockRequestHandler)
    }

    @Test
    fun handler_NoHandlerRegistration_Throws() {
        // Act and Assert
        assertThatThrownBy { requestHandlerFactory.getHandler(mockRequestResolver) }.isInstanceOf(
            HandlerMissingException::class.java
        )
    }

    @Test
    fun handler_NoCompatibleResolver_Throws() {
        // Arrange
        requestHandlerFactory.requestHandlers.add(firstMockRequestHandler)
        doesNotHaveCompatibleResolver(firstMockRequestHandler)

        // Act and Assert
        assertThatThrownBy { requestHandlerFactory.getHandler(mockRequestResolver) }.isInstanceOf(
            UnSupportedResolverException::class.java
        )
    }

    @Test
    fun handler_RegisterMultipleHandlers_Succeeds() {
        // Arrange
        val secondMockRequestHandler: RequestHandler = mockk()
        requestHandlerFactory.requestHandlers.add(firstMockRequestHandler)
        requestHandlerFactory.requestHandlers.add(secondMockRequestHandler)
        hasCompatibleResolver()
        doesNotHaveCompatibleResolver(secondMockRequestHandler)

        // Act
        val actualResult = requestHandlerFactory.getHandler(mockRequestResolver)

        // Assert
        assertThat(actualResult).isEqualTo(firstMockRequestHandler)
    }
}