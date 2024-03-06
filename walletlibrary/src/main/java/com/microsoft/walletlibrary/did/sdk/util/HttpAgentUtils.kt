// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.did.sdk.util

import com.microsoft.walletlibrary.util.http.URLFormEncoding
import javax.inject.Inject
import javax.inject.Named

/**
 * Internal Class to apply common headers
 */
internal class HttpAgentUtils @Inject constructor(@Named("userAgentInfo") private val userAgentInfo: String,
                                                  @Named("walletLibraryVersionInfo") private val walletLibraryVersionInfo: String) {
    enum class ContentType {
        Json,
        UrlFormEncoded
    }

    fun combineMaps(a: Map<String, String>, b: Map<String, String>): Map<String, String> {
        val combinedMap = a.toMutableMap()
        combinedMap.putAll(b)
        return combinedMap
    }
    fun defaultHeaders(contentType: ContentType? = null, body: ByteArray? = null): MutableMap<String, String> {
        val headers = mutableMapOf(
            Constants.USER_AGENT_HEADER to userAgentInfo,
            Constants.WALLET_LIBRARY_VERSION_HEADER to walletLibraryVersionInfo
        )
        headers[Constants.CONTENT_TYPE] = when (contentType) {
            ContentType.Json -> { "application/json"}
            ContentType.UrlFormEncoded -> { URLFormEncoding.mimeType }
            else -> { "text/plain" }
        }

        body?.let {
            headers[Constants.CONTENT_LENGTH] = body.size.toString()
        }
        return headers
    }

}