package com.microsoft.walletlibrary.networking.entities.openid4vci.credentialmetadata

import kotlinx.serialization.Serializable

/**
 * The display information for the credential for a specific locale.
 */
@Serializable
internal data class LocalizedDisplayDefinition(
    // Name of the credential in a specific locale.
    val name: String? = null,

    // Locale of the display information.
    val locale: String? = null,

    // Metadata for the logo of the credential.
    val logo: LogoDisplayDefinition? = null,

    // Background color of the credential.
    val background_color: String? = null,

    // Text color of the credential.
    val text_color: String? = null
)