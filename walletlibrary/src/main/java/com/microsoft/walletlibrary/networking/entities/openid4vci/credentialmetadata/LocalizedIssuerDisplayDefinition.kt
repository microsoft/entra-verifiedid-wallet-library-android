package com.microsoft.walletlibrary.networking.entities.openid4vci.credentialmetadata

import kotlinx.serialization.Serializable

/**
 * Localized display definition for the issuer.
 */
@Serializable
data class LocalizedIssuerDisplayDefinition(
    // The name of the issuer.
    val name: String? = null,

    // The locale of the display definition.
    val locale: String? = null
)