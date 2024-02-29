package com.microsoft.walletlibrary.networking.entities.openid4vci.credentialmetadata

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
    val cryptographic_binding_methods_supported: List<String>? = null,

    // The crypto suites supported (ex. ES256).
    val cryptographic_suites_supported: List<String>? = null,

    // Describes the metadata of the supported credential.
    val credential_definition: CredentialDefinition? = null ,

    // Display information for the credential.
    val display: List<LocalizedDisplayDefinition>? = null,

    // The types of proofs supported.
    val proof_types_supported: Map<String, ProofTypesSupported>? = null
)