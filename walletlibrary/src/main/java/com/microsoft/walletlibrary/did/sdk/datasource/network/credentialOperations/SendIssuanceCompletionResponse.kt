/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.did.sdk.datasource.network.credentialOperations

import com.microsoft.walletlibrary.did.sdk.datasource.network.PostNetworkOperation
import com.microsoft.walletlibrary.did.sdk.datasource.network.apis.HttpAgentApiProvider
import com.microsoft.walletlibrary.util.http.httpagent.IResponse

internal class SendIssuanceCompletionResponse(
    url: String,
    serializedResponse: String,
    apiProvider: HttpAgentApiProvider
) : PostNetworkOperation<Unit>() {
    override val call: suspend () -> Result<IResponse> = { apiProvider.issuanceApis.sendCompletionResponse(url, serializedResponse) }

    override suspend fun toResult(response: IResponse): Result<Unit> {
        return Result.success(Unit)
    }
}