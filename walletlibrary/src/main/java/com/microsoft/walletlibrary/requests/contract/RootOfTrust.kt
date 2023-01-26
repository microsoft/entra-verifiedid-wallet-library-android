package com.microsoft.walletlibrary.requests.contract

data class RootOfTrust(
    // Source of root of trust (eg. well-known endpoint url)
    val source: String,

    // Result of verification of source
    val verified: Boolean
)