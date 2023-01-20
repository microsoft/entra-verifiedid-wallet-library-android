package com.microsoft.walletlibrary.requests.requirements

data class AccessTokenRequirement(
    val id: String,
    val encrypted: Boolean,
    val required: Boolean,
    val configuration: String,
    val client_id: String,
    val resourceId: String,
    val scope: String,
    val claims: List<RequestedClaim>
)