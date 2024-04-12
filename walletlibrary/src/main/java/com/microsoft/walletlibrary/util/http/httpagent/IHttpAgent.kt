package com.microsoft.walletlibrary.util.http.httpagent

import com.microsoft.walletlibrary.did.sdk.util.Constants
import com.microsoft.walletlibrary.interceptor.CommonHeaders
import com.microsoft.walletlibrary.util.NetworkingException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

abstract class IHttpAgent {
    open class HttpAgentException(val response: IResponse): Exception() {
        private fun stringifyInnerErrorCodes(firstInnerError: JsonObject): String? {
            var innerError: String? = null
            var errorObject = firstInnerError
            innerError = errorObject["code"]?.jsonPrimitive?.content
            while (errorObject.containsKey("innererror")) {
                errorObject["innererror"]?.jsonObject?.let {
                    errorObject = it
                    innerError += "," + errorObject["code"]?.jsonPrimitive?.content
                }
            }
            return innerError
        }

        fun toNetworkException(): NetworkingException {
            var message = String(response.body)
            var code = response.status.toString()
            val correlationVector: String? = response.headers[Constants.CORRELATION_VECTOR_HEADER]
            var innerError: String? = null
            val retriable = response.headers.containsKey(CommonHeaders.RetryAfter.key)
            try {
                val response = Json.decodeFromString(JsonObject.serializer(), message)
                val error = response["error"]?.jsonObject
                code = error?.get("code")?.jsonPrimitive?.content ?: code
                message = error?.get("message")?.jsonPrimitive?.content ?: message
                error?.get("innererror")?.jsonObject?.let {
                    firstInnerError ->
                    innerError = stringifyInnerErrorCodes(firstInnerError)
                }
            } catch (_: Throwable) {
                // best effort to parse additional info
            }

            return NetworkingException(
                message,
                code,
                correlationVector,
                response.status.toString(),
                innerError,
                response.body.toString(),
                retriable
            )
        }
    }

    class ClientException(response: IResponse): HttpAgentException(response)
    class ServerException(response: IResponse): HttpAgentException(response)

    abstract suspend fun get(url: String, headers: Map<String, String>): Result<IResponse>

    abstract suspend fun post(url: String, headers: Map<String, String>, payload: ByteArray): Result<IResponse>
}