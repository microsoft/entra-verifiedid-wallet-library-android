package com.microsoft.walletlibrary.util

sealed class VerifiedIdException(message: String, val code: String, val correlationId: String? = null): Exception(message) {
    fun <T>toVerifiedIdResult(): VerifiedIdResult<T> {
        return VerifiedIdResult.failure(this)
    }
}
class NetworkingException(
    message: String,
    code: String,
    correlationId: String? = null,
    statusCode: String? = null,
    innerError: Exception? = null,
    retryable: Boolean = false
) : VerifiedIdException(message, code, correlationId)

class RequirementNotMetException(message: String, code: String, val innerErrors: List<Exception>? = null, correlationId: String? = null) :
    VerifiedIdException(message, code, correlationId)

class MalformedInputException(message: String, code: String, val innerError: Exception?= null, correlationId: String? = null) :
    VerifiedIdException(message, code, correlationId)

class UserCanceledException(message: String, code: String, correlationId: String? = null) :
    VerifiedIdException(message, code, correlationId)

class UnspecifiedVerifiedIdException(message: String, code: String, val innerError: Exception? = null, correlationId: String? = null) :
    VerifiedIdException(message, code, correlationId)