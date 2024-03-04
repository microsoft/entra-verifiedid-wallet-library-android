package com.microsoft.walletlibrary.requests

import com.microsoft.walletlibrary.requests.handlers.RequestHandler
import com.microsoft.walletlibrary.requests.rawrequests.OpenIdRawRequest
import com.microsoft.walletlibrary.util.HandlerMissingException
import com.microsoft.walletlibrary.util.UnSupportedRawRequestException
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

class RequestHandlerFactoryTest {
    private val firstMockRequestHandler: RequestHandler = mockk()
    private val mockRawRequest: OpenIdRawRequest = mockk()
    private val requestHandlerFactory = RequestHandlerFactory()

    private fun hasCompatibleHandler() {
        every { firstMockRequestHandler.canHandle(mockRawRequest) } returns true
    }

    private fun doesNotHaveCompatibleHandler(mockRawRequest: OpenIdRawRequest) {
        every { firstMockRequestHandler.canHandle(mockRawRequest) } returns false
    }

    @Test
    fun handler_RegisterOneHandler_Succeeds() {
        // Arrange
        requestHandlerFactory.requestHandlers.add(firstMockRequestHandler)
        hasCompatibleHandler()

        // Act
        val actualResult = requestHandlerFactory.getHandler(mockRawRequest)

        // Assert
        assertThat(actualResult).isEqualTo(firstMockRequestHandler)
    }

    @Test
    fun handler_NoHandlerRegistration_Throws() {
        // Act and Assert
        assertThatThrownBy { requestHandlerFactory.getHandler(mockRawRequest) }.isInstanceOf(
            HandlerMissingException::class.java
        )
    }

    @Test
    fun handler_NoCompatibleResolver_Throws() {
        // Arrange
        requestHandlerFactory.requestHandlers.add(firstMockRequestHandler)
        doesNotHaveCompatibleHandler(mockRawRequest)

        // Act and Assert
        assertThatThrownBy { requestHandlerFactory.getHandler(mockRawRequest) }.isInstanceOf(
            UnSupportedRawRequestException::class.java
        )
    }

    @Test
    fun handler_RegisterMultipleHandlers_Succeeds() {
        // Arrange
        val secondMockRequestHandler: RequestHandler = mockk()
        requestHandlerFactory.requestHandlers.add(firstMockRequestHandler)
        requestHandlerFactory.requestHandlers.add(secondMockRequestHandler)
        every { secondMockRequestHandler.canHandle(mockRawRequest) } returns false
        hasCompatibleHandler()

        // Act
        val actualResult = requestHandlerFactory.getHandler(mockRawRequest)

        // Assert
        assertThat(actualResult).isEqualTo(firstMockRequestHandler)
    }
}