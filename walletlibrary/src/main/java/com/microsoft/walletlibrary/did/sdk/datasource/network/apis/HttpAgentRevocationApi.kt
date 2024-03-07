package com.microsoft.walletlibrary.did.sdk.datasource.network.apis

import com.microsoft.walletlibrary.did.sdk.credential.service.models.serviceResponses.RevocationServiceResponse
import com.microsoft.walletlibrary.did.sdk.util.HttpAgentUtils
import com.microsoft.walletlibrary.util.http.httpagent.IHttpAgent
import com.microsoft.walletlibrary.util.http.httpagent.IResponse
import kotlinx.serialization.json.Json

/**
 * Api class to perform revocation related network operations using the provided HttpAgent, utils
 * and json serializer to convert the network response to Revocation related model.
 */
internal class HttpAgentRevocationApi(private val agent: IHttpAgent,
                                      private val httpAgentUtils: HttpAgentUtils,
                                      private val json : Json
) {
    fun toResponse(response: IResponse): RevocationServiceResponse {
        return json.decodeFromString(RevocationServiceResponse.serializer(), response.body.decodeToString())
    }

    suspend fun sendResponse(overrideUrl: String, body: String): Result<IResponse> {
        val encodedBody = body.encodeToByteArray()
        return agent.post(overrideUrl,
            httpAgentUtils.defaultHeaders(HttpAgentUtils.ContentType.Json, encodedBody),
            encodedBody)
    }
}