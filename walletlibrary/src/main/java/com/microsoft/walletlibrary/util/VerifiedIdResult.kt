package com.microsoft.walletlibrary.util

import com.microsoft.walletlibrary.did.sdk.util.controlflow.SdkException
import kotlinx.coroutines.CancellationException

typealias VerifiedIdResult<T> = Result<T>

internal suspend fun <T> getResult(block: suspend () -> T): VerifiedIdResult<T> {
    return try {
        val result = block()
        VerifiedIdResult.success(result)
    } catch (verifiedIdException: VerifiedIdException) {
        verifiedIdException.toVerifiedIdResult()
    } catch (exception: WalletLibraryException) {
        when (val innerException = exception.cause) {
            is SdkException -> {
                val malformedInputException = MalformedInputException(
                    exception.message ?: "",
                    VerifiedIdExceptions.MALFORMED_INPUT_EXCEPTION.value,
                    exception
                )
                malformedInputException.cause = innerException
                malformedInputException.toVerifiedIdResult()
            }
            else -> {
                val unspecifiedVerifiedIdException = UnspecifiedVerifiedIdException(
                    exception.message ?: "",
                    VerifiedIdExceptions.UNSPECIFIED_EXCEPTION.value,
                    exception
                )
                unspecifiedVerifiedIdException.cause = innerException
                unspecifiedVerifiedIdException.toVerifiedIdResult()
            }
        }
    } catch (exception: CancellationException) {
        throw exception
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