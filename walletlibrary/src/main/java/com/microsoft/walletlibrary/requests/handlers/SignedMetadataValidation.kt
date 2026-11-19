// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.requests.handlers

import com.microsoft.walletlibrary.did.sdk.canonicalizeLinkedDomainOrigin
import com.microsoft.walletlibrary.networking.entities.openid4vci.credentialmetadata.CredentialMetadata
import com.microsoft.walletlibrary.util.VerifiedIdException
import com.microsoft.walletlibrary.util.VerifiedIdExceptions
import com.microsoft.walletlibrary.util.WalletLibraryLogger
import java.net.URI

internal object SignedMetadataValidation {
    enum class Outcome(val value: String) {
        ACCEPTED("accepted"),
        REJECTED("rejected"),
        CANCELLED("cancelled")
    }

    enum class Reason(val value: String) {
        NONE("none"),
        INVALID_ISSUER("invalid_issuer"),
        ISSUER_MISMATCH("issuer_mismatch"),
        MISSING_SIGNED_METADATA("missing_signed_metadata"),
        INVALID_SIGNATURE_OR_CLAIMS("invalid_signature_or_claims"),
        UNVERIFIED_ISSUER_ORIGIN("unverified_issuer_origin"),
        MISSING_SIGNED_FIELDS("missing_signed_fields"),
        INVALID_ENDPOINT("invalid_endpoint"),
        VALIDATION_ERROR("validation_error"),
        NETWORK_ERROR("network_error"),
        CANCELLED("cancelled")
    }

    fun issuerOrigin(issuer: String): String {
        val uri = try {
            URI(issuer)
        } catch (_: IllegalArgumentException) {
            throw SignedMetadataValidationException(Reason.INVALID_ISSUER)
        } catch (_: java.net.URISyntaxException) {
            throw SignedMetadataValidationException(Reason.INVALID_ISSUER)
        }
        if (!uri.isAbsolute || uri.isOpaque || uri.rawAuthority == null ||
            uri.host.isNullOrBlank() ||
            uri.rawQuery != null || uri.rawFragment != null || uri.rawUserInfo != null
        ) {
            throw SignedMetadataValidationException(Reason.INVALID_ISSUER)
        }
        return try {
            canonicalizeLinkedDomainOrigin("${uri.scheme}://${uri.rawAuthority}")
        } catch (_: IllegalArgumentException) {
            throw SignedMetadataValidationException(Reason.INVALID_ISSUER)
        }
    }

    fun validateFields(metadata: CredentialMetadata, expectedIssuer: String) {
        if (metadata.credentialIssuer != expectedIssuer) {
            throw SignedMetadataValidationException(Reason.ISSUER_MISMATCH)
        }
        if (metadata.credentialEndpoint.isNullOrBlank() ||
            metadata.authorizationServers.isNullOrEmpty() ||
            metadata.credentialConfigurationsSupported.isNullOrEmpty()
        ) {
            throw SignedMetadataValidationException(Reason.MISSING_SIGNED_FIELDS)
        }
        validateEndpoint(metadata.credentialEndpoint)
        metadata.authorizationServers.forEach(::validateEndpoint)
        metadata.notificationEndpoint?.let(::validateEndpoint)
    }

    private fun validateEndpoint(endpoint: String) {
        val uri = try {
            URI(endpoint)
        } catch (_: java.net.URISyntaxException) {
            throw SignedMetadataValidationException(Reason.INVALID_ENDPOINT)
        }
        if (!uri.scheme.equals("https", ignoreCase = true) ||
            uri.host.isNullOrBlank() ||
            uri.rawUserInfo != null ||
            uri.rawFragment != null ||
            (uri.port != -1 && uri.port !in 1..65535)
        ) {
            throw SignedMetadataValidationException(Reason.INVALID_ENDPOINT)
        }
    }

    fun record(flightEnabled: Boolean, outcome: Outcome, reason: Reason = Reason.NONE) {
        WalletLibraryLogger.event(
            "SignedMetadataValidation",
            linkedMapOf(
                "flight_enabled" to flightEnabled.toString(),
                "outcome" to outcome.value,
                "reason" to reason.value
            )
        )
    }
}

internal class SignedMetadataValidationException(
    val reason: SignedMetadataValidation.Reason
) : VerifiedIdException(
    "Signed credential metadata validation failed (${reason.value}).",
    VerifiedIdExceptions.MALFORMED_SIGNED_METADATA_EXCEPTION.value
)
