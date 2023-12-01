package com.microsoft.walletlibrary.util.httpagent

import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody

class OkHttpAgent : IHttpAgent() {
    val client: OkHttpClient = OkHttpClient()

    override suspend fun get(url: String, headers: Map<String, String>): IResponse {
        val request = Request.Builder()
            .url(url)
            .headers(mapToHeaders(headers))
            .build()

        return toResponse(client.newCall(request).execute())
    }

    override suspend fun post(
        url: String,
        headers: Map<String, String>,
        payload: ByteArray
    ): IResponse {
        val request = Request.Builder()
            .url(url)
            .headers(mapToHeaders(headers))
            .post(payload.toRequestBody())
            .build()

        return toResponse(client.newCall(request).execute())
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

    private fun toResponse(response: Response) : IResponse {
        response.body?.let {
            val body = bodyToBuffer(it)
            return IResponse(
                status = response.code.toUInt(),
                headers = headersToMap(response.headers),
                body = body
            )
        }
        return IResponse(
            status = response.code.toUInt(),
            headers = headersToMap(response.headers),
            body = ByteArray(0)
        )
    }
}