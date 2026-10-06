// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.did.sdk

import java.net.URI
import java.util.Locale

internal fun canonicalizeLinkedDomainOrigin(origin: String): String {
    val uri = runCatching { URI(origin) }
        .getOrElse { throw IllegalArgumentException("Linked-domain origin is invalid.") }
    require(!uri.isOpaque)
    require(uri.scheme.equals("https", ignoreCase = true))
    require(uri.rawAuthority != null)
    require(uri.rawUserInfo == null)
    require(!uri.host.isNullOrBlank())
    require(uri.rawPath.isNullOrEmpty() || uri.rawPath == "/")
    require(uri.rawQuery == null)
    require(uri.rawFragment == null)
    require(uri.port == -1 || uri.port in 1..65535)

    val canonicalHost = uri.host
        .lowercase(Locale.ROOT)
        .removeSuffix(".")
    require(canonicalHost.isNotBlank())
    require(!canonicalHost.endsWith("."))

    val canonicalPort = uri.port.takeUnless { it == DEFAULT_HTTPS_PORT } ?: -1
    return URI(
        HTTPS_SCHEME,
        null,
        canonicalHost,
        canonicalPort,
        null,
        null,
        null
    ).toASCIIString()
}

private const val DEFAULT_HTTPS_PORT = 443
private const val HTTPS_SCHEME = "https"
