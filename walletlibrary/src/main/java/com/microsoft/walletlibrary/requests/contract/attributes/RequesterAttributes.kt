package com.microsoft.walletlibrary.requests.contract.attributes

data class RequesterAttributes(val requester: String, val locale: String) {
    val logo: Logo? = null
}