package com.microsoft.walletlibrary.networking.entities.openid4vci.credentialmetadata

import com.microsoft.walletlibrary.requests.styles.BasicVerifiedIdStyle
import com.microsoft.walletlibrary.requests.styles.VerifiedIdLogo
import com.microsoft.walletlibrary.requests.styles.VerifiedIdStyle
import kotlinx.serialization.SerialName
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
    @SerialName("background_color")
    val backgroundColor: String? = null,

    // Text color of the credential.
    @SerialName("text_color")
    val textColor: String? = null
) {
    fun transformToVerifiedIdLogo(): VerifiedIdLogo {
        return VerifiedIdLogo(
            logo?.uri ?: "",
            logo?.alternativeText ?: ""
        )
    }

    fun transformToVerifiedIdStyle(issuerName: String): VerifiedIdStyle {
        return BasicVerifiedIdStyle(
            name ?: "",
            issuerName,
            backgroundColor ?: "",
            textColor ?: "",
            description ?: "",
            transformToVerifiedIdLogo()
        )
    }
}