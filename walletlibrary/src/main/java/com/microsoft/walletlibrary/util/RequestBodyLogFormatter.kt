package com.microsoft.walletlibrary.util

import com.microsoft.walletlibrary.requests.rawrequests.RequestType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

internal object RequestBodyLogFormatter {
    private const val REDACTED = "[REDACTED]"

    private val safeValueKeys = setOf(
        "alg",
        "client_name",
        "credential_configuration_ids",
        "facesdk",
        "limit_disclosure",
        "path",
        "prompt",
        "required",
        "response_mode",
        "response_type",
        "scope",
        "subject_syntax_types_supported",
        "type",
        "uri"
    )

    fun format(requestType: RequestType, requestBody: Map<String, Any>): String {
        val flowName = when (requestType) {
            RequestType.ISSUANCE -> "Issuance"
            RequestType.PRESENTATION -> "Presentation"
        }
        return "$flowName request body received: ${Json.encodeToString(sanitize(requestBody))}"
    }

    private fun sanitize(value: Any?, parentKey: String? = null): JsonElement = when (value) {
        null -> JsonNull
        is Map<*, *> -> JsonObject(
            value.entries.associate { (key, nestedValue) ->
                val keyString = key.toString()
                keyString to sanitize(nestedValue, keyString)
            }
        )
        is Iterable<*> -> JsonArray(value.map { sanitize(it, parentKey) })
        is Array<*> -> JsonArray(value.map { sanitize(it, parentKey) })
        is Boolean -> if (isSafeValueKey(parentKey)) JsonPrimitive(value) else JsonPrimitive(REDACTED)
        is Number -> if (isSafeValueKey(parentKey)) JsonPrimitive(value) else JsonPrimitive(REDACTED)
        is String -> if (isSafeValueKey(parentKey)) JsonPrimitive(value) else JsonPrimitive(REDACTED)
        else -> JsonPrimitive(REDACTED)
    }

    private fun isSafeValueKey(key: String?): Boolean = key?.lowercase() in safeValueKeys
}