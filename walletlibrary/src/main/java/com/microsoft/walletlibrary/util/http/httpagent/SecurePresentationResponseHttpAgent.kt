package com.microsoft.walletlibrary.util.http.httpagent

import okhttp3.Call
import okhttp3.Callback
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal interface PresentationResponseHttpAgent {
    suspend fun post(url: String, headers: Map<String, String>, payload: ByteArray): Result<IResponse>
}

internal class SecurePresentationResponseHttpAgent(
    internal val client: OkHttpClient = OkHttpClient.Builder()
        .followRedirects(false)
        .followSslRedirects(false)
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

        return suspendCancellableCoroutine { continuation ->
            val call = client.newCall(request)
            continuation.invokeOnCancellation { call.cancel() }
            call.enqueue(object : Callback {
                override fun onFailure(call: Call, exception: IOException) {
                    if (continuation.isActive) {
                        continuation.resumeWithException(exception)
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val result = try {
                        response.use { it.toResult() }
                    } catch (exception: IOException) {
                        if (continuation.isActive) {
                            continuation.resumeWithException(exception)
                        }
                        return
                    }
                    if (continuation.isActive) {
                        continuation.resume(result)
                    }
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
