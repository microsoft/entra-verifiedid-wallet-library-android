package com.microsoft.walletlibrary.util

import com.microsoft.walletlibrary.did.sdk.util.controlflow.SdkException
import com.microsoft.walletlibrary.did.sdk.util.controlflow.IssuanceResponseIssuerBindingException
import com.microsoft.walletlibrary.did.sdk.util.controlflow.IssuanceResponseIssuerBindingFailure
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
        val mockNetworkException = NetworkingException(
            "",
            VerifiedIdExceptions.NETWORKING_EXCEPTION.value,
            "abcd.1",
            "500",
            "badRequest",
            "errorBody",
            false
        )

        val block = {
            throw mockNetworkException
        }

        // Act
        runBlocking {
            val result = getResult(block)

            // Assert
            assertThat(result.isFailure).isTrue
            val exception = result.exceptionOrNull()
            assertThat(exception is NetworkingException).isTrue
            assertThat(exception).isEqualTo(mockNetworkException)
        }
    }

    @Test
    fun getResult_blockThrowsWalletLibraryExceptionCausedBySdkException_returnsVerifiedIdResultFailureWithMalformedInputException() {
        // Arrange
        val sdkException = SdkException("Testing")

        val block = {
            throw WalletLibraryException(
                "Testing",
                sdkException
            )
        }

        // Act
        runBlocking {
            val result = getResult(block)

            // Assert
            assertThat(result.isFailure).isTrue
            assertThat(result.exceptionOrNull() is MalformedInputException).isTrue
            assertThat((result.exceptionOrNull() as MalformedInputException).cause).isEqualTo(
                sdkException
            )
        }
    }

    @Test
    fun getResult_blockThrowsSignerDidMismatch_returnsCategorizedMalformedInputException() {
        val sdkException = IssuanceResponseIssuerBindingException(
            "signer mismatch",
            IssuanceResponseIssuerBindingFailure.SignerDidMismatch
        )
        val nestedException = WalletLibraryException("network operation failed", sdkException)

        val result = runBlocking {
            getResult<String> { throw WalletLibraryException("Testing", nestedException) }
        }

        assertThat(result.exceptionOrNull())
            .isInstanceOf(MalformedInputException::class.java)
            .extracting("code")
            .isEqualTo(VerifiedIdExceptions.ISSUANCE_RESPONSE_SIGNER_DID_MISMATCH.value)
    }

    @Test
    fun getResult_blockThrowsPayloadIssuerMismatch_returnsCategorizedMalformedInputException() {
        val sdkException = IssuanceResponseIssuerBindingException(
            "payload mismatch",
            IssuanceResponseIssuerBindingFailure.PayloadIssuerMismatch
        )

        val result = runBlocking {
            getResult<String> { throw WalletLibraryException("Testing", sdkException) }
        }

        assertThat(result.exceptionOrNull())
            .isInstanceOf(MalformedInputException::class.java)
            .extracting("code")
            .isEqualTo(VerifiedIdExceptions.ISSUANCE_RESPONSE_PAYLOAD_ISSUER_MISMATCH.value)
    }

    @Test
    fun getResult_blockThrowsWalletLibraryExceptionCausedByOtherException_returnsVerifiedIdResultFailureWithUnspecifiedVerifiedIdException() {
        // Arrange
        val exception = Exception("Testing")

        val block = {
            throw WalletLibraryException(
                "Testing",
                exception
            )
        }

        // Act
        runBlocking {
            val result = getResult(block)

            // Assert
            assertThat(result.isFailure).isTrue
            assertThat(result.exceptionOrNull() is UnspecifiedVerifiedIdException).isTrue
            assertThat((result.exceptionOrNull() as UnspecifiedVerifiedIdException).cause).isEqualTo(
                exception
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