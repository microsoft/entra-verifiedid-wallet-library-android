package com.microsoft.walletlibrary.requests

data class RootOfTrust(val verified: Boolean) {
    var source: String? = null
}