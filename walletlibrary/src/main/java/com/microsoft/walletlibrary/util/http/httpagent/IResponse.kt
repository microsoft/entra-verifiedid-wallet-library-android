package com.microsoft.walletlibrary.util.http.httpagent

class IResponse(
    val status: Int,
    val headers: Map<String, String>,
    val body: ByteArray
)