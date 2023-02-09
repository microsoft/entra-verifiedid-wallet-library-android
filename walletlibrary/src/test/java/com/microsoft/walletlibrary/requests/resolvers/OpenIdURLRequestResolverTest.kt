package com.microsoft.walletlibrary.requests.resolvers

import com.microsoft.walletlibrary.requests.handlers.OpenIdRequestHandler
import com.microsoft.walletlibrary.requests.input.VerifiedIdRequestURL
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class OpenIdURLRequestResolverTest {
    private val mockOpenIdURLRequestResolver = OpenIdURLRequestResolver()

    @Test
    fun resolver_CanResolveHandler_Succeeds() {
        // Arrange
        val mockOpenIdRequestHandler: OpenIdRequestHandler = mockk()

        // Act
        val actualResult = mockOpenIdURLRequestResolver.canResolve(mockOpenIdRequestHandler)

        // Assert
        assertThat(actualResult).isEqualTo(true)
    }

    @Test
    fun resolver_CanResolveInput_Succeeds() {
        // Arrange
        val mockVerifiedIdRequestURL: VerifiedIdRequestURL = mockk()
        every { mockVerifiedIdRequestURL.url.scheme } returns "openid-vc"

        // Act
        val actualResult = mockOpenIdURLRequestResolver.canResolve(mockVerifiedIdRequestURL)

        // Assert
        assertThat(actualResult).isEqualTo(true)
    }
}