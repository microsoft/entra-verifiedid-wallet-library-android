package com.microsoft.walletlibrary.requests

import com.microsoft.walletlibrary.requests.handlers.RequestProcessor
import com.microsoft.walletlibrary.requests.input.VerifiedIdRequestInput
import com.microsoft.walletlibrary.requests.rawrequests.OpenIdRawRequest
import com.microsoft.walletlibrary.requests.resolvers.RequestResolver
import com.microsoft.walletlibrary.util.HandlerMissingException
import com.microsoft.walletlibrary.util.UnSupportedRawRequestException
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

class RequestProcessorFactoryTest {
    private val firstMockRequestProcessor: RequestProcessor<OpenIdRawRequest> = mockk()
    private val mockVerifiedIdRequestInput: VerifiedIdRequestInput = mockk()
    private val mockRequestResolver: RequestResolver = mockk()
    private val requestProcessorFactory = RequestProcessorFactory()

    private fun hasCompatibleResolver() {
        every { mockRequestResolver.canResolve(mockVerifiedIdRequestInput) } returns true
    }

    private fun doesNotHaveCompatibleResolver() {
        every { mockRequestResolver.canResolve(mockVerifiedIdRequestInput) } returns false
    }

    @Test
    fun handler_RegisterOneHandler_Succeeds() {
        // Arrange
        requestProcessorFactory.requestProcessors.add(firstMockRequestProcessor)
        hasCompatibleResolver()
        coEvery { firstMockRequestProcessor.canHandleRequest(any()) } returns true

        runBlocking {
            // Act
            val actualResult = requestProcessorFactory.getHandler(mockRequestResolver)

            // Assert
            assertThat(actualResult).isEqualTo(firstMockRequestProcessor)
        }
    }

    @Test
    fun handler_NoHandlerRegistration_Throws() {
        // Act and Assert
        assertThatThrownBy {
            runBlocking {
                requestProcessorFactory.getHandler(mockRequestResolver)
            }
        }.isInstanceOf(
            HandlerMissingException::class.java
        )
    }

    @Test
    fun handler_NoCompatibleResolver_ThrowsException() {
        // Arrange
        requestProcessorFactory.requestProcessors.add(firstMockRequestProcessor)
        doesNotHaveCompatibleResolver()
        coEvery { firstMockRequestProcessor.canHandleRequest(any()) } returns false

        // Act and Assert
        assertThatThrownBy {
            runBlocking {
                requestProcessorFactory.getHandler(mockRequestResolver)
            }
        }.isInstanceOf(
            UnSupportedRawRequestException::class.java
        )
    }

    @Test
    fun handler_RegisterMultipleHandlers_Succeeds() {
        // Arrange
        val secondMockRequestProcessor: RequestProcessor<OpenIdRawRequest> = mockk()
        requestProcessorFactory.requestProcessors.add(firstMockRequestProcessor)
        requestProcessorFactory.requestProcessors.add(secondMockRequestProcessor)
        hasCompatibleResolver()
        doesNotHaveCompatibleResolver()
        coEvery { firstMockRequestProcessor.canHandleRequest(any()) } returns true
        coEvery { secondMockRequestProcessor.canHandleRequest(any()) } returns false

        runBlocking {
            // Act
            val actualResult = requestProcessorFactory.getHandler(mockRequestResolver)

            // Assert
            assertThat(actualResult).isEqualTo(firstMockRequestProcessor)
        }
    }
}