package com.microsoft.walletlibrary.util

import com.microsoft.walletlibrary.did.sdk.util.controlflow.NetworkException
import com.microsoft.walletlibrary.did.sdk.util.controlflow.SdkException

typealias VerifiedIdResult<T> = Result<T>

internal suspend fun <T> getResult(block: suspend () -> T): VerifiedIdResult<T> {
    return try {
        val result = block()
        VerifiedIdResult.success(result)
    } catch (verifiedIdException: VerifiedIdException) {
        verifiedIdException.toVerifiedIdResult()
    } catch (exception: WalletLibraryException) {
        when (val innerException = exception.cause) {
            is NetworkException -> {
                val networkingException = NetworkingException(
                    exception.message ?: "",
                    VerifiedIdExceptions.NETWORKING_EXCEPTION.value,
                    innerException.correlationVector,
                    innerException.errorCode,
                    innerException.innerErrorCodes,
                    innerException.errorBody,
                    innerException.retryable
                )
                networkingException.cause = innerException
                networkingException.toVerifiedIdResult()
            }
            is SdkException -> {
                val malformedInputException = MalformedInputException(
                    exception.message ?: "",
                    VerifiedIdExceptions.MALFORMED_INPUT_EXCEPTION.value,
                    exception
                )
                malformedInputException.cause = exception.cause
                malformedInputException.toVerifiedIdResult()
            }
            else -> {
                val unspecifiedVerifiedIdException = UnspecifiedVerifiedIdException(
                    exception.message ?: "",
                    VerifiedIdExceptions.UNSPECIFIED_EXCEPTION.value,
                    exception
                )
                unspecifiedVerifiedIdException.cause = exception.cause
                unspecifiedVerifiedIdException.toVerifiedIdResult()
            }
        }
    } catch (exception: Exception) {
        val unspecifiedVerifiedIdException = UnspecifiedVerifiedIdException(
            exception.message ?: "",
            VerifiedIdExceptions.UNSPECIFIED_EXCEPTION.value,
            exception
        )
        unspecifiedVerifiedIdException.cause = exception
        unspecifiedVerifiedIdException.toVerifiedIdResult()
    }
}