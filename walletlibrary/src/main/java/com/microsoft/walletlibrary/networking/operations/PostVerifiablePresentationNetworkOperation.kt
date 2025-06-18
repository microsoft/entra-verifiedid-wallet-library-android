// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.networking.operations

import com.microsoft.walletlibrary.did.sdk.datasource.network.PostNetworkOperation
import com.microsoft.walletlibrary.did.sdk.datasource.network.apis.HttpAgentApiProvider
import com.microsoft.walletlibrary.networking.entities.EmptyResponse
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
        val normalizedBody = preprocessResponseBody(response.body.decodeToString())

        // We deal with empty response explicitly since polymorphic serialization requires at least one property (either optional
        // or required) to discriminate.
        if (normalizedBody.isNullOrEmpty()) {
            return Result.success(EmptyResponse())
        }

        return serializer.decodeFromString(SuccessfulCompletionResult.serializer(), normalizedBody).let {
            Result.success(it)
        }
    }

    private fun preprocessResponseBody(responseBody: String): String? {
        val trimmedResponseBody = responseBody.trim()

        // Present API does not always respond with JSON, so we can treat "OK" as empty.
        if (trimmedResponseBody == "OK") {
            return null
        }

        return  trimmedResponseBody
    }
}