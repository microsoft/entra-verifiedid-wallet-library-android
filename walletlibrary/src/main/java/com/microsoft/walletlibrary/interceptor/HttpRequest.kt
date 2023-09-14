package com.microsoft.walletlibrary.interceptor

data class HttpRequest (
    /**
     * URI/URL of the request
     */
    val uri: String,

    /**
     * Http method of the request
     */
    val method: String,

    /**
     * Http Headers on the request
     */
    var headers: MutableHttpHeaders
)