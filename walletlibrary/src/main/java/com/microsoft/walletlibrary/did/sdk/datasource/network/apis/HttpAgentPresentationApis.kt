package com.microsoft.walletlibrary.did.sdk.datasource.network.apis

import com.microsoft.walletlibrary.did.sdk.util.Constants
import com.microsoft.walletlibrary.util.http.URLFormEncoding
import com.microsoft.walletlibrary.util.http.httpagent.IHttpAgent
import com.microsoft.walletlibrary.util.http.httpagent.IResponse

internal class HttpAgentPresentationApis(private val agent: IHttpAgent) {

    suspend fun getRequest(overrideUrl: String): Result<IResponse> {
        return agent.get(overrideUrl, mapOf(
            Constants.PREFER to "JWT-interop-profile-0.0.1"
        ))
    }

    suspend fun sendResponse(
        overrideUrl: String,
        token: String,
        vpToken: String,
        state: String?
    ): Result<IResponse> {
        return agent.post(
            overrideUrl,
            mapOf(
                Constants.CONTENT_TYPE to URLFormEncoding.mimeType
            ),
            URLFormEncoding.encode(mapOf<String, Any?>(
                "id_token" to token,
                "vp_token" to vpToken,
                "state" to state
            ))
        )
    }

    suspend fun sendResponses(
        overrideUrl: String,
        token: String,
        vpToken: List<String>,
        state: String?
    ): Result<IResponse> {
        return agent.post(
            overrideUrl,
            mapOf(
                Constants.CONTENT_TYPE to URLFormEncoding.mimeType
            ),
            URLFormEncoding.encode(mapOf<String, Any?>(
                "id_token" to token,
                "vp_token" to vpToken,
                "state" to state
            ))
        )
    }

}