package com.microsoft.walletlibrary.verifiedid

data class VerifiedId(
    val id: String,
    val type: VerifiedIdType,
    val claims: List<VerifiedIdClaim>,
    val issuedOn: Long,
    val expiresOn: Long? = null,
    val raw: String
)