package com.microsoft.walletlibrary.networking.entities.openid4vci.credentialmetadata

import android.content.res.Resources
import androidx.core.os.ConfigurationCompat
import kotlinx.serialization.Serializable

/**
 * The display information for the credential.
 */
@Serializable
internal data class CredentialSubjectDefinition(
    // An array of display information to display the credential in different locales.
    val display: List<LocalizedDisplayDefinition>? = null,

    val value_type: String? = null
) {
    fun getPreferredLocalizedDisplayDefinition(): LocalizedDisplayDefinition? {
        if (display == null) {
            return null
        }
        val preferredLanguages = ConfigurationCompat.getLocales(Resources.getSystem().configuration)
        val preferredLanguagesSize = preferredLanguages.size()
        for (i in 0 until preferredLanguagesSize) {
            val preferredLanguage = preferredLanguages.get(i)
            display.forEach {
                if (it.locale == preferredLanguage?.language) {
                    return it
                }
            }
        }
        return display.firstOrNull()
    }
}