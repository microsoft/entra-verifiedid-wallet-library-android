package com.microsoft.walletlibrary.did.sdk.datasource.network.apis

import com.microsoft.walletlibrary.did.sdk.credential.service.models.serviceResponses.ContractServiceResponse
import com.microsoft.walletlibrary.did.sdk.credential.service.models.serviceResponses.IssuanceServiceResponse
import com.microsoft.walletlibrary.did.sdk.util.Constants
import com.microsoft.walletlibrary.util.http.httpagent.IHttpAgent
import com.microsoft.walletlibrary.util.http.httpagent.IResponse
import kotlinx.serialization.json.Json

internal class HttpAgentIssuanceApi(private val agent: IHttpAgent,
        private val json :Json) {

    fun parseContract(response: IResponse): ContractServiceResponse {
        return json.decodeFromString(ContractServiceResponse.serializer(), response.body.decodeToString())
    }
    suspend fun getContract(overrideUrl: String): Result<IResponse> {
        return agent.get(overrideUrl, mapOf(
            "x-ms-sign-contract" to "true"
        ))
    }

    fun parseIssuance(response: IResponse): IssuanceServiceResponse {
        return json.decodeFromString(IssuanceServiceResponse.serializer(), response.body.decodeToString())
    }

    suspend fun sendResponse(overrideUrl: String, body: String): Result<IResponse> {
        return agent.post(overrideUrl, emptyMap(), body.encodeToByteArray())
    }

    suspend fun sendCompletionResponse(overrideUrl: String, body: String): Result<IResponse> {
        return agent.post(overrideUrl, mapOf(
            Constants.CONTENT_TYPE to "application/json"
        ), body.toByteArray())
    }
}