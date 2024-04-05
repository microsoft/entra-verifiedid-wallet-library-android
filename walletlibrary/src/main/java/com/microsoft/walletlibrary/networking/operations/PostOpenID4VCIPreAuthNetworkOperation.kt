package com.microsoft.walletlibrary.networking.operations

import com.microsoft.walletlibrary.did.sdk.datasource.network.PostNetworkOperation
import com.microsoft.walletlibrary.did.sdk.datasource.network.apis.HttpAgentApiProvider
import com.microsoft.walletlibrary.networking.entities.openid4vci.OpenID4VCIPreAuthTokenResponse
import com.microsoft.walletlibrary.networking.entities.openid4vci.request.OpenID4VCIPreAuthTokenRequest
import com.microsoft.walletlibrary.util.http.httpagent.IResponse
import kotlinx.serialization.json.Json

internal class PostOpenID4VCIPreAuthNetworkOperation(
    private val url: String,
    private val openID4VCIPreAuthTokenRequest: OpenID4VCIPreAuthTokenRequest,
    private val apiProvider: HttpAgentApiProvider,
    private val serializer: Json
) : PostNetworkOperation<OpenID4VCIPreAuthTokenResponse>() {
    override val call: suspend () -> Result<IResponse> =
        {
            apiProvider.openId4VciApi.postOpenID4VCIPreAuthToken(
                url,
                openID4VCIPreAuthTokenRequest.grant_type,
                openID4VCIPreAuthTokenRequest.pre_authorized_code,
                openID4VCIPreAuthTokenRequest.tx_code
            )
        }

    override suspend fun toResult(response: IResponse): Result<OpenID4VCIPreAuthTokenResponse> {
        return serializer.decodeFromString(
            OpenID4VCIPreAuthTokenResponse.serializer(),
            response.body.decodeToString()
        ).let { Result.success(it) }
    }
}