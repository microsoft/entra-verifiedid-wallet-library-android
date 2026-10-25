package com.microsoft.walletlibrary.util

import com.microsoft.walletlibrary.did.sdk.util.controlflow.SdkException
import com.microsoft.walletlibrary.did.sdk.util.controlflow.IssuanceResponseIssuerBindingException
import com.microsoft.walletlibrary.did.sdk.util.controlflow.IssuanceResponseIssuerBindingFailure
import kotlinx.coroutines.CancellationException

typealias VerifiedIdResult<T> = Result<T>

internal suspend fun <T> getResult(block: suspend () -> T): VerifiedIdResult<T> {
    return try {
        val result = block()
        VerifiedIdResult.success(result)
    } catch (verifiedIdException: VerifiedIdException) {
        verifiedIdException.toVerifiedIdResult()
    } catch (exception: WalletLibraryException) {
        val issuerBindingException = generateSequence(exception.cause) { it.cause }
            .filterIsInstance<IssuanceResponseIssuerBindingException>()
            .firstOrNull()
        if (issuerBindingException != null) {
            val code = when (issuerBindingException.failure) {
                    IssuanceResponseIssuerBindingFailure.SignerDidMismatch ->
                        VerifiedIdExceptions.ISSUANCE_RESPONSE_SIGNER_DID_MISMATCH.value
                    IssuanceResponseIssuerBindingFailure.PayloadIssuerMismatch ->
                        VerifiedIdExceptions.ISSUANCE_RESPONSE_PAYLOAD_ISSUER_MISMATCH.value
            }
            val malformedInputException = MalformedInputException(
                exception.message ?: "",
                code,
                exception
            )
            malformedInputException.cause = issuerBindingException
            malformedInputException.toVerifiedIdResult()
        } else {
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