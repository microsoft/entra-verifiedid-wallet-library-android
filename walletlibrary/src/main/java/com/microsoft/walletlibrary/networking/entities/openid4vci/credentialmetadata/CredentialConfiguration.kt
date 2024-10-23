package com.microsoft.walletlibrary.networking.entities.openid4vci.credentialmetadata

import android.content.res.Resources
import androidx.core.os.ConfigurationCompat
import com.microsoft.walletlibrary.requests.styles.BasicVerifiedIdStyle
import com.microsoft.walletlibrary.requests.styles.VerifiedIdStyle
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Information about the issuer and credential issued.
 */
@Serializable
internal data class CredentialConfiguration(
    // The format of Verified ID that will be issued.
    val format: String? = null,

    // The scope to be used to get Access Token.
    val scope: String? = null,

    // The crypto binding methods supported (ex. did:jwk).
    @SerialName("cryptographic_binding_methods_supported")
    val cryptographicBindingMethodsSupported: List<String>? = null,

    // The crypto suites supported (ex. ES256).
    @SerialName("credential_signing_alg_values_supported")
    val credentialSigningAlgorithmsSupported: List<String>? = null,

    // Describes the metadata of the supported credential.
    @SerialName("credential_definition")
    val credentialDefinition: CredentialDefinition? = null,

    // Display information for the credential.
    val display: List<LocalizedDisplayDefinition>? = null,

    // The types of proofs supported.
    @SerialName("proof_types_supported")
    val proofTypesSupported: Map<String, ProofTypesSupported>? = null
) {
    fun getVerifiedIdStyleInPreferredLocale(issuerName: String): VerifiedIdStyle {
        val localizedDisplayDefinition = getPreferredLocalizedDisplayDefinition()
        return localizedDisplayDefinition?.transformToVerifiedIdStyle(issuerName)
            ?: BasicVerifiedIdStyle("", issuerName, "", "", "", null)
    }

    private fun getPreferredLocalizedDisplayDefinition(): LocalizedDisplayDefinition? {
        if (display == null) return null
        val preferredLocalesList =
            ConfigurationCompat.getLocales(Resources.getSystem().configuration)
        for (index in 0 until preferredLocalesList.size()) {
            val preferredLocale = preferredLocalesList[index]
            display.forEach { displayDefinition ->
                displayDefinition.locale?.let { language ->
                    if (language == preferredLocale?.language) {
                        return displayDefinition
                    }
                }
            }
        }
        return display.first()
    }
}