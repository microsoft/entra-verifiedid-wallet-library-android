// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.networking.operations

import com.microsoft.walletlibrary.did.sdk.datasource.network.PostNetworkOperation
import com.microsoft.walletlibrary.did.sdk.datasource.network.apis.HttpAgentApiProvider
import com.microsoft.walletlibrary.networking.entities.openid4vci.RawOpenID4VCIResponse
import com.microsoft.walletlibrary.util.http.httpagent.IResponse
import kotlinx.serialization.json.Json

<<<<<<< HEAD
=======
/**
 * Network operation to make a post call to issuer with OpenID4VCI request and access token
 * to receive the credential.
 */
>>>>>>> dev
internal class PostOpenID4VCINetworkOperation(
    private val url: String,
    private val serializedToken: String,
    private val accessToken: String,
    private val apiProvider: HttpAgentApiProvider,
    private val serializer: Json
) : PostNetworkOperation<RawOpenID4VCIResponse>() {
    override val call: suspend () -> Result<IResponse> =
        { apiProvider.openId4VciApi.postOpenID4VCIRequest(url, serializedToken, accessToken) }

    override suspend fun toResult(response: IResponse): Result<RawOpenID4VCIResponse> {
        return serializer.decodeFromString(
            RawOpenID4VCIResponse.serializer(),
            response.body.decodeToString()
        ).let { Result.success(it) }
    }
}