package com.microsoft.walletlibrary.requests.resolvers

import com.microsoft.walletlibrary.networking.operations.FetchOpenID4VCIRequestNetworkOperation
import com.microsoft.walletlibrary.requests.input.VerifiedIdRequestInput
import com.microsoft.walletlibrary.requests.input.VerifiedIdRequestURL
import com.microsoft.walletlibrary.requests.rawrequests.OpenIdRawRequest
import com.microsoft.walletlibrary.util.LibraryConfiguration
import com.microsoft.walletlibrary.util.UnSupportedVerifiedIdRequestInputException
import com.microsoft.walletlibrary.wrapper.OpenIdResolver
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class OpenIdURLRequestResolverTest {
    private val mockLibraryConfiguration = mockk<LibraryConfiguration>()
    private val openIdURLRequestResolver = OpenIdURLRequestResolver(mockLibraryConfiguration)
    private lateinit var mockVerifiedIdRequestInput: VerifiedIdRequestInput
    private lateinit var mockVerifiedIdRequestURL: VerifiedIdRequestURL

    init {
        mockkConstructor(FetchOpenID4VCIRequestNetworkOperation::class)
    }

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
    fun resolve_ResolveInput_ThrowsUnSupportedVerifiedIdRequestInputException() {
        // Arrange
        createMockVerifiedIdRequestInput()

        // Act and Assert
        Assertions.assertThatThrownBy {
            runBlocking {
                openIdURLRequestResolver.resolve(mockVerifiedIdRequestInput)
            }
        }.isInstanceOf(UnSupportedVerifiedIdRequestInputException::class.java)
    }

    @Test
    fun resolve_validURL_ReturnsRawRequest() {
        // Arrange
        mockVerifiedIdRequestURL = mockk()
        every { mockVerifiedIdRequestURL.url.scheme } returns "openid-vc"
        every { mockLibraryConfiguration.isPreviewFeatureEnabled(any()) } returns false
        mockkObject(OpenIdResolver)
        coEvery { OpenIdResolver.getRequest(any()) } returns mockk()

        runBlocking {
            // Act
            val actualResult = openIdURLRequestResolver.resolve(mockVerifiedIdRequestURL)

            // Assert
            assertThat(actualResult).isInstanceOf(OpenIdRawRequest::class.java)
        }
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