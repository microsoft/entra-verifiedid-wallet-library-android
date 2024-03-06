package com.microsoft.walletlibrary.did.sdk.datasource.network.apis

import com.microsoft.walletlibrary.did.sdk.util.Constants
import com.microsoft.walletlibrary.did.sdk.util.HttpAgentUtils
import com.microsoft.walletlibrary.util.http.URLFormEncoding
import com.microsoft.walletlibrary.util.http.httpagent.IHttpAgent
import com.microsoft.walletlibrary.util.http.httpagent.IResponse

internal class HttpAgentPresentationApis(private val agent: IHttpAgent, private val httpAgentUtils: HttpAgentUtils) {

    suspend fun getRequest(overrideUrl: String): Result<IResponse> {
        return agent.get(overrideUrl, httpAgentUtils.combineMaps(
            httpAgentUtils.defaultHeaders(),
            mapOf(
                Constants.PREFER to "JWT-interop-profile-0.0.1"
        )))
    }

    suspend fun sendResponse(
        overrideUrl: String,
        token: String,
        vpToken: String,
        state: String?
    ): Result<IResponse> {
        val body = URLFormEncoding.encode(mapOf<String, Any?>(
            "id_token" to token,
            "vp_token" to vpToken,
            "state" to state
        ))
        return agent.post(
            overrideUrl,
            httpAgentUtils.defaultHeaders(HttpAgentUtils.ContentType.UrlFormEncoded, body),
            body
        )
    }

    suspend fun sendResponses(
        overrideUrl: String,
        token: String,
        vpToken: List<String>,
        state: String?
    ): Result<IResponse> {
        val body = URLFormEncoding.encode(mapOf<String, Any?>(
            "id_token" to token,
            "vp_token" to vpToken,
            "state" to state
        ))
        return agent.post(
            overrideUrl,
            httpAgentUtils.defaultHeaders(HttpAgentUtils.ContentType.UrlFormEncoded, body),
            body
        )
    }
}