package com.microsoft.walletlibrary.util.http.httpagent

import okhttp3.Call
import okhttp3.Callback
import okhttp3.Dns
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.net.InetAddress
import java.net.Proxy
import java.net.UnknownHostException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal interface PresentationResponseHttpAgent {
    suspend fun post(url: String, headers: Map<String, String>, payload: ByteArray): Result<IResponse>
}

internal class SecurePresentationResponseHttpAgent(
    internal val client: OkHttpClient = OkHttpClient.Builder()
        .followRedirects(false)
        .followSslRedirects(false)
        .proxy(Proxy.NO_PROXY)
        .dns(PublicNetworkDns(Dns.SYSTEM))
        .build()
) : PresentationResponseHttpAgent {
    override suspend fun post(
        url: String,
        headers: Map<String, String>,
        payload: ByteArray
    ): Result<IResponse> {
        val request = Request.Builder()
            .url(url)
            .headers(mapToHeaders(headers))
            .post(payload.toRequestBody())
            .build()

        return suspendCoroutine { continuation ->
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, exception: IOException) {
                    continuation.resumeWithException(exception)
                }

                override fun onResponse(call: Call, response: Response) {
                    continuation.resume(response.toResult())
                }
            })
        }
    }

    private fun mapToHeaders(headers: Map<String, String>): Headers {
        return Headers.Builder().apply {
            headers.forEach { (name, value) -> add(name, value) }
        }.build()
    }

    private fun Response.toResult(): Result<IResponse> {
        val result = IResponse(
            status = code,
            headers = headers.associate { it.first to it.second },
            body = body?.bytes() ?: ByteArray(0)
        )
        return if (code in 200..299) {
            Result.success(result)
        } else {
            Result.failure(result.toNetworkingException())
        }
    }
}

internal class PublicNetworkDns(private val delegate: Dns) : Dns {
    override fun lookup(hostname: String): List<InetAddress> {
        val addresses = delegate.lookup(hostname)
        if (addresses.isEmpty() || addresses.any { !PublicNetworkAddressValidator.isPublic(it) }) {
            throw UnknownHostException("Presentation response destination must resolve only to public addresses")
        }
        return addresses
    }
}
