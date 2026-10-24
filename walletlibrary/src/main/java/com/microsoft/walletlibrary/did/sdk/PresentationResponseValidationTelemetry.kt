// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.did.sdk

import com.microsoft.walletlibrary.did.sdk.util.controlflow.PresentationException
import com.microsoft.walletlibrary.util.MalformedInputException
import com.microsoft.walletlibrary.util.NetworkingException
import com.microsoft.walletlibrary.util.RequirementNotMetException
import com.microsoft.walletlibrary.util.WalletLibraryLogger

internal object PresentationResponseValidationTelemetry {
    private const val EVENT_NAME = "PresentationResponseValidation"

    fun record(
        stage: Stage,
        flightEnabled: Boolean,
        outcome: Outcome,
        reason: Reason = Reason.NONE,
        destinationRelationship: DestinationRelationship = DestinationRelationship.NOT_APPLICABLE,
        transport: Transport = Transport.NOT_APPLICABLE,
        responseCount: ResponseCount = ResponseCount.NOT_APPLICABLE,
        httpStatusClass: HttpStatusClass = HttpStatusClass.NOT_APPLICABLE
    ) {
        WalletLibraryLogger.event(
            EVENT_NAME,
            linkedMapOf(
                "stage" to stage.value,
                "flight_enabled" to flightEnabled.toString(),
                "outcome" to outcome.value,
                "reason" to reason.value,
                "destination_relationship" to destinationRelationship.value,
                "transport" to transport.value,
                "response_count" to responseCount.value,
                "http_status_class" to httpStatusClass.value
            )
        )
    }

    fun completionFailureReason(throwable: Throwable): Reason {
        val causes = generateSequence(throwable) { it.cause }
        return when {
            causes.any { it is NetworkingException } -> Reason.NETWORK_ERROR
            causes.any {
                it is PresentationException ||
                    it is MalformedInputException ||
                    it is RequirementNotMetException
            } -> Reason.VALIDATION_ERROR
            else -> Reason.OTHER_ERROR
        }
    }

    enum class Stage(val value: String) {
        SIGNER_BINDING("signer_binding"),
        DESTINATION_VALIDATION("destination_validation"),
        RESPONSE_POST("response_post"),
        COMPLETION("completion")
    }

    enum class Outcome(val value: String) {
        ACCEPTED("accepted"),
        REJECTED("rejected"),
        SUCCEEDED("succeeded"),
        FAILED("failed")
    }

    enum class Reason(val value: String) {
        NONE("none"),
        SIGNER_MISMATCH("signer_mismatch"),
        UNVERIFIED_DOMAIN("unverified_domain"),
        MALFORMED_URL("malformed_url"),
        NON_HTTPS_URL("non_https_url"),
        URL_CREDENTIALS("url_credentials"),
        URL_FRAGMENT("url_fragment"),
        VALIDATION_ERROR("validation_error"),
        NETWORK_ERROR("network_error"),
        OTHER_ERROR("other_error")
    }

    enum class DestinationRelationship(val value: String) {
        SAME_ORIGIN("same_origin"),
        SAME_HOST_DIFFERENT_PORT("same_host_different_port"),
        SUBDOMAIN("subdomain"),
        EXTERNAL_HOST("external_host"),
        UNKNOWN("unknown"),
        NOT_APPLICABLE("not_applicable")
    }

    enum class Transport(val value: String) {
        REDIRECT_RESISTANT("redirect_resistant"),
        CALLER_PROVIDED("caller_provided"),
        NOT_APPLICABLE("not_applicable")
    }

    enum class ResponseCount(val value: String) {
        SINGLE("single"),
        MULTIPLE("multiple"),
        NOT_APPLICABLE("not_applicable")
    }

    enum class HttpStatusClass(val value: String) {
        REDIRECTION("3xx"),
        CLIENT_ERROR("4xx"),
        SERVER_ERROR("5xx"),
        UNKNOWN("unknown"),
        NOT_APPLICABLE("not_applicable");

        companion object {
            fun from(throwable: Throwable): HttpStatusClass {
                val status = (throwable as? NetworkingException)?.statusCode?.toIntOrNull()
                    ?: return UNKNOWN
                return when (status) {
                    in 300..399 -> REDIRECTION
                    in 400..499 -> CLIENT_ERROR
                    in 500..599 -> SERVER_ERROR
                    else -> UNKNOWN
                }
            }
        }
    }
}
