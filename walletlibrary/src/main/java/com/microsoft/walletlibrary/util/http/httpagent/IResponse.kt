package com.microsoft.walletlibrary.util.http.httpagent

class IResponse(
    val status: UInt,
    val headers: Map<String, String>,
    val body: ByteArray
)