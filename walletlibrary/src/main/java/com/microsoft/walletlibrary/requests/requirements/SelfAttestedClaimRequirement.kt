package com.microsoft.walletlibrary.requests.requirements

data class SelfAttestedClaimRequirement(
    val id: String,
    val encrypted: Boolean,
    val required: Boolean,
    val claim: String
)