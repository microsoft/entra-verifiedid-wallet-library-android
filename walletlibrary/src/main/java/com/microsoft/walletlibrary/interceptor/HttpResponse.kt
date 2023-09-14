package com.microsoft.walletlibrary.interceptor

data class HttpResponse (
    /**
     * Status Code
     * @see https://developer.mozilla.org/en-US/docs/Web/HTTP/Status
     */
    val statusCode: Int,

    /**
     * Status message for the corresponding status code
     */
    val statusMessage: String,

    /**
     * Headers on the response
     */
    val headers: HttpHeaders
)