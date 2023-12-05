package com.microsoft.walletlibrary.util.http.httpagent

import com.microsoft.walletlibrary.did.sdk.CorrelationVectorService
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import javax.inject.Inject

class OkHttpAgent @Inject constructor(userAgentInfo: String,
                  walletLibraryVersion: String,
                  correlationVectorService: CorrelationVectorService
) : IHttpAgent(
    userAgentInfo,
    walletLibraryVersion,
    correlationVectorService
) {
    val client: OkHttpClient = OkHttpClient()

    override suspend fun get(url: String, headers: Map<String, String>): Result<IResponse> {
        val sendMap = combineMaps(headers, defaultHeaders())
        val request = Request.Builder()
            .url(url)
            .headers(mapToHeaders(sendMap))
            .build()

        return toResponse(client.newCall(request).execute())
    }

    override suspend fun post(
        url: String,
        headers: Map<String, String>,
        payload: ByteArray
    ): Result<IResponse> {
        val sendMap = combineMaps(headers, defaultHeaders(body = payload))
        val request = Request.Builder()
            .url(url)
            .headers(mapToHeaders(sendMap))
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

    private fun toResponse(response: Response) : Result<IResponse> {
        val body = response.body?.let {
            bodyToBuffer(it)
        } ?: ByteArray(0)

        val abstractResponse = IResponse(
            status = response.code.toUInt(),
            headers = headersToMap(response.headers),
            body = body
        )

        return when (response.code) {
            in 200..299 -> {
                Result.success(abstractResponse)
            }
            in 400..499 -> {
                Result.failure(ClientError(abstractResponse))
            }
            in 500..599 -> {
                Result.failure(ServerError(abstractResponse))
            }
            else -> {
                Result.failure(UnknownError())
            }
        }

    }
}