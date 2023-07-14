package com.microsoft.walletlibrary.util

typealias VerifiedIdResult<T> = Result<T>

internal suspend fun <T> getResult(block: suspend () -> T): VerifiedIdResult<T> {
    return try {
        val result = block()
        VerifiedIdResult.success(result)
    } catch (verifiedIdException: VerifiedIdException) {
        VerifiedIdResult.failure(verifiedIdException)
    } catch (exception: Exception) {
        VerifiedIdResult.failure(
            UnspecifiedVerifiedIdException(
                exception.message ?: "",
                "UnspecifiedVerifiedIdException",
                exception
            )
        )
    }
}