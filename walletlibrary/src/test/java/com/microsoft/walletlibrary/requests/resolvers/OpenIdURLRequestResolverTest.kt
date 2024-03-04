package com.microsoft.walletlibrary.requests.resolvers

import com.microsoft.walletlibrary.requests.input.VerifiedIdRequestInput
import com.microsoft.walletlibrary.requests.input.VerifiedIdRequestURL
import com.microsoft.walletlibrary.util.UnSupportedVerifiedIdRequestInputException
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class OpenIdURLRequestResolverTest {
    private val openIdURLRequestResolver = OpenIdURLRequestResolver(mockk())
    private lateinit var mockVerifiedIdRequestInput: VerifiedIdRequestInput
    private lateinit var mockVerifiedIdRequestURL: VerifiedIdRequestURL

    @Test
    fun resolver_CanResolveInput_ReturnsTrue() {
        // Arrange
        mockVerifiedIdRequestURL()
        every { mockVerifiedIdRequestURL.url.scheme } returns "openid-vc"

        // Act
        val actualResult = openIdURLRequestResolver.canResolve(mockVerifiedIdRequestURL)

        // Assert
        assertThat(actualResult).isEqualTo(true)
    }

    @Test
    fun resolver_CanResolveInput_ReturnsFalse() {
        // Arrange
        createMockVerifiedIdRequestInput()

        // Act
        val actualResult = openIdURLRequestResolver.canResolve(mockVerifiedIdRequestInput)

        // Assert
        assertThat(actualResult).isEqualTo(false)
    }

    @Test
    fun resolver_ResolveInput_ThrowsUnSupportedVerifiedIdRequestInputException() {
        // Arrange
        createMockVerifiedIdRequestInput()

        // Act and Assert
        Assertions.assertThatThrownBy {
            runBlocking {
                openIdURLRequestResolver.resolve(mockVerifiedIdRequestInput)
            }
        }.isInstanceOf(UnSupportedVerifiedIdRequestInputException::class.java)
    }

    private fun createMockVerifiedIdRequestInput() {
        class MockVerifiedIdRequestInput : VerifiedIdRequestInput
        mockVerifiedIdRequestInput = MockVerifiedIdRequestInput()
    }

    private fun mockVerifiedIdRequestURL() {
        mockVerifiedIdRequestURL = mockk()
        every { mockVerifiedIdRequestURL.url.toString() } returns ""
    }
}