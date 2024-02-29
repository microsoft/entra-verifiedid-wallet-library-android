package com.microsoft.walletlibrary.networking.entities.openid4vci.credentialmetadata

import kotlinx.serialization.Serializable

/**
 * The metadata of the credential issuer.
 */
@Serializable
internal data class CredentialMetadata(
    // The end point of the credential issuer.
    val credential_issuer: String? = null,

    // The authorization servers property is a list of endpoints that can be used to get access token for this issuer.
    val authorization_servers: List<String>? = null,

    // The endpoint used to send the proofs to in order to be issued the Verified ID.
    val credential_endpoint: String? = null,

    // The callback endpoint to send the result of issuance.
    val notification_endpoint: String? = null,

    // Token to verify the issuer owns the DID and domain that the metadata comes from.
    val signed_metadata: String? = null,

    // A dictionary of Credential IDs to the corresponding contract.
    val credential_configurations_supported: Map<String, CredentialConfiguration>? = null,

    // Display information for the issuer.
    val display: List<LocalizedIssuerDisplayDefinition>? = null,
)