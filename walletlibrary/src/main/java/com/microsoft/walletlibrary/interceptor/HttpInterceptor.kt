package com.microsoft.walletlibrary.interceptor

/**
 * Allows for hooking into http request events.
 * Inherit from this class and override functions to add hooks
 */
open class HttpInterceptor {

    /**
     * Runs before an http request is made
     * @Param request the http request about to be made
     */
    fun beforeRequest(request: HttpRequest) {
        // no-op
    }

    /**
     * Runs after an http request is made
     * @Param response the http request about to be made
     */
    fun onResponse(response: HttpResponse) {
        // no-op
    }
}