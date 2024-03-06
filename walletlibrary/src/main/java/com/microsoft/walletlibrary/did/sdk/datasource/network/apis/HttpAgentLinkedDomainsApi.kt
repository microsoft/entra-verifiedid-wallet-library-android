package com.microsoft.walletlibrary.did.sdk.datasource.network.apis

import com.microsoft.walletlibrary.did.sdk.credential.service.models.serviceResponses.LinkedDomainsResponse
import com.microsoft.walletlibrary.did.sdk.util.HttpAgentUtils
import com.microsoft.walletlibrary.util.http.httpagent.IHttpAgent
import com.microsoft.walletlibrary.util.http.httpagent.IResponse
import kotlinx.serialization.json.Json

/**
 * Api class to perform linked domains related network operations using the provided HttpAgent, utils
 * and json serializer to convert the network response to Linked Domains related model.
 */
internal class HttpAgentLinkedDomainsApi(private val agent: IHttpAgent,
                                         private val httpAgentUtils: HttpAgentUtils,
                                         private val json : Json
) {

    fun toLinkedDomainsResponse(response: IResponse): LinkedDomainsResponse {
        return json.decodeFromString(LinkedDomainsResponse.serializer(), response.body.decodeToString())
    }
    suspend fun fetchWellKnownConfigDocument (overrideUrl: String): Result<IResponse> {
        return agent.get(overrideUrl, httpAgentUtils.defaultHeaders())
    }
}