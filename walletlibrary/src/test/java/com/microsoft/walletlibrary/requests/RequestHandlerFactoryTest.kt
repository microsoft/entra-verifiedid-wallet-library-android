package com.microsoft.walletlibrary.requests

import com.microsoft.walletlibrary.requests.handlers.RequestProcessor
import com.microsoft.walletlibrary.requests.rawrequests.OpenIdRawRequest
import com.microsoft.walletlibrary.util.HandlerMissingException
import com.microsoft.walletlibrary.util.UnSupportedRawRequestException
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

class RequestHandlerFactoryTest {
    private val firstMockRequestProcessor: RequestProcessor = mockk()
    private val mockRawRequest: OpenIdRawRequest = mockk()
    private val requestProcessorFactory = RequestProcessorFactory()

    private fun hasCompatibleHandler() {
        coEvery { firstMockRequestProcessor.canHandleRequest(mockRawRequest) } returns true
    }

    private fun doesNotHaveCompatibleHandler(mockRawRequest: OpenIdRawRequest) {
        coEvery { firstMockRequestProcessor.canHandleRequest(mockRawRequest) } returns false
    }

    @Test
    fun handler_RegisterOneHandler_Succeeds() {
        // Arrange
        requestProcessorFactory.requestProcessors.add(firstMockRequestProcessor)
        hasCompatibleHandler()

        // Act
        runBlocking {
            val actualResult = requestProcessorFactory.getHandler(mockRawRequest)

            // Assert
            assertThat(actualResult).isEqualTo(firstMockRequestProcessor)
        }
    }

    @Test
    fun handler_NoHandlerRegistration_Throws() {
        // Act and Assert
        assertThatThrownBy {
            runBlocking {
                requestProcessorFactory.getHandler(mockRawRequest)
            } }.isInstanceOf(
            HandlerMissingException::class.java
        )
    }

    @Test
    fun handler_NoCompatibleResolver_Throws() {
        // Arrange
        requestProcessorFactory.requestProcessors.add(firstMockRequestProcessor)
        doesNotHaveCompatibleHandler(mockRawRequest)

        // Act and Assert
        assertThatThrownBy {
            runBlocking {
                requestProcessorFactory.getHandler(mockRawRequest)
            }
        }.isInstanceOf(
            UnSupportedRawRequestException::class.java
        )
    }

    @Test
    fun handler_RegisterMultipleHandlers_Succeeds() {
        // Arrange
        val secondMockRequestHandler: RequestProcessor = mockk()
        requestProcessorFactory.requestProcessors.add(firstMockRequestProcessor)
        requestProcessorFactory.requestProcessors.add(secondMockRequestHandler)
        coEvery { secondMockRequestHandler.canHandleRequest(mockRawRequest) } returns false
        hasCompatibleHandler()

        // Act
        runBlocking {
            val actualResult = requestProcessorFactory.getHandler(mockRawRequest)

            // Assert
            assertThat(actualResult).isEqualTo(firstMockRequestProcessor)
        }
    }
}