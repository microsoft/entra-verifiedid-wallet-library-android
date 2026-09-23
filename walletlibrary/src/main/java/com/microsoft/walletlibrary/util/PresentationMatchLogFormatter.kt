package com.microsoft.walletlibrary.util

import com.microsoft.walletlibrary.requests.requirements.constraints.GroupConstraint
import com.microsoft.walletlibrary.requests.requirements.constraints.VcPathRegexConstraint
import com.microsoft.walletlibrary.requests.requirements.constraints.VcTypeConstraint
import com.microsoft.walletlibrary.requests.requirements.constraints.VerifiedIdConstraint
import com.microsoft.walletlibrary.verifiedid.VerifiedId
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal object PresentationMatchLogFormatter {
    fun format(constraint: VerifiedIdConstraint, candidates: List<VerifiedId>): String =
        buildJsonObject {
            put("event", "presentation_match_not_found")
            put("candidate_count", candidates.size)
            put("criterion", describe(constraint, candidates))
        }.toString()

    private fun describe(
        constraint: VerifiedIdConstraint,
        candidates: List<VerifiedId>
    ): JsonElement = buildJsonObject {
        when (constraint) {
            is VcTypeConstraint -> {
                put("kind", "type")
                put("requested_type", constraint.vcType)
            }
            is VcPathRegexConstraint -> {
                put("kind", "claim")
                put("paths", JsonArray(constraint.path.map { kotlinx.serialization.json.JsonPrimitive(it) }))
                put("filter_present", constraint.pattern.isNotEmpty())
            }
            is GroupConstraint -> {
                put("kind", "group")
                put("operator", constraint.constraintOperator.name)
                put(
                    "criteria",
                    JsonArray(constraint.constraints.map { describe(it, candidates) })
                )
            }
            else -> put("kind", constraint::class.simpleName ?: "unknown")
        }
        put("matched_count", candidates.count { constraint.doesMatch(it) })
    }
}