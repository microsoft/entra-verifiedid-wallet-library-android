package com.microsoft.walletlibrary.networking.entities.openid4vci.credentialmetadata

import kotlinx.serialization.Serializable

/**
 * The display information for the credential.
 */
@Serializable
internal data class CredentialSubjectDefinition(
    // An array of display information to display the credential in different locales.
    val display: List<LocalizedDisplayDefinition>
)