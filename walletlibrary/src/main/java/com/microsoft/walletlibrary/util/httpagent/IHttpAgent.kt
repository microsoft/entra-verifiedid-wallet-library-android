package com.microsoft.walletlibrary.util.httpagent

abstract class IHttpAgent {
    abstract suspend fun get(url: String, headers: Map<String, String>): IResponse

    abstract suspend fun post(url: String, headers: Map<String, String>, payload: ByteArray): IResponse
}