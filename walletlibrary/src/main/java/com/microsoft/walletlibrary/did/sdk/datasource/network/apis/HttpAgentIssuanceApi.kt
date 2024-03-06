package com.microsoft.walletlibrary.did.sdk.datasource.network.apis

import com.microsoft.walletlibrary.did.sdk.credential.service.models.serviceResponses.ContractServiceResponse
import com.microsoft.walletlibrary.did.sdk.credential.service.models.serviceResponses.IssuanceServiceResponse
import com.microsoft.walletlibrary.did.sdk.util.HttpAgentUtils
import com.microsoft.walletlibrary.util.http.httpagent.IHttpAgent
import com.microsoft.walletlibrary.util.http.httpagent.IResponse
import kotlinx.serialization.json.Json

internal class HttpAgentIssuanceApi(private val agent: IHttpAgent,
                                    private val httpAgentUtils: HttpAgentUtils,
                                    private val json :Json) {

    fun parseContract(response: IResponse): ContractServiceResponse {
        return json.decodeFromString(ContractServiceResponse.serializer(), response.body.decodeToString())
    }

    suspend fun getContract(overrideUrl: String): Result<IResponse> {
        return agent.get(overrideUrl,
            httpAgentUtils.combineMaps(
                httpAgentUtils.defaultHeaders(),
            mapOf(
            "x-ms-sign-contract" to "true"
        )))
    }

    fun parseIssuance(response: IResponse): IssuanceServiceResponse {
        return json.decodeFromString(IssuanceServiceResponse.serializer(), response.body.decodeToString())
    }

    suspend fun sendResponse(overrideUrl: String, body: String): Result<IResponse> {
        val bodyBytes = body.encodeToByteArray()
        return agent.post(overrideUrl,
            httpAgentUtils.defaultHeaders(null, bodyBytes),
            bodyBytes)
    }

    suspend fun sendCompletionResponse(overrideUrl: String, body: String): Result<IResponse> {
        val bodyBytes = body.toByteArray()
        return agent.post(overrideUrl,
            httpAgentUtils.defaultHeaders(HttpAgentUtils.ContentType.Json, bodyBytes),
            bodyBytes)
    }
}