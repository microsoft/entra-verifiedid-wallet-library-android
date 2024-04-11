package com.microsoft.walletlibrary.networking.operations

import com.microsoft.walletlibrary.did.sdk.datasource.network.GetNetworkOperation
import com.microsoft.walletlibrary.did.sdk.datasource.network.apis.HttpAgentApiProvider
import com.microsoft.walletlibrary.networking.entities.openid4vci.wellknownconfig.OpenIdWellKnownConfig
import com.microsoft.walletlibrary.util.http.httpagent.IResponse
import kotlinx.serialization.json.Json

/**
 * Network operation to fetch the token endpoint from openId well known configuration for the issuer.
 */
internal class FetchOpenIdWellKnownConfigNetworkOperation(
    private val url: String,
    private val apiProvider: HttpAgentApiProvider,
    private val serializer: Json
) : GetNetworkOperation<OpenIdWellKnownConfig>() {
    override val call: suspend () -> Result<IResponse> =
        { apiProvider.openId4VciApi.getOpenIdWellKnownConfig(url) }

    override suspend fun toResult(response: IResponse): Result<OpenIdWellKnownConfig> {
        return Result.success(
            serializer.decodeFromString(
                OpenIdWellKnownConfig.serializer(),
                response.body.decodeToString()
            )
        )
    }
}