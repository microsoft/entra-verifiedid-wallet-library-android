package com.microsoft.walletlibrary.did.sdk.datasource.network.apis

import com.microsoft.walletlibrary.did.sdk.credential.service.models.serviceResponses.ContractServiceResponse
import com.microsoft.walletlibrary.did.sdk.credential.service.models.serviceResponses.IssuanceServiceResponse
import com.microsoft.walletlibrary.did.sdk.util.Constants
import com.microsoft.walletlibrary.util.http.httpagent.IHttpAgent
import kotlinx.serialization.json.Json
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Url

internal class HttpAgentIssuanceApi(private val agent: IHttpAgent,
        private val json :Json) {
    suspend fun getContract(overrideUrl: String): Result<ContractServiceResponse> {
        return agent.get(overrideUrl, mapOf(
            "x-ms-sign-contract" to "true"
        )).map {
                response ->
            json.decodeFromString(ContractServiceResponse.serializer(), response.body.decodeToString())
        }
    }

    suspend fun sendResponse(overrideUrl: String, body: String): Result<IssuanceServiceResponse> {
        return agent.post(overrideUrl, emptyMap(), body.encodeToByteArray())
            .map { response ->
                json.decodeFromString(IssuanceServiceResponse.serializer(), response.body.decodeToString())
            }
    }

    suspend fun sendCompletionResponse(overrideUrl: String, body: String): Result<Unit> {
        return agent.post(overrideUrl, mapOf(
            Constants.CONTENT_TYPE to "application/json"
        ), body.toByteArray())
            .map { _ ->
                Unit
            }
    }
}