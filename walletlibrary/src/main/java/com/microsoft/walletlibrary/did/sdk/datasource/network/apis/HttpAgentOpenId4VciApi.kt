package com.microsoft.walletlibrary.did.sdk.datasource.network.apis

import com.microsoft.walletlibrary.did.sdk.util.Constants
import com.microsoft.walletlibrary.did.sdk.util.HttpAgentUtils
import com.microsoft.walletlibrary.util.http.URLFormEncoding
import com.microsoft.walletlibrary.util.http.httpagent.IHttpAgent
import com.microsoft.walletlibrary.util.http.httpagent.IResponse
import kotlinx.serialization.json.Json

/**
 * Api class to perform OpenId4VCI related network operations using the provided HttpAgent, utils
 * and json serializer to convert the network response to Issuance related model.
 */
internal class HttpAgentOpenId4VciApi(
    private val agent: IHttpAgent,
    private val httpAgentUtils: HttpAgentUtils,
    private val json: Json
) {
    companion object {
        const val OPENID4VCI_INTER_OP_PROFILE = "oid4vci-interop-profile-version=0.0.1"
        private val openId4VciInteropHeadersMap = mapOf(Constants.PREFER to OPENID4VCI_INTER_OP_PROFILE)
    }

    suspend fun getOpenID4VCIRequest(overrideUrl: String): Result<IResponse> {
        return agent.get(
            overrideUrl,
            combineAdditionalHeadersWithDefaultHeaders(
                openId4VciInteropHeadersMap,
                httpAgentUtils.defaultHeaders()
            )
        )
    }

    suspend fun getCredentialMetadata(overrideUrl: String): Result<IResponse> {
        return agent.get(
            overrideUrl,
            combineAdditionalHeadersWithDefaultHeaders(
                openId4VciInteropHeadersMap,
                httpAgentUtils.defaultHeaders()
            )
        )
    }

    suspend fun getOpenIdWellKnownConfig(overrideUrl: String): Result<IResponse> =
        agent.get(overrideUrl, httpAgentUtils.defaultHeaders())

    suspend fun postOpenID4VCIRequest(
        overrideUrl: String,
        rawOpenID4VCIRequest: String,
        accessToken: String
    ): Result<IResponse> {
        val bodyBytes = rawOpenID4VCIRequest.encodeToByteArray()
        return agent.post(
            overrideUrl,
            combineAdditionalHeadersWithDefaultHeaders(
                mapOf(
                    Constants.AUTHORIZATION to "Bearer $accessToken",
                    Constants.PREFER to OPENID4VCI_INTER_OP_PROFILE
                ),
                httpAgentUtils.defaultHeaders(HttpAgentUtils.ContentType.Json, bodyBytes)
            ),
            bodyBytes
        )
    }

    suspend fun postOpenID4VCIPreAuthToken(
        overrideUrl: String,
        grantType: String,
        preAuthorizedCode: String,
        txCode: String?
    ): Result<IResponse> {
        val bodyToBeEncoded = mutableMapOf<String, Any?>(
            "grant_type" to grantType,
            "pre-authorized_code" to preAuthorizedCode,
        )
        txCode?.let { bodyToBeEncoded["tx_code"] = it }
        val body = URLFormEncoding.encode(bodyToBeEncoded)
        return agent.post(
            overrideUrl,
            combineAdditionalHeadersWithDefaultHeaders(
                mapOf(Constants.PREFER to OPENID4VCI_INTER_OP_PROFILE),
                httpAgentUtils.defaultHeaders(HttpAgentUtils.ContentType.UrlFormEncoded, body)
            ),
            body
        )
    }

    private fun combineAdditionalHeadersWithDefaultHeaders(
        additionalHeaders: Map<String, String>,
        defaultHeaders: Map<String, String>
    ) = httpAgentUtils.combineMaps(defaultHeaders, additionalHeaders)
}