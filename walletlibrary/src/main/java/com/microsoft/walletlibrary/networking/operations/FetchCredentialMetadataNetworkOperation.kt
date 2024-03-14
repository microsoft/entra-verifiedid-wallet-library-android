package com.microsoft.walletlibrary.networking.operations

import com.microsoft.walletlibrary.did.sdk.datasource.network.GetNetworkOperation
import com.microsoft.walletlibrary.did.sdk.datasource.network.apis.HttpAgentApiProvider
import com.microsoft.walletlibrary.networking.entities.openid4vci.credentialmetadata.CredentialMetadata
import com.microsoft.walletlibrary.util.http.httpagent.IResponse
import kotlinx.serialization.json.Json

internal class FetchCredentialMetadataNetworkOperation(
    private val url: String,
    private val apiProvider: HttpAgentApiProvider,
    private val serializer: Json
) : GetNetworkOperation<CredentialMetadata>() {
    override val call: suspend () -> Result<IResponse> =
        { apiProvider.openId4VciApi.getCredentialMetadata(url) }

    override suspend fun toResult(response: IResponse): Result<CredentialMetadata> {
        return Result.success(
            serializer.decodeFromString(
                CredentialMetadata.serializer(),
                response.body.decodeToString()
            )
        )
    }
}