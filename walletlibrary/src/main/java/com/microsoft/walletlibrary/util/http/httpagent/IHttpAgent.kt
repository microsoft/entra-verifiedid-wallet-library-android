package com.microsoft.walletlibrary.util.http.httpagent

abstract class IHttpAgent {
    abstract suspend fun get(url: String, headers: Map<String, String>): Result<IResponse>

    open suspend fun getWithoutRedirects(url: String, headers: Map<String, String>): Result<IResponse> {
        return Result.failure(UnsupportedOperationException("HTTP agent does not support disabling redirects"))
    }

    abstract suspend fun post(url: String, headers: Map<String, String>, payload: ByteArray): Result<IResponse>
}