// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.networking.operations

import com.microsoft.walletlibrary.did.sdk.datasource.network.PostNetworkOperation
import com.microsoft.walletlibrary.did.sdk.datasource.network.apis.HttpAgentApiProvider
import com.microsoft.walletlibrary.networking.entities.tlr.request.RawAcmaRequest
import com.microsoft.walletlibrary.networking.entities.tlr.request.RawAcmaResponse
import com.microsoft.walletlibrary.util.http.httpagent.IResponse
import kotlinx.serialization.json.Json

internal class PostAcmaRequestNetworkOperation(
    private val url: String,
    private val continuationToken: String,
    private val apiProvider: HttpAgentApiProvider,
    private val serializer: Json
) : PostNetworkOperation<RawAcmaResponse>() {
    override val call: suspend () -> Result<IResponse> = {
        apiProvider.acmaApi.submitAcmaRequest(url, RawAcmaRequest(continuationToken))
    }

    override suspend fun toResult(response: IResponse): Result<RawAcmaResponse> {
        return serializer.decodeFromString(
            RawAcmaResponse.serializer(),
            response.body.decodeToString()
        ).let { Result.success(it) }
    }
}