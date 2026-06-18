/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.did.sdk.datasource.network.apis

import com.microsoft.walletlibrary.did.sdk.util.HttpAgentUtils
import com.microsoft.walletlibrary.util.http.httpagent.IHttpAgent
import com.microsoft.walletlibrary.util.http.httpagent.IResponse

/**
 * API class for fetching W3C StatusList2021 status list credentials from an issuer's endpoint.
 */
internal class HttpAgentStatusListApi(
    private val agent: IHttpAgent,
    private val httpAgentUtils: HttpAgentUtils
) {
    /**
     * Fetches a StatusList2021 credential directly from an HTTPS [url] (the `statusListCredential`
     * URL on the credential's `credentialStatus` entry).
     *
     * @see [Status List 2021](https://www.w3.org/community/reports/credentials/CG-FINAL-vc-status-list-2021-20230102/)
     */
    suspend fun getStatusListCredential(url: String): Result<IResponse> {
        return agent.get(url, httpAgentUtils.defaultHeaders())
    }

    /**
     * Fetches a StatusList2021 credential from an issuer's IdentityHub by POSTing a CollectionsQuery
     * message to [url]. Used by older Entra Verified ID credentials whose `credentialStatus.id` is a
     * `urn:uuid:` reference rather than a direct HTTPS URL; the [body] is the CollectionsQuery
     * envelope produced by the caller.
     *
     * @see [Status List 2021](https://www.w3.org/community/reports/credentials/CG-FINAL-vc-status-list-2021-20230102/)
     */
    suspend fun postCollectionsQuery(url: String, body: String): Result<IResponse> {
        val bodyBytes = body.toByteArray(Charsets.UTF_8)
        return agent.post(url, httpAgentUtils.defaultHeaders(HttpAgentUtils.ContentType.Json, bodyBytes), bodyBytes)
    }
}
