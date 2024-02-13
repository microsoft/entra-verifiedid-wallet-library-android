package com.microsoft.walletlibrary.requests

import com.microsoft.walletlibrary.requests.handlers.RequestProcessor
import com.microsoft.walletlibrary.requests.resolvers.RequestResolver
import com.microsoft.walletlibrary.util.HandlerMissingException
import com.microsoft.walletlibrary.util.UnSupportedResolverException
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

class RequestProcessorFactoryTest {
    private val firstMockRequestProcessor: RequestProcessor = mockk()
    private val mockRequestResolver: RequestResolver = mockk()
    private val requestProcessorFactory = RequestProcessorFactory()

    private fun hasCompatibleResolver() {
        every { mockRequestResolver.canResolve(firstMockRequestProcessor) } returns true
    }

    private fun doesNotHaveCompatibleResolver(mockRequestProcessor: RequestProcessor) {
        every { mockRequestResolver.canResolve(mockRequestProcessor) } returns false
    }

    @Test
    fun handler_RegisterOneHandler_Succeeds() {
        // Arrange
        requestProcessorFactory.requestProcessors.add(firstMockRequestProcessor)
        hasCompatibleResolver()

        // Act
        val actualResult = requestProcessorFactory.getHandler(mockRequestResolver)

        // Assert
        assertThat(actualResult).isEqualTo(firstMockRequestProcessor)
    }

    @Test
    fun handler_NoHandlerRegistration_Throws() {
        // Act and Assert
        assertThatThrownBy { requestProcessorFactory.getHandler(mockRequestResolver) }.isInstanceOf(
            HandlerMissingException::class.java
        )
    }

    @Test
    fun handler_NoCompatibleResolver_Throws() {
        // Arrange
        requestProcessorFactory.requestProcessors.add(firstMockRequestProcessor)
        doesNotHaveCompatibleResolver(firstMockRequestProcessor)

        // Act and Assert
        assertThatThrownBy { requestProcessorFactory.getHandler(mockRequestResolver) }.isInstanceOf(
            UnSupportedResolverException::class.java
        )
    }

    @Test
    fun handler_RegisterMultipleHandlers_Succeeds() {
        // Arrange
        val secondMockRequestProcessor: RequestProcessor = mockk()
        requestProcessorFactory.requestProcessors.add(firstMockRequestProcessor)
        requestProcessorFactory.requestProcessors.add(secondMockRequestProcessor)
        hasCompatibleResolver()
        doesNotHaveCompatibleResolver(secondMockRequestProcessor)

        // Act
        val actualResult = requestProcessorFactory.getHandler(mockRequestResolver)

        // Assert
        assertThat(actualResult).isEqualTo(firstMockRequestProcessor)
    }
}