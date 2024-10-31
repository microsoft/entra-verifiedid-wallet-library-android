package com.microsoft.walletlibrary.util.http.httpagent

abstract class IHttpAgent {
    abstract suspend fun get(url: String, headers: Map<String, String>): Result<IResponse>

    abstract suspend fun post(url: String, headers: Map<String, String>, payload: ByteArray): Result<IResponse>
}