package com.microsoft.walletlibrary.did.sdk

import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainResult
import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainVerified
import com.microsoft.walletlibrary.did.sdk.util.controlflow.PresentationException
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

internal object PresentationResponseEndpointValidator {
    fun validate(redirectUrl: String, linkedDomainResult: LinkedDomainResult): String {
        if (linkedDomainResult !is LinkedDomainVerified) {
            PresentationResponseValidationTelemetry.record(
                stage = PresentationResponseValidationTelemetry.Stage.DESTINATION_VALIDATION,
                flightEnabled = true,
                outcome = PresentationResponseValidationTelemetry.Outcome.REJECTED,
                reason = PresentationResponseValidationTelemetry.Reason.UNVERIFIED_DOMAIN
            )
            throw PresentationException("Presentation response destination is not linked to a verified domain.")
        }
        val redirectEndpoint = parseHttpsUrl(redirectUrl, linkedDomainResult)
        PresentationResponseValidationTelemetry.record(
            stage = PresentationResponseValidationTelemetry.Stage.DESTINATION_VALIDATION,
            flightEnabled = true,
            outcome = PresentationResponseValidationTelemetry.Outcome.ACCEPTED,
            destinationRelationship = destinationRelationship(
                redirectEndpoint,
                linkedDomainResult
            )
        )
        return redirectEndpoint.toString()
    }

    private fun parseHttpsUrl(
        value: String,
        linkedDomainResult: LinkedDomainVerified
    ): HttpUrl {
        val url = value.toHttpUrlOrNull()
        val reason = when {
            url == null -> PresentationResponseValidationTelemetry.Reason.MALFORMED_URL
            !url.isHttps -> PresentationResponseValidationTelemetry.Reason.NON_HTTPS_URL
            url.username.isNotEmpty() || url.password.isNotEmpty() ->
                PresentationResponseValidationTelemetry.Reason.URL_CREDENTIALS
            url.fragment != null -> PresentationResponseValidationTelemetry.Reason.URL_FRAGMENT
            else -> null
        }
        if (reason != null) {
            PresentationResponseValidationTelemetry.record(
                stage = PresentationResponseValidationTelemetry.Stage.DESTINATION_VALIDATION,
                flightEnabled = true,
                outcome = PresentationResponseValidationTelemetry.Outcome.REJECTED,
                reason = reason,
                destinationRelationship = url?.let {
                    destinationRelationship(it, linkedDomainResult)
                } ?: PresentationResponseValidationTelemetry.DestinationRelationship.UNKNOWN
            )
            throw PresentationException("Presentation response destination must be an absolute HTTPS URL.")
        }
        return requireNotNull(url)
    }

    private fun destinationRelationship(
        redirectEndpoint: HttpUrl,
        linkedDomainResult: LinkedDomainVerified
    ): PresentationResponseValidationTelemetry.DestinationRelationship {
        val linkedDomainValue = linkedDomainResult.origin
            ?: linkedDomainResult.validatedOrigin
            ?: linkedDomainResult.domainUrl
        val linkedDomainEndpoint = (
            if (linkedDomainValue.contains("://")) linkedDomainValue else "https://$linkedDomainValue"
            ).toHttpUrlOrNull()
            ?: return PresentationResponseValidationTelemetry.DestinationRelationship.UNKNOWN
        val redirectHost = redirectEndpoint.host.trimEnd('.')
        val linkedDomainHost = linkedDomainEndpoint.host.trimEnd('.')
        return when {
            redirectHost == linkedDomainHost && redirectEndpoint.port == linkedDomainEndpoint.port ->
                PresentationResponseValidationTelemetry.DestinationRelationship.SAME_ORIGIN
            redirectHost == linkedDomainHost ->
                PresentationResponseValidationTelemetry.DestinationRelationship.SAME_HOST_DIFFERENT_PORT
            redirectHost.endsWith(".$linkedDomainHost") ->
                PresentationResponseValidationTelemetry.DestinationRelationship.SUBDOMAIN
            else -> PresentationResponseValidationTelemetry.DestinationRelationship.EXTERNAL_HOST
        }
    }
}
