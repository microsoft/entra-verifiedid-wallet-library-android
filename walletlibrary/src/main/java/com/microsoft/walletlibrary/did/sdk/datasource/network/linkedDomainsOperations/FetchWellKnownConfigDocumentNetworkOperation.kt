/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.did.sdk.datasource.network.linkedDomainsOperations

import com.microsoft.walletlibrary.did.sdk.credential.service.models.serviceResponses.LinkedDomainsResponse
import com.microsoft.walletlibrary.did.sdk.datasource.network.GetNetworkOperation
import com.microsoft.walletlibrary.did.sdk.datasource.network.apis.HttpAgentApiProvider
import com.microsoft.walletlibrary.did.sdk.util.Constants
import com.microsoft.walletlibrary.util.http.httpagent.IResponse
import java.net.URL
import javax.inject.Inject

internal class FetchWellKnownConfigDocumentNetworkOperation @Inject constructor(val url: String, private val apiProvider: HttpAgentApiProvider) :
    GetNetworkOperation<LinkedDomainsResponse>() {

    override suspend fun toResult(response: IResponse): Result<LinkedDomainsResponse> {
        return Result.success(apiProvider.linkedDomainsApis.toLinkedDomainsResponse(response))
    }

    override val call: suspend () -> Result<IResponse> =
        {
            val contextPath = URL(url)
            apiProvider.linkedDomainsApis.fetchWellKnownConfigDocument(
                URL(
                    contextPath,
                    Constants.WELL_KNOWN_CONFIG_DOCUMENT_LOCATION
                ).toString()
            )
        }
}