package com.microsoft.walletlibrary.requests.requirements

data class IdTokenRequirement(
    internal val id: String,

    // Properties used by developers to get access token (configuration, clientId, resourceId, scope)
    val configuration: String,
    val client_id: String,
    val redirect_uri: String,
    val scope: String,

    // Nonce is generated using user DID
    val nonce: String,

    // Specific claims requested from id token
    val claims: List<RequestedClaim>,

    // Indicates whether the requirement must be encrypted
    internal val encrypted: Boolean = false,

    // Indicates whether the requirement is required or optional
    val required: Boolean = false
)