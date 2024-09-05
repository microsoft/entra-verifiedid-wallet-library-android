package com.microsoft.walletlibrary.requests

import com.microsoft.walletlibrary.requests.handlers.RequestProcessor
import com.microsoft.walletlibrary.requests.input.VerifiedIdRequestInput
import com.microsoft.walletlibrary.requests.rawrequests.OpenIdRawRequest
import com.microsoft.walletlibrary.requests.resolvers.RequestResolver
import com.microsoft.walletlibrary.util.HandlerMissingException
import com.microsoft.walletlibrary.util.UnSupportedResolverException
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
    fun handler_NoCompatibleResolver_Throws() {
        // Arrange
        requestProcessorFactory.requestProcessors.add(firstMockRequestProcessor)
        doesNotHaveCompatibleResolver()

        // Act and Assert
        assertThatThrownBy {
            runBlocking {
                requestProcessorFactory.getHandler(mockRequestResolver)
            }
        }.isInstanceOf(
            UnSupportedResolverException::class.java
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

        runBlocking {
            // Act
            val actualResult = requestProcessorFactory.getHandler(mockRequestResolver)

            // Assert
            assertThat(actualResult).isEqualTo(firstMockRequestProcessor)
        }
    }
}