package com.microsoft.walletlibrary.requests.contract.attributes

data class RequesterAttributes(
    // Name of the requester
    val requester: String,

    // Locale of the requester
    val locale: String,

    // Logo of the requester. If a url is present in contract, it is fetched to this
    val logo: Logo? = null)