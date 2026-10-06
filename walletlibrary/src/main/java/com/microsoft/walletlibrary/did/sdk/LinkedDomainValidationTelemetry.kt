// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.did.sdk

import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainMissing
import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainResult
import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainUnVerified
import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainVerified
import com.microsoft.walletlibrary.did.sdk.util.log.SdkLog
import com.microsoft.walletlibrary.util.NetworkingException

internal data class LinkedDomainValidationAttempt(
    val result: LinkedDomainResult,
    val source: LinkedDomainValidationSource,
    val failureStage: LinkedDomainValidationFailureStage,
    val flow: LinkedDomainValidationFlow = LinkedDomainValidationFlow.UNKNOWN,
    val validationMode: LinkedDomainValidationMode = LinkedDomainValidationMode.HARDENED,
    val endpointCountBucket: LinkedDomainValidationCountBucket = LinkedDomainValidationCountBucket.UNKNOWN,
    val credentialCountBucket: LinkedDomainValidationCountBucket = LinkedDomainValidationCountBucket.UNKNOWN,
    val httpStatusClass: LinkedDomainValidationHttpStatusClass = LinkedDomainValidationHttpStatusClass.NOT_APPLICABLE
) {
    fun emit() {
        SdkLog.event(
            LINKED_DOMAIN_VALIDATION_EVENT,
            linkedMapOf(
                "validation_mode" to validationMode.value,
                "flow" to flow.value,
                "source" to source.value,
                "outcome" to result.telemetryOutcome(),
                "failure_stage" to failureStage.value,
                "endpoint_count_bucket" to endpointCountBucket.value,
                "credential_count_bucket" to credentialCountBucket.value,
                "http_status_class" to httpStatusClass.value
            )
        )
    }
}

internal enum class LinkedDomainValidationSource(val value: String) {
    TRUSTED_RESOLVER("trusted_resolver"),
    WELL_KNOWN("well_known")
}

internal enum class LinkedDomainValidationMode(val value: String) {
    HARDENED("hardened"),
    LEGACY("legacy"),
    NOT_AVAILABLE("not_available")
}

internal enum class LinkedDomainValidationFlow(val value: String) {
    ISSUANCE("issuance"),
    PRESENTATION("presentation"),
    UNKNOWN("unknown")
}

internal enum class LinkedDomainValidationFailureStage(val value: String) {
    NONE("none"),
    ENDPOINT_MISSING("endpoint_missing"),
    ENDPOINT_INVALID("endpoint_invalid"),
    DESTINATION_BLOCKED("destination_blocked"),
    RESOLVER_REJECTED("resolver_rejected"),
    RESOLVER_UNAVAILABLE("resolver_unavailable"),
    FETCH_FAILED("fetch_failed"),
    REDIRECT_RESPONSE("redirect_response"),
    DOCUMENT_INVALID("document_invalid"),
    NO_CREDENTIALS("no_credentials"),
    CREDENTIAL_INVALID("credential_invalid"),
    SIGNATURE_INVALID("signature_invalid"),
    CLAIMS_INVALID("claims_invalid"),
    DID_MISMATCH("did_mismatch"),
    ORIGIN_MISMATCH("origin_mismatch"),
    LIMIT_EXCEEDED("limit_exceeded")
}

internal enum class LinkedDomainValidationCountBucket(val value: String) {
    NONE("none"),
    ONE("one"),
    TWO_TO_FIVE("two_to_five"),
    SIX_OR_MORE("six_or_more"),
    UNKNOWN("unknown");

    companion object {
        fun from(count: Int): LinkedDomainValidationCountBucket =
            when (count) {
                0 -> NONE
                1 -> ONE
                in 2..5 -> TWO_TO_FIVE
                else -> SIX_OR_MORE
            }
    }
}

internal enum class LinkedDomainValidationHttpStatusClass(val value: String) {
    SUCCESS("2xx"),
    REDIRECTION("3xx"),
    CLIENT_ERROR("4xx"),
    SERVER_ERROR("5xx"),
    UNKNOWN("unknown"),
    NOT_APPLICABLE("not_applicable");

    companion object {
        fun from(throwable: Throwable): LinkedDomainValidationHttpStatusClass {
            val status = (throwable as? NetworkingException)?.statusCode?.toIntOrNull()
                ?: return UNKNOWN
            return when (status) {
                in 200..299 -> SUCCESS
                in 300..399 -> REDIRECTION
                in 400..499 -> CLIENT_ERROR
                in 500..599 -> SERVER_ERROR
                else -> UNKNOWN
            }
        }
    }
}

private fun LinkedDomainResult.telemetryOutcome(): String =
    when (this) {
        is LinkedDomainVerified -> "verified"
        is LinkedDomainUnVerified -> "unverified"
        is LinkedDomainMissing -> "missing"
        else -> "error"
    }

private const val LINKED_DOMAIN_VALIDATION_EVENT = "LinkedDomainValidation"
