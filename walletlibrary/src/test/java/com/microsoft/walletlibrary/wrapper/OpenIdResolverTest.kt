package com.microsoft.walletlibrary.wrapper

import com.microsoft.walletlibrary.did.sdk.PresentationService
import com.microsoft.walletlibrary.did.sdk.VerifiableCredentialSdk
import com.microsoft.walletlibrary.did.sdk.credential.service.PresentationRequest
import com.microsoft.walletlibrary.did.sdk.credential.service.models.oidc.PresentationRequestContent
import com.microsoft.walletlibrary.did.sdk.util.controlflow.Result
import com.microsoft.walletlibrary.did.sdk.util.controlflow.SdkException
import com.microsoft.walletlibrary.requests.rawrequests.OpenIdRawRequest
import com.microsoft.walletlibrary.requests.rawrequests.RequestType
import com.microsoft.walletlibrary.requests.rawrequests.VerifiedIdOpenIdJwtRawRequest
import com.microsoft.walletlibrary.util.VerifiedIdRequestFetchException
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class OpenIdResolverTest {
    private val mockPresentationService: PresentationService = mockk()
    private val openIdUrl = ""
    private val mockPresentationRequest: PresentationRequest = mockk()
    private val mockPresentationRequestContent: PresentationRequestContent = mockk()
    private val mockOpenIdRawRequest: OpenIdRawRequest = mockk()

    init {
        setupInput(false)
    }

    private fun setupInput(isFailure: Boolean) {
        mockkStatic(VerifiableCredentialSdk::class)
        every { VerifiableCredentialSdk.presentationService } returns mockPresentationService
        if (!isFailure) {
            coEvery { mockPresentationService.getRequest(openIdUrl, null, emptyList()) } returns Result.Success(Pair(mockPresentationRequest, mockOpenIdRawRequest))
            coEvery { mockPresentationService.validateRequest(mockPresentationRequestContent) } returns Result.Success(mockPresentationRequest)
            every { mockPresentationRequest.content } returns mockPresentationRequestContent
            every { mockPresentationRequestContent.prompt } returns ""
        } else {
            coEvery { mockPresentationService.getRequest(openIdUrl, null, emptyList()) } returns Result.Failure(
                SdkException()
            )
            coEvery { mockPresentationService.validateRequest(mockPresentationRequestContent) } returns Result.Failure(SdkException())
        }
    }

    @Test
    fun resolveOpenIdRequest_SuccessfulPresentationRequestFromSdk_ReturnsRawRequestOfTypePresentation() {
        runBlocking {
            // Act
            val actualResult = OpenIdResolver.getRequest(openIdUrl, null, emptyList())

            // Assert
            assertThat(actualResult).isInstanceOf(VerifiedIdOpenIdJwtRawRequest::class.java)
            assertThat(actualResult.requestType).isEqualTo(RequestType.PRESENTATION)
            assertThat(actualResult.rawRequest).isEqualTo(mockPresentationRequest)
        }
    }

    @Test
    fun resolveOpenIdRequest_ValidateRequestSuccessful_ReturnsRawRequestOfTypePresentation() {
        runBlocking {
            // Act
            val actualResult = OpenIdResolver.validateRequest(mockPresentationRequestContent, emptyMap())

            // Assert
            assertThat(actualResult).isInstanceOf(VerifiedIdOpenIdJwtRawRequest::class.java)
            assertThat(actualResult.requestType).isEqualTo(RequestType.PRESENTATION)
            assertThat(actualResult.rawRequest).isEqualTo(mockPresentationRequest)
        }
    }

    @Test
    fun resolveOpenIdRequest_ValidateRequestFailure_ThrowsException() {
        // Arrange
        setupInput(true)

        // Act and Assert
        Assertions.assertThatThrownBy {
            runBlocking {
                OpenIdResolver.validateRequest(mockPresentationRequestContent, emptyMap())
            }
        }.isInstanceOf(VerifiedIdRequestFetchException::class.java)
    }

    @Test
    fun resolveOpenIdRequest_FailureFromSdk_ThrowsException() {
        // Arrange
        setupInput(true)

        // Act and Assert
        Assertions.assertThatThrownBy {
            runBlocking {
                OpenIdResolver.getRequest(openIdUrl, null, emptyList())
            }
        }.isInstanceOf(VerifiedIdRequestFetchException::class.java)
    }
}