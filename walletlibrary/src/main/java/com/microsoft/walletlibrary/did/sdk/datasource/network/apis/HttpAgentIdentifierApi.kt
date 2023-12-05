package com.microsoft.walletlibrary.did.sdk.datasource.network.apis

import com.microsoft.walletlibrary.did.sdk.identifier.models.identifierdocument.IdentifierResponse
import com.microsoft.walletlibrary.util.http.httpagent.IHttpAgent
import kotlinx.serialization.json.Json

internal class HttpAgentIdentifierApi(private val agent: IHttpAgent,
                             private val json: Json) {
    suspend fun resolveIdentifier(overrideUrl: String): Result<IdentifierResponse> {
        return agent.get(overrideUrl, emptyMap())
            .map {
                 json.decodeFromString(IdentifierResponse.serializer(), it.body.decodeToString())
            }
    }
}