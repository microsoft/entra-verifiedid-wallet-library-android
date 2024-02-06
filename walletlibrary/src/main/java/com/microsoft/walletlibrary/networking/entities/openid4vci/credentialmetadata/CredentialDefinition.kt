package com.microsoft.walletlibrary.networking.entities.openid4vci.credentialmetadata

import kotlinx.serialization.Serializable

/**
 * Describes the metadata of the credential.
 */
@Serializable
internal data class CredentialDefinition(
    // The types of the credential.
    val type: List<String>? = null,

    // Mapping of the claims in the credential to its display information.
    val credentialSubject: Map<String, CredentialSubjectDefinition>? = null,

    // The type of proof that can be used to show ownership of keys bound to crypto binding method (ex. jwt).
    val proof_types_supported: List<String>? = null,

    // Display information for the credential.
    val display: List<LocalizedDisplayDefinition>? = null
)