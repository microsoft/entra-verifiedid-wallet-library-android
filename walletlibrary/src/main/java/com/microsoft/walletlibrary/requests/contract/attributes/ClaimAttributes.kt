package com.microsoft.walletlibrary.requests.contract.attributes

data class ClaimAttributes(
    // Type of the claim (eg. String or Date)
    val type: String,

    // Label of the claim
    val label: String
)