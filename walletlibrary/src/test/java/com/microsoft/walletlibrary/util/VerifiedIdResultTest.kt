package com.microsoft.walletlibrary.util

import com.microsoft.did.sdk.util.controlflow.NetworkException
import com.microsoft.did.sdk.util.controlflow.SdkException
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class VerifiedIdResultTest {

    @Test
    fun getResult_blockSucceeds_returnsSuccess() {
        // Arrange
        val block = { "success" }

        // Act
        runBlocking {
            val result = getResult(block)

            // Assert
            assertThat(result.isSuccess).isTrue
            assertThat(result.getOrNull() == "success").isTrue
        }
    }

    @Test
    fun getResult_blockThrowsRequirementNotMetException_returnsVerifiedIdResultFailureWithRequirementNotMetException() {
        // Arrange
        val block = {
            throw RequirementNotMetException(
                "Testing",
                VerifiedIdExceptions.REQUIREMENT_NOT_MET_EXCEPTION.value
            )
        }

        // Act
        runBlocking {
            val result = getResult(block)

            // Assert
            assertThat(result.isFailure).isTrue
            assertThat(result.exceptionOrNull() is VerifiedIdException).isTrue
        }
    }

    @Test
    fun getResult_blockThrowsWalletLibraryExceptionCausedByNetworkException_returnsVerifiedIdResultFailureWithNetworkingException() {
        // Arrange
        val mockNetworkException: NetworkException = mockk()
        every { mockNetworkException.correlationVector } returns "abcd.1"
        every { mockNetworkException.errorCode } returns "500"
        every { mockNetworkException.innerErrorCodes } returns "500"
        every { mockNetworkException.errorBody } returns "errorBody"
        every { mockNetworkException.retryable } returns false

        val block = {
            throw WalletLibraryException(
                "Testing",
                mockNetworkException
            )
        }

        // Act
        runBlocking {
            val result = getResult(block)

            // Assert
            assertThat(result.isFailure).isTrue
            assertThat(result.exceptionOrNull() is NetworkingException).isTrue
            assertThat((result.exceptionOrNull() as NetworkingException).cause).isEqualTo(
                mockNetworkException
            )
        }
    }

    @Test
    fun getResult_blockThrowsWalletLibraryExceptionCausedBySdkException_returnsVerifiedIdResultFailureWithMalformedInputException() {
        // Arrange
        val mockSdkException: SdkException = mockk()

        val block = {
            throw WalletLibraryException(
                "Testing",
                mockSdkException
            )
        }

        // Act
        runBlocking {
            val result = getResult(block)

            // Assert
            assertThat(result.isFailure).isTrue
            assertThat(result.exceptionOrNull() is MalformedInputException).isTrue
            assertThat((result.exceptionOrNull() as MalformedInputException).cause).isEqualTo(
                mockSdkException
            )
        }
    }

    @Test
    fun getResult_blockThrowsWalletLibraryExceptionCausedByOtherException_returnsVerifiedIdResultFailureWithUnspecifiedVerifiedIdException() {
        // Arrange
        val mockException: Exception = mockk()

        val block = {
            throw WalletLibraryException(
                "Testing",
                mockException
            )
        }

        // Act
        runBlocking {
            val result = getResult(block)

            // Assert
            assertThat(result.isFailure).isTrue
            assertThat(result.exceptionOrNull() is UnspecifiedVerifiedIdException).isTrue
            assertThat((result.exceptionOrNull() as UnspecifiedVerifiedIdException).cause).isEqualTo(
                mockException
            )
        }
    }

    @Test
    fun getResult_blockThrowsOtherException_returnsVerifiedIdResultFailureWithUnspecifiedVerifiedIdException() {
        // Arrange
        val mockException: Exception = mockk()
        every { mockException.message } returns "Testing"

        val block = {
            throw mockException
        }

        // Act
        runBlocking {
            val result = getResult(block)

            // Assert
            assertThat(result.isFailure).isTrue
            assertThat(result.exceptionOrNull() is UnspecifiedVerifiedIdException).isTrue
            assertThat((result.exceptionOrNull() as UnspecifiedVerifiedIdException).cause).isEqualTo(
                mockException
            )
        }
    }
}