package com.microsoft.walletlibrary.requests.requirements

data class SelfAttestedClaimRequirement(
    internal val id: String,

    // Claim requested
    val claim: String,

    // Indicates if the requirement must be encrypted
    internal val encrypted: Boolean = false,

    // Indicates if the requirement is required or optional
    val required: Boolean = false
)