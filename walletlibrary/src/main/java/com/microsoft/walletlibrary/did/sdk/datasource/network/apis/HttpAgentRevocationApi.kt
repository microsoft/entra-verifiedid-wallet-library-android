package com.microsoft.walletlibrary.did.sdk.datasource.network.apis

import com.microsoft.walletlibrary.did.sdk.credential.service.models.serviceResponses.RevocationServiceResponse
import com.microsoft.walletlibrary.util.http.httpagent.IHttpAgent
import kotlinx.serialization.json.Json

internal class HttpAgentRevocationApi(private val agent: IHttpAgent,
                             private val json : Json
) {
    suspend fun sendResponse(overrideUrl: String, body: String): Result<RevocationServiceResponse> {
        return agent.post(overrideUrl, emptyMap(), body.encodeToByteArray())
            .map { response ->
                json.decodeFromString(RevocationServiceResponse.serializer(), response.body.decodeToString())
            }
    }
}