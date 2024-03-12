package com.microsoft.walletlibrary.networking.entities.openid4vci.credentialmetadata

import com.microsoft.walletlibrary.requests.styles.VerifiedIdLogo
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

    // Description of the credential.
    val description: String? = null,

    // Background color of the credential.
    val background_color: String? = null,

    // Text color of the credential.
    val text_color: String? = null
) {
    fun transformToVerifiedIdLogo(): VerifiedIdLogo {
        return VerifiedIdLogo(
            logo?.uri ?: "",
            logo?.alt_text ?: ""
        )
    }
}