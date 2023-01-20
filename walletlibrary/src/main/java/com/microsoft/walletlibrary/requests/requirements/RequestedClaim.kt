package com.microsoft.walletlibrary.requests.requirements

data class RequestedClaim(val required: Boolean, val indexed: Boolean, val claim: String)