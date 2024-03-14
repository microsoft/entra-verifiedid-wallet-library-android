package com.microsoft.walletlibrary.util

sealed class VerifiedIdException(
    message: String,
    val code: String,
    val correlationId: String? = null,
    override var cause: Throwable? = null
) : Exception(message, cause) {
    fun <T> toVerifiedIdResult(): VerifiedIdResult<T> {
        return VerifiedIdResult.failure(this)
    }
}

class NetworkingException(
    message: String,
    code: String,
    correlationId: String? = null,
    val statusCode: String? = null,
    val innerError: String? = null,
    val errorBody: String? = null,
    val retryable: Boolean = false
) : VerifiedIdException(message, code, correlationId)

class RequirementNotMetException(
    message: String,
    code: String,
    val innerErrors: List<Exception>? = null,
    correlationId: String? = null
) : VerifiedIdException(message, code, correlationId)

class MalformedInputException(
    message: String,
    code: String,
    val innerError: Exception? = null,
    correlationId: String? = null
) : VerifiedIdException(message, code, correlationId)

class UserCanceledException(message: String, code: String, correlationId: String? = null) :
    VerifiedIdException(message, code, correlationId)

class UnspecifiedVerifiedIdException(
    message: String,
    code: String,
    val innerError: Exception? = null,
    correlationId: String? = null
) : VerifiedIdException(message, code, correlationId)

class OpenId4VciException(
    message: String,
    code: String,
    val innerError: Exception? = null,
    correlationId: String? = null
) : VerifiedIdException(message, code, correlationId)