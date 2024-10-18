/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.did.sdk.datasource.network.credentialOperations

import com.microsoft.walletlibrary.did.sdk.datasource.network.PostNetworkOperation
import com.microsoft.walletlibrary.did.sdk.datasource.network.apis.HttpAgentApiProvider
import com.microsoft.walletlibrary.util.http.httpagent.IResponse

internal class SendPresentationResponseNetworkOperation(url: String,
                                                        serializedIdToken: String,
                                                        vpToken: String,
                                                        state: String?,
                                                        apiProvider: HttpAgentApiProvider,
                                                        additionalHeaders: Map<String, String> = emptyMap()) :
    PostNetworkOperation<Unit>() {
    override val call: suspend () -> Result<IResponse> = {
        apiProvider.presentationApis.sendResponse(url, serializedIdToken, vpToken, state, additionalHeaders) }

    override suspend fun toResult(response: IResponse): Result<Unit> {
        return Result.success(Unit)
    }
}

// The plural vp_token format
internal class SendPresentationResponsesNetworkOperation(url: String,
                                                         serializedIdToken: String,
                                                         vpToken: List<String>,
                                                         state: String?,
                                                         apiProvider: HttpAgentApiProvider,
                                                         additionalHeaders: Map<String, String> = emptyMap()) :
    PostNetworkOperation<Unit>() {
    override val call: suspend () -> Result<IResponse> = {
        apiProvider.presentationApis.sendResponses(url, serializedIdToken, vpToken, state, additionalHeaders) }

    override suspend fun toResult(response: IResponse): Result<Unit> {
        return Result.success(Unit)
    }
}