package com.microsoft.walletlibrary.util.http.httpagent

import java.nio.ByteBuffer

class IResponse(
    val status: UInt,
    val headers: Map<String, String>,
    val body: ByteArray
)