package com.microsoft.walletlibrary.networking.operations

import com.microsoft.walletlibrary.did.sdk.datasource.network.GetNetworkOperation
import com.microsoft.walletlibrary.did.sdk.datasource.network.apis.HttpAgentApiProvider
import com.microsoft.walletlibrary.util.http.httpagent.IResponse

internal class FetchOpenID4VCIRequestNetworkOperation(
    private val url: String,
    private val preferHeaders: List<String>,
    private val apiProvider: HttpAgentApiProvider
) : GetNetworkOperation<ByteArray>() {
    override val call: suspend () -> Result<IResponse> =
        { apiProvider.openId4VciApi.getOpenID4VCIRequest(url, preferHeaders) }

    override suspend fun toResult(response: IResponse): Result<ByteArray> {
        return Result.success(response.body)
    }
}