package com.microsoft.walletlibrary.did.sdk.datasource.network.apis

import com.microsoft.walletlibrary.did.sdk.credential.service.models.serviceResponses.LinkedDomainsResponse
import com.microsoft.walletlibrary.util.http.httpagent.IHttpAgent
import kotlinx.serialization.json.Json

internal class HttpAgentLinkedDomainsApi(private val agent: IHttpAgent,
                                         private val json : Json
) {
    suspend fun fetchWellKnownConfigDocument (overrideUrl: String): Result<LinkedDomainsResponse> {
        return agent.get(overrideUrl, emptyMap())
            .map { response ->
                json.decodeFromString(LinkedDomainsResponse.serializer(), response.body.decodeToString())
            }
    }
}