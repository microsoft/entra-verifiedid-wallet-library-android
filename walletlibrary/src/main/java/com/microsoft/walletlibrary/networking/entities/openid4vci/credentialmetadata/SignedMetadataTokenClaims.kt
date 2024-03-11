package com.microsoft.walletlibrary.networking.entities.openid4vci.credentialmetadata

import com.microsoft.walletlibrary.util.TokenValidationException
import com.microsoft.walletlibrary.util.VerifiedIdExceptions
import kotlinx.serialization.Serializable

@Serializable
data class SignedMetadataTokenClaims(
    val sub: String?,
    val iat: String?,
    val exp: String?,
    val iss: String?,
    val nbf: String?
) {
    fun validateSignedMetadataTokenClaims(expectedSubject: String, expectedIssuer: String) {
        validateIssuer(expectedIssuer)
        validateSubject(expectedSubject)
    }

    private fun validateIssuer(expectedIssuer: String) {
        if (iss == null) {
            throw TokenValidationException(
                "Issuer property missing in signed metadata.",
                VerifiedIdExceptions.INVALID_PROPERTY_EXCEPTION.value
            )
        }
        if (iss != expectedIssuer) {
            throw TokenValidationException(
                "Invalid issuer property in signed metadata.",
                VerifiedIdExceptions.INVALID_PROPERTY_EXCEPTION.value
            )
        }
    }

    private fun validateSubject(expectedSubject: String) {
        if (sub == null) {
            throw TokenValidationException(
                "Subject property missing in signed metadata.",
                VerifiedIdExceptions.INVALID_PROPERTY_EXCEPTION.value
            )
        }
        if (sub != expectedSubject) {
            throw TokenValidationException(
                "Invalid subject property in signed metadata.",
                VerifiedIdExceptions.INVALID_PROPERTY_EXCEPTION.value
            )
        }
    }
}