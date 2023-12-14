/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.did.sdk.datasource.network.credentialOperations

import com.microsoft.walletlibrary.did.sdk.credential.models.RevocationReceipt
import com.microsoft.walletlibrary.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.walletlibrary.did.sdk.datasource.network.PostNetworkOperation
import com.microsoft.walletlibrary.did.sdk.datasource.network.apis.HttpAgentApiProvider
import com.microsoft.walletlibrary.did.sdk.util.controlflow.RevocationException
import com.microsoft.walletlibrary.util.http.httpagent.IResponse
import kotlinx.serialization.json.Json

internal class SendVerifiablePresentationRevocationRequestNetworkOperation(
    url: String,
    serializedResponse: String,
    private val apiProvider: HttpAgentApiProvider,
    private val serializer: Json
) : PostNetworkOperation<RevocationReceipt>() {
    override val call: suspend () -> Result<IResponse> =
        { apiProvider.revocationApis.sendResponse(url, serializedResponse) }

    override suspend fun toResult(response: IResponse): Result<RevocationReceipt> {
        val revokeResponse = apiProvider.revocationApis.toResponse(response)
        val receipts = revokeResponse.receipt.entries
        if (receipts.isEmpty())
            return Result.failure(RevocationException("No Receipt in revocation response body"))
        val serializedReceipt = receipts.first().value
        val revocationReceipt = unwrapRevocationReceipt(serializedReceipt, serializer)
        return Result.success(revocationReceipt)
    }

    fun unwrapRevocationReceipt(signedReceipt: String, serializer: Json): RevocationReceipt {
        val token = JwsToken.deserialize(signedReceipt)
        return serializer.decodeFromString(RevocationReceipt.serializer(), token.content())
    }
}