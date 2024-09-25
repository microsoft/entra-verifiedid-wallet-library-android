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
    suspend fun getOpenID4VCIRequest(
        overrideUrl: String,
        preferHeaders: List<String>
    ): Result<IResponse> {
        val headers =
            mutableListOf(com.microsoft.walletlibrary.util.Constants.OPENID4VCI_INTER_OP_PROFILE)
        headers.addAll(preferHeaders)
        return agent.get(
            overrideUrl,
            combineAdditionalHeadersWithDefaultHeaders(
                mapOf(
                    Constants.PREFER to httpAgentUtils.formatPreferValues(headers)
                )
            )
        )
    }

    suspend fun getCredentialMetadata(overrideUrl: String): Result<IResponse> {
        val headers =
            mutableListOf(com.microsoft.walletlibrary.util.Constants.OPENID4VCI_INTER_OP_PROFILE)
        return agent.get(
            overrideUrl,
            combineAdditionalHeadersWithDefaultHeaders(
                mapOf(
                    Constants.PREFER to httpAgentUtils.formatPreferValues(headers)
                )
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
                    Constants.PREFER to com.microsoft.walletlibrary.util.Constants.OPENID4VCI_INTER_OP_PROFILE
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
        txCode: String
    ): Result<IResponse> {
        val body = URLFormEncoding.encode(
            mapOf<String, Any?>(
                "grant_type" to grantType,
                "pre-authorized_code" to preAuthorizedCode,
                "tx_code" to txCode
            )
        )
        return agent.post(
            overrideUrl,
            combineAdditionalHeadersWithDefaultHeaders(
                mapOf(Constants.PREFER to com.microsoft.walletlibrary.util.Constants.OPENID4VCI_INTER_OP_PROFILE),
                httpAgentUtils.defaultHeaders(HttpAgentUtils.ContentType.UrlFormEncoded, body)
            ),
            body
        )
    }

    private fun combineAdditionalHeadersWithDefaultHeaders(additionalHeaders: Map<String, String>): Map<String, String> {
        return httpAgentUtils.combineMaps(
            httpAgentUtils.defaultHeaders(),
            additionalHeaders
        )
    }

    private fun combineAdditionalHeadersWithDefaultHeaders(
        additionalHeaders: Map<String, String>,
        defaultHeaders: Map<String, String>
    ) = httpAgentUtils.combineMaps(defaultHeaders, additionalHeaders)
}