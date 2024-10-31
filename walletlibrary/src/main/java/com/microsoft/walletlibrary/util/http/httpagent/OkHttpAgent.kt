package com.microsoft.walletlibrary.util.http.httpagent

import okhttp3.Call
import okhttp3.Callback
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class OkHttpAgent : IHttpAgent() {
    var client: OkHttpClient = OkHttpClient()

    override suspend fun get(url: String, headers: Map<String, String>): Result<IResponse> {
        val request = Request.Builder()
            .url(url)
            .headers(mapToHeaders(headers))
            .build()

        return call(request)
    }

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

        return call(request)
    }

    private suspend fun call(request: Request): Result<IResponse> {
        return suspendCoroutine<Result<IResponse>> {
            client.newCall(request).enqueue(object: Callback {
                override fun onFailure(call: Call, e: IOException) {
                    it.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    it.resume(toResponse(response))
                }
            })
        }
    }
    private fun mapToHeaders(headersMap: Map<String, String>) : Headers {
        val builder = Headers.Builder()
        headersMap.forEach { (headerName, headerValue) ->
            builder.add(headerName, headerValue)
        }
        return builder.build()
    }

    private fun headersToMap(headers: Headers): Map<String, String> {
        val map = HashMap<String, String>()
        headers.forEach { (headerName, headerValue) ->
            map[headerName] = headerValue
        }
        return map
    }

    private fun bodyToBuffer(body: ResponseBody): ByteArray {
        return body.bytes()
    }

    private fun toResponse(response: Response): Result<IResponse> {
        val body = response.body?.let {
            bodyToBuffer(it)
        } ?: ByteArray(0)

        val abstractResponse = IResponse(
            status = response.code,
            headers = headersToMap(response.headers),
            body = body
        )

        return when (response.code) {
            in 200..299 -> {
                Result.success(abstractResponse)
            }
            else -> {
                Result.failure(abstractResponse.toNetworkingException())
            }
        }
    }
}