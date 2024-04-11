package com.microsoft.walletlibrary.interceptor

typealias MutableHttpHeaders = MutableMap<String, String>
typealias HttpHeaders = Map<String, String>

enum class CommonHeaders(val key: String) {
    RetryAfter("Retry-After")
}