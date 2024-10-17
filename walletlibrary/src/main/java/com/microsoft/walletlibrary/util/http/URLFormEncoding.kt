package com.microsoft.walletlibrary.util.http

import android.os.Build
import com.microsoft.walletlibrary.util.WalletLibraryException
import java.net.URLEncoder

/**
 * Utility object for URL Form Encoding data.
 */
object URLFormEncoding {
    const val mimeType = "application/x-www-form-urlencoded"

    /***
     * Encodes a map of string to  string, string?, or List<string> to URL Form Encoded payload
     * @throws URLEncodingException if any other type is in the Map
     */
    fun encode(data: Map<String, Any?>): ByteArray {
        val builder = StringBuilder()
        var firstKeyPair = true
        data.forEach { (key, value) ->
            val encodedKey = URLEncoder.encode(key, "UTF-8")
            when (value) {
                is String -> {
                    if (!firstKeyPair) {
                        builder.append("&")
                    }
                    builder.append("${encodedKey}=${URLEncoder.encode(value, "UTF-8")}")
                    if (firstKeyPair)
                    {
                        firstKeyPair = false
                    }
                }
                is List<*> -> {
                    value.iterator().forEach { arrayValue ->
                        when (arrayValue) {
                            is String -> {
                                if (!firstKeyPair) {
                                    builder.append("&")
                                }
                                builder.append("${encodedKey}=${URLEncoder.encode(arrayValue, "UTF-8")}")
                                if (firstKeyPair)
                                {
                                    firstKeyPair = false
                                }
                            }
                            null -> {
                                // skip this value
                            }
                            else -> {
                                throw URLEncodingException(key)
                            }
                        }
                    }
                }
                else -> {
                    throw URLEncodingException(key)
                }
            }
        }
        return builder.toString().toByteArray()
    }

    class URLEncodingException(val keyName: String) : WalletLibraryException()
}

