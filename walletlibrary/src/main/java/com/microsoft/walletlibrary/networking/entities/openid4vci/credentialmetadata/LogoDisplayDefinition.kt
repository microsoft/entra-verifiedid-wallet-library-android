package com.microsoft.walletlibrary.networking.entities.openid4vci.credentialmetadata

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The display information for the logo of the credential.
 */
@Serializable
data class LogoDisplayDefinition(
    // The URI of the logo.
    val uri: String? = null,

    // The alternative text to describe the logo.
    @SerialName("alt_text")
    val alternativeText: String? = null
)