package com.microsoft.walletlibrary.did.sdk.datasource.network.apis

import com.microsoft.walletlibrary.did.sdk.identifier.models.identifierdocument.IdentifierResponse
import com.microsoft.walletlibrary.did.sdk.util.HttpAgentUtils
import com.microsoft.walletlibrary.util.http.httpagent.IHttpAgent
import com.microsoft.walletlibrary.util.http.httpagent.IResponse
import kotlinx.serialization.json.Json

/**
 * Api class to perform identifier related network operations using the provided HttpAgent, utils
 * and json serializer to convert the network response to Identifier related model.
 */
internal class HttpAgentIdentifierApi(private val agent: IHttpAgent,
                                      private val httpAgentUtils: HttpAgentUtils,
                                      private val json: Json) {

    fun toIdentifierResponse(response: IResponse): IdentifierResponse {
        return json.decodeFromString(IdentifierResponse.serializer(), response.body.decodeToString())
    }

    suspend fun resolveIdentifier(overrideUrl: String): Result<IResponse> {
        return agent.get(overrideUrl, httpAgentUtils.defaultHeaders())
    }
}