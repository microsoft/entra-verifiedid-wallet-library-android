/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.did.sdk.datasource.network.identifierOperations

import com.microsoft.walletlibrary.BooleanProvider
import com.microsoft.walletlibrary.did.sdk.crypto.protocols.jose.JwaCryptoHelper
import com.microsoft.walletlibrary.did.sdk.datasource.network.GetNetworkOperation
import com.microsoft.walletlibrary.did.sdk.datasource.network.apis.HttpAgentApiProvider
import com.microsoft.walletlibrary.did.sdk.identifier.models.identifierdocument.IdentifierResponse
import com.microsoft.walletlibrary.did.sdk.util.controlflow.ResolverException
import com.microsoft.walletlibrary.util.http.httpagent.IResponse
import javax.inject.Named

internal class ResolveIdentifierNetworkOperation(
    private val apiProvider: HttpAgentApiProvider,
    url: String,
    val identifier: String,
    @Named("didResolverHardeningEnabledProvider")
    private val didResolverHardeningEnabledProvider: BooleanProvider
) :
    GetNetworkOperation<IdentifierResponse>() {

    override val call: suspend () -> Result<IResponse> = {
        // Reject identifiers containing characters that could redirect the request to an unintended path
        // (e.g. '/', '?', '#', whitespace, '..') before they are concatenated into the resolver URL.
        if (didResolverHardeningEnabledProvider.get() &&
            (identifier.isBlank() || !JwaCryptoHelper.isSyntacticallyValidDid(identifier))
        ) {
            throw ResolverException("Identifier '$identifier' is not a syntactically valid DID")
        }
        apiProvider.identifierApi.resolveIdentifier("$url/$identifier")
    }

    override suspend fun toResult(response: IResponse): Result<IdentifierResponse> {
        return Result.success(apiProvider.identifierApi.toIdentifierResponse(response))
    }
}