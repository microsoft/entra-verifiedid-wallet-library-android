/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.did.sdk.datasource.network.identifierOperations

import com.microsoft.walletlibrary.did.sdk.datasource.network.GetNetworkOperation
import com.microsoft.walletlibrary.did.sdk.datasource.network.apis.HttpAgentApiProvider
import com.microsoft.walletlibrary.did.sdk.identifier.models.identifierdocument.IdentifierResponse
import com.microsoft.walletlibrary.did.sdk.util.controlflow.ResolverException
import com.microsoft.walletlibrary.util.http.httpagent.IResponse
import javax.inject.Inject

internal class ResolveIdentifierNetworkOperation @Inject constructor(private val apiProvider: HttpAgentApiProvider, url: String, val identifier: String) :
    GetNetworkOperation<IdentifierResponse>() {

    // Reject identifiers containing characters that could redirect the request to an unintended path
    // (e.g. '/', '?', '#', whitespace, '..') before they are concatenated into the resolver URL.
    private val sanitizedIdentifier: String = identifier.also {
        if (it.isBlank() || !Regex("^did:[a-zA-Z0-9]+:[A-Za-z0-9._%-]+(?::[A-Za-z0-9._%-]+)*$").matches(it)) {
            throw ResolverException("Identifier '$it' is not a syntactically valid DID")
        }
    }

    override val call: suspend () -> Result<IResponse> = { apiProvider.identifierApi.resolveIdentifier("$url/$sanitizedIdentifier") }
    override suspend fun toResult(response: IResponse): Result<IdentifierResponse> {
        return Result.success(apiProvider.identifierApi.toIdentifierResponse(response))
    }
}