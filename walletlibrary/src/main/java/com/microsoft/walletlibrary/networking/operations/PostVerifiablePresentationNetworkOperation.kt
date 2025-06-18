// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.networking.operations

import com.microsoft.walletlibrary.did.sdk.datasource.network.PostNetworkOperation
import com.microsoft.walletlibrary.did.sdk.datasource.network.apis.HttpAgentApiProvider
import com.microsoft.walletlibrary.util.http.httpagent.IResponse
import com.microsoft.walletlibrary.verifiedid.SuccessfulCompletionResult
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
) : PostNetworkOperation<SuccessfulCompletionResult>() {
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

    override suspend fun toResult(response: IResponse): Result<SuccessfulCompletionResult> {
        return serializer.decodeFromString(
            SuccessfulCompletionResult.serializer(),
            normalizeResponseBodyAsJson(response.body.decodeToString())
        ).let { Result.success(it) }
    }

    private fun normalizeResponseBodyAsJson(responseBody: String): String {
        val trimmedResponseBody = responseBody.trim()

        // Present API does not always respond with JSON.
        if (trimmedResponseBody.isBlank() || trimmedResponseBody.isEmpty() || trimmedResponseBody == "OK") {
            return "{}"
        }

        return  trimmedResponseBody
    }
}