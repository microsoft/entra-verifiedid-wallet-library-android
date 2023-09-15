package com.microsoft.walletlibrary.interceptor

data class HttpResponse(
    /**
     * original http request
     */
    val request: HttpRequest,

    /**
     * Status Code
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