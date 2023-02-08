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
    fun `test registering one handler`() {
        requestHandlerFactory.requestHandlers.add(firstMockRequestHandler)
        hasCompatibleResolver()
        val actualResult = requestHandlerFactory.getHandler(mockRequestResolver)
        assertThat(actualResult).isEqualTo(firstMockRequestHandler)
    }

    @Test
    fun `test without registering any handler`() {
        assertThatThrownBy { requestHandlerFactory.getHandler(mockRequestResolver) }.isInstanceOf(
            HandlerMissingException::class.java
        )
    }

    @Test
    fun `test without any compatible resolver for handler`() {
        requestHandlerFactory.requestHandlers.add(firstMockRequestHandler)
        doesNotHaveCompatibleResolver(firstMockRequestHandler)
        assertThatThrownBy { requestHandlerFactory.getHandler(mockRequestResolver) }.isInstanceOf(
            UnSupportedResolverException::class.java
        )
    }

    @Test
    fun `test registering multiple handlers`() {
        val secondMockRequestHandler: RequestHandler = mockk()
        requestHandlerFactory.requestHandlers.add(firstMockRequestHandler)
        requestHandlerFactory.requestHandlers.add(secondMockRequestHandler)
        hasCompatibleResolver()
        doesNotHaveCompatibleResolver(secondMockRequestHandler)
        val actualResult = requestHandlerFactory.getHandler(mockRequestResolver)
        assertThat(actualResult).isEqualTo(firstMockRequestHandler)
    }
}