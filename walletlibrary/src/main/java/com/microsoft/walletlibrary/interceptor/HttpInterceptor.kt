package com.microsoft.walletlibrary.interceptor

interface HttpInterceptor {
    fun beforeRequest(Request: HttpRequest)

    fun onResponse(Response: HttpResponse)
}