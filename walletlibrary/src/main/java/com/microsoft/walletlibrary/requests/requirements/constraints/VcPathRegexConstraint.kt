package com.microsoft.walletlibrary.requests.requirements.constraints

import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.Option
import com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredentialContent
import com.microsoft.walletlibrary.did.sdk.util.log.SdkLog
import com.microsoft.walletlibrary.util.NoMatchForVcPathRegexConstraintException
import com.microsoft.walletlibrary.verifiedid.VerifiableCredential
import com.microsoft.walletlibrary.verifiedid.VerifiableCredentialSerializer
import com.microsoft.walletlibrary.verifiedid.VerifiedId
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.regex.Pattern

internal class VcPathRegexConstraint(
    internal val path: List<String>,
    internal val pattern: String
) : VerifiedIdConstraint {
    override fun doesMatch(verifiedId: VerifiedId): Boolean {
//        if (verifiedId !is VerifiableCredential) return false
        val encoded = Json.encodeToString(verifiedId)
        val verifiableCredential = Json.decodeFromString(VerifiableCredentialSerializer, encoded) as VerifiableCredential
        val verifiableCredentialJsonString =
            Json.encodeToString(VerifiableCredentialContent.serializer(), verifiableCredential.raw.contents)
        return path.any { matchAnyPathInFields(pattern, it, verifiableCredentialJsonString) }
    }

    private fun matchAnyPathInFields(
        pattern: String,
        path: String,
        verifiableCredentialJsonString: String
    ): Boolean {
        if (pattern.isEmpty()) SdkLog.i("Empty pattern in filter.")
        val configuration =
            Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS)
        val constraintValue: String? =
            JsonPath.using(configuration).parse(verifiableCredentialJsonString).read(path)
        return if (constraintValue != null) {
            matchPattern(pattern, constraintValue)
        } else false
    }

    internal fun matchPattern(pattern: String, value: String): Boolean {
        val sanitizedPattern = sanitizePattern(pattern)
        return Pattern.compile(sanitizedPattern, Pattern.CASE_INSENSITIVE).matcher(value).find()
    }

    private fun sanitizePattern(pattern: String): String {
        return if (pattern.isNotEmpty() && pattern.length > 1) {
            val escapeRegex = ".*(/[gi]*)"
            val flagsToEscape = Regex(escapeRegex).find(pattern)?.groups?.last()?.value ?: ""
            pattern.substring(1, pattern.length - flagsToEscape.length)
        } else ""
    }

    override fun matches(verifiedId: VerifiedId) {
        if (!doesMatch(verifiedId)) throw NoMatchForVcPathRegexConstraintException(
            "Provided Verified Id claim doesn't match."
        )
    }
}