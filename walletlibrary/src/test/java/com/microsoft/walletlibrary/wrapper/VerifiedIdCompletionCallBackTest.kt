package com.microsoft.walletlibrary.wrapper

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.microsoft.did.sdk.IssuanceService
import com.microsoft.did.sdk.VerifiableCredentialSdk
import com.microsoft.did.sdk.credential.service.models.issuancecallback.IssuanceCompletionResponse
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.SdkException
import com.microsoft.walletlibrary.util.VerifiedIdIssuanceCompletionCallbackException
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.junit.Test

class VerifiedIdCompletionCallBackTest {
    private val mockIssuanceService: IssuanceService = mockk()
    private val mockIssuanceCompletionResponse: IssuanceCompletionResponse = mockk()
    private val issuanceCallbackUrl = "test.com"

    init {
        setupInput()
    }

    private fun setupInput() {
        mockkStatic(VerifiableCredentialSdk::class)
        every { VerifiableCredentialSdk.issuanceService } returns mockIssuanceService
    }

    @Test
    fun sendIssuanceCallback_SuccessFromVcSDK_SuccessfulInWalletLibrary() {
        // Arrange
        coEvery {
            mockIssuanceService.sendCompletionResponse(
                mockIssuanceCompletionResponse,
                issuanceCallbackUrl
            )
        } returns Result.Success(Unit)

        runBlocking {
            // Act
            val actualResult = VerifiedIdCompletionCallBack.sendIssuanceCompletionResponse(
                mockIssuanceCompletionResponse,
                issuanceCallbackUrl
            )

            // Assert
            assertThat(actualResult).isEqualTo(Unit)
        }
    }

    @Test
    fun sendIssuanceCallback_FailureFromVcSDK_ThrowsException() {
        // Arrange
        coEvery {
            mockIssuanceService.sendCompletionResponse(
                mockIssuanceCompletionResponse,
                issuanceCallbackUrl
            )
        } returns Result.Failure(SdkException("Test failure"))

        // Act and Assert
        Assertions.assertThatThrownBy {
            runBlocking {
                VerifiedIdCompletionCallBack.sendIssuanceCompletionResponse(
                    mockIssuanceCompletionResponse,
                    issuanceCallbackUrl
                )
            }
        }.isInstanceOf(VerifiedIdIssuanceCompletionCallbackException::class.java)
    }
}