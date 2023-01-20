package com.microsoft.walletlibrary.requests.requirements

data class IdTokenRequirement(
    val id: String,
    val encrypted: Boolean,
    val required: Boolean,
    val configuration: String,
    val client_id: String,
    val redirect_uri: String,
    val scope: String,
    val nonce: String,
    val claims: List<RequestedClaim>
)