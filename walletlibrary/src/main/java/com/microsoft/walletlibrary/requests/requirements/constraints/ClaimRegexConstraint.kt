package com.microsoft.walletlibrary.requests.requirements.constraints

import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.Option
import com.microsoft.did.sdk.credential.models.VerifiableCredentialContent
import com.microsoft.did.sdk.util.log.SdkLog
import com.microsoft.walletlibrary.util.VerifiedIdIssuerIsNotRequestedException
import com.microsoft.walletlibrary.verifiedid.VerifiableCredential
import com.microsoft.walletlibrary.verifiedid.VerifiedId
import kotlinx.serialization.json.Json
import java.util.regex.Pattern

class ClaimRegexConstraint(internal val path: List<String>, internal val pattern: String): VerifiedIdConstraint {
    override fun doesMatch(verifiedId: VerifiedId): Boolean {
        if (verifiedId !is VerifiableCredential)
            return false
        val vccJsonString = Json.encodeToString(VerifiableCredentialContent.serializer(), verifiedId.raw.contents)
        return path.any { matchAnyPathInFields(pattern, it, vccJsonString) }
    }

    private fun matchAnyPathInFields(pattern: String, path: String, vccJson: String): Boolean {
        if (pattern.isEmpty())
            SdkLog.d("Empty pattern in filter.")
        val configuration = Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS)
        val constraintValue: String? = JsonPath.using(configuration).parse(vccJson).read(path)
        return if (constraintValue != null) {
            matchPattern(pattern, constraintValue)
        } else
            false
    }

    internal fun matchPattern(pattern: String, value: String): Boolean {
        val sanitizedPattern = sanitizePattern(pattern)
        return Pattern.compile(sanitizedPattern, Pattern.CASE_INSENSITIVE).matcher(value).find()
    }

    private fun sanitizePattern(pattern: String): String {
        return pattern.split("/").firstOrNull { it.isNotEmpty() } ?: ""
    }

    override fun matches(verifiedId: VerifiedId) {
        if (!doesMatch(verifiedId))
            throw VerifiedIdIssuerIsNotRequestedException("Provided Verified Id claim doesn't match.")
    }
}