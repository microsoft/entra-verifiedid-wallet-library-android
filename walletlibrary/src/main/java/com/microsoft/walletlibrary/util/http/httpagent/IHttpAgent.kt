package com.microsoft.walletlibrary.util.http.httpagent

import com.microsoft.walletlibrary.did.sdk.CorrelationVectorService
import com.microsoft.walletlibrary.did.sdk.util.Constants
import com.microsoft.walletlibrary.util.http.URLFormEncoding
import javax.inject.Inject

abstract class IHttpAgent constructor(
    private val userAgentInfo: String,
    private val walletLibraryVersion: String,
    private val correlationVectorService: CorrelationVectorService) {

    class ClientError(val response: IResponse): Error() {}
    class ServerError(val response: IResponse): Error() {}

    abstract suspend fun get(url: String, headers: Map<String, String>): Result<IResponse>

    abstract suspend fun post(url: String, headers: Map<String, String>, payload: ByteArray): Result<IResponse>


    enum class ContentType {
        Json,
        UrlFormEncoded
    }

    fun combineMaps(a: Map<String, String>, b: Map<String, String>): Map<String, String> {
        a.toMutableMap().putAll(b)
        return a
    }
    fun defaultHeaders(contentType: ContentType? = null, body: ByteArray? = null): Map<String, String> {
        val headers = mutableMapOf(
            Constants.USER_AGENT_HEADER to userAgentInfo,
            Constants.WALLET_LIBRARY_VERSION_HEADER to walletLibraryVersion
        )
        correlationVectorService.incrementAndSave().let {
                correlationVector ->
            headers[Constants.CORRELATION_VECTOR_HEADER] =  correlationVector
        }
        contentType?.let {
            headers[Constants.CONTENT_TYPE] = when (contentType) {
                ContentType.Json -> { "application/json"}
                ContentType.UrlFormEncoded -> { URLFormEncoding.mimeType }
            }
        }
        body?.let {
            headers[Constants.CONTENT_LENGTH] = body.size.toString()
        }
        return headers
    }
}