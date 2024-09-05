package com.microsoft.walletlibrary.requests.resolvers

import com.microsoft.walletlibrary.did.sdk.identifier.resolvers.RootOfTrustResolver
import com.microsoft.walletlibrary.requests.VerifiedIdRequest
import com.microsoft.walletlibrary.requests.handlers.OpenIdRequestProcessor
import com.microsoft.walletlibrary.requests.handlers.RequestProcessor
import com.microsoft.walletlibrary.requests.input.VerifiedIdRequestInput
import com.microsoft.walletlibrary.requests.input.VerifiedIdRequestURL
import com.microsoft.walletlibrary.requests.rawrequests.RawRequest
import com.microsoft.walletlibrary.networking.operations.FetchOpenID4VCIRequestNetworkOperation
import com.microsoft.walletlibrary.requests.rawrequests.OpenIdRawRequest
import com.microsoft.walletlibrary.requests.rawrequests.OpenIdProcessedRequest
import com.microsoft.walletlibrary.requests.requestProcessorExtensions.RequestProcessorExtension
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
    private val openIdURLRequestResolver = OpenIdURLRequestResolver(mockLibraryConfiguration, emptyList())
    private lateinit var mockVerifiedIdRequestInput: VerifiedIdRequestInput
    private lateinit var mockVerifiedIdRequestURL: VerifiedIdRequestURL

    init {
        mockkConstructor(FetchOpenID4VCIRequestNetworkOperation::class)
    }

    @Test
    fun resolver_CanResolveHandler_ReturnsTrue() {
        // Arrange
        val mockOpenIdRequestHandler: OpenIdRequestProcessor = mockk()

        // Act
        val actualResult = openIdURLRequestResolver.canResolve(mockVerifiedIdRequestInput)

        // Assert
        assertThat(actualResult).isEqualTo(true)
    }

    @Test
    fun resolver_CanResolveHandler_ReturnsFalse() {
        // Arrange
        class MockRequestProcessor(override var requestProcessors: MutableList<RequestProcessorExtension<OpenIdRawRequest>>) : RequestProcessor<OpenIdRawRequest> {

            override suspend fun handleRequest(rawRequest: Any, rootOfTrustResolver: RootOfTrustResolver?): VerifiedIdRequest<*> {
                return mockk()
            }

            override suspend fun canHandleRequest(rawRequest: Any): Boolean {
                return mockk()
            }

        }

        val mockRequestHandler = MockRequestProcessor(mockk())

        // Act
        val actualResult = openIdURLRequestResolver.canResolve(mockVerifiedIdRequestInput)

        // Assert
        assertThat(actualResult).isEqualTo(false)
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
        coEvery { OpenIdResolver.getRequest(any(), null, emptyList()) } returns mockk()

        runBlocking {
            // Act
            val actualResult = openIdURLRequestResolver.resolve(mockVerifiedIdRequestURL)

            // Assert
            assertThat(actualResult).isInstanceOf(OpenIdProcessedRequest::class.java)
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