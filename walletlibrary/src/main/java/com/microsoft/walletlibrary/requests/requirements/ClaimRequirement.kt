package com.microsoft.walletlibrary.requests.requirements

data class ClaimRequirement (
    val claim: String,

    val required: Boolean = false,

    var type: String = ""
)