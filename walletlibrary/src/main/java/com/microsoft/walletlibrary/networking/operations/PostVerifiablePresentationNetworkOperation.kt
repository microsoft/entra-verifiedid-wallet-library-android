// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.networking.operations

import com.microsoft.walletlibrary.did.sdk.datasource.network.PostNetworkOperation
import com.microsoft.walletlibrary.did.sdk.datasource.network.apis.HttpAgentApiProvider
import com.microsoft.walletlibrary.networking.entities.VerifiablePresentationResponse
import com.microsoft.walletlibrary.util.http.httpagent.IResponse
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * Network operation to make a post call to submit a Verifiable Presentation.
 */
internal class PostVerifiablePresentationNetworkOperation(
    private val url: String,
    private val idToken: String,
    private val vpTokens: List<String>,
    private val state: String?,
    private val additionalHeaders: Map<String, String> = emptyMap(),
    private val apiProvider: HttpAgentApiProvider,
    private val serializer: Json
) : PostNetworkOperation<VerifiablePresentationResponse>() {
    override val call: suspend () -> Result<IResponse> = {
        if (vpTokens.size == 1) {
            apiProvider.presentationApis.sendResponse(
                url,
                idToken,
                vpTokens.first(),
                state,
                additionalHeaders
            )
        } else {
            apiProvider.presentationApis.sendResponses(
                url,
                idToken,
                vpTokens,
                state,
                additionalHeaders
            )
        }
    }

    override suspend fun toResult(response: IResponse): Result<VerifiablePresentationResponse> {
        return try {
            serializer.decodeFromString(
                VerifiablePresentationResponse.serializer(),
                response.body.decodeToString()
            ).let { Result.success(it) }
        } catch (e: SerializationException) {
            Result.success(VerifiablePresentationResponse())
        }
    }
}