package com.microsoft.walletlibrary.util.http.httpagent

import com.google.common.net.HttpHeaders
import com.microsoft.walletlibrary.util.NetworkingException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import com.microsoft.walletlibrary.did.sdk.util.Constants

class IResponse(
    val status: Int,
    val headers: Map<String, String>,
    val body: ByteArray
) {
    fun toNetworkingException(): NetworkingException {
        var code = status.toString()
        var innerError: String? = null
        var message = ""
        var correlationId: String? = headers["ms-cv"]
        var bodyAsString: String? = null
        val retryable: Boolean = headers.containsKey(HttpHeaders.RETRY_AFTER)
        try {
            bodyAsString = body.decodeToString()
            val json = Json.decodeFromString<JsonObject>(bodyAsString)
            if (correlationId == null) {
                correlationId = (json[Constants.CORRELATION_VECTOR_HEADER] as? JsonPrimitive)?.content
            }
            // Attempt to parse assuming https://github.com/microsoft/api-guidelines/blob/vNext/azure/Guidelines.md#handling-errors
            var error = (json["error"] as? JsonObject)
            var outerMostError = true
            while (error != null) {
                val errorCode = (error["code"] as? JsonPrimitive)?.content
                if (errorCode != null) {
                    if (outerMostError) {
                        outerMostError = false
                        code = errorCode
                    } else if (innerError == null) {
                        innerError = errorCode
                    } else {
                        innerError += ",$innerError"
                    }
                }
                val errorMessage = (error["message"] as? JsonPrimitive)?.content
                if (errorMessage != null) {
                    if (message.isEmpty()) {
                        message = errorMessage
                    } else {
                        message += ". $errorMessage"
                    }
                }
                error = (error?.get("innererror") as? JsonObject)
            }

        } catch (e: Exception) {
            // we've handled as best as we could
        }
        return NetworkingException(
            message = message,
            code = code,
            correlationId = correlationId,
            statusCode = status.toString(),
            innerError = innerError,
            errorBody = bodyAsString,
            retryable = retryable
        )
    }
}