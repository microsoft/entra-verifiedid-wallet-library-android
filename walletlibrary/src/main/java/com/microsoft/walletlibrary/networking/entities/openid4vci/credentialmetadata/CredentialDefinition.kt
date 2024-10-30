package com.microsoft.walletlibrary.networking.entities.openid4vci.credentialmetadata

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Describes the metadata of the credential.
 */
@Serializable
internal data class CredentialDefinition(
    // The types of the credential.
    val type: List<String>? = null,

    // Mapping of the claims in the credential to its display information.
    @SerialName("credential_subject")
    val credentialSubject: Map<String, CredentialSubjectDefinition>? = null
)