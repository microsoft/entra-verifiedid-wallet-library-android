// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.did.sdk.datasource.network.apis

import com.microsoft.walletlibrary.did.sdk.util.HttpAgentUtils
import com.microsoft.walletlibrary.networking.entities.tlr.request.RawAcmaRequest
import com.microsoft.walletlibrary.util.http.httpagent.IHttpAgent
import com.microsoft.walletlibrary.util.http.httpagent.IResponse
import kotlinx.serialization.json.Json

internal class HttpAgentAcmaApi(
    private val agent: IHttpAgent,
    private val httpAgentUtils: HttpAgentUtils,
    private val json: Json
) {
    suspend fun submitAcmaRequest(url: String, rawAcmaRequest: RawAcmaRequest): Result<IResponse> {
        val bodyBytes = json.encodeToString(RawAcmaRequest.serializer(), rawAcmaRequest).encodeToByteArray()
        return agent.post(
            url,
            httpAgentUtils.defaultHeaders(HttpAgentUtils.ContentType.Json, bodyBytes),
            bodyBytes
        )
    }
}