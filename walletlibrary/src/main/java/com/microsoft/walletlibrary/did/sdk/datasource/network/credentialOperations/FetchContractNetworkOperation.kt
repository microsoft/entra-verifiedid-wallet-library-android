/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.did.sdk.datasource.network.credentialOperations

import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.VerifiableCredentialContract
import com.microsoft.walletlibrary.did.sdk.credential.service.validators.JwtValidator
import com.microsoft.walletlibrary.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.walletlibrary.did.sdk.datasource.network.GetNetworkOperation
import com.microsoft.walletlibrary.did.sdk.datasource.network.apis.HttpAgentApiProvider
import com.microsoft.walletlibrary.did.sdk.util.controlflow.DidInHeaderAndPayloadNotMatching
import com.microsoft.walletlibrary.did.sdk.util.controlflow.InvalidSignatureException
import com.microsoft.walletlibrary.util.http.httpagent.IResponse
import kotlinx.serialization.json.Json

internal class FetchContractNetworkOperation(
    val url: String,
    private val apiProvider: HttpAgentApiProvider,
    private val jwtValidator: JwtValidator,
    private val serializer: Json
) : GetNetworkOperation<VerifiableCredentialContract>() {
    override val call: suspend () -> Result<IResponse> = { apiProvider.issuanceApis.getContract(url) }

    override suspend fun toResult(response: IResponse): Result<VerifiableCredentialContract> {
        val contract = apiProvider.issuanceApis.parseContract(response)
        val jwsTokenString = contract.token
        return verifyAndUnwrapContract(jwsTokenString)
    }

    private suspend fun verifyAndUnwrapContract(jwsTokenString: String): Result<VerifiableCredentialContract> {
        val jwsToken = JwsToken.deserialize(jwsTokenString)
        val verifiableCredentialContract = serializer.decodeFromString(VerifiableCredentialContract.serializer(), jwsToken.content())
        if (!jwtValidator.verifySignature(jwsToken))
            throw InvalidSignatureException("Signature is not valid on Issuance Request.")
        if (!jwtValidator.validateDidInHeaderAndPayload(jwsToken, verifiableCredentialContract.input.issuer))
            throw DidInHeaderAndPayloadNotMatching("DID used to sign the contract doesn't match the DID in the contract.")
        return Result.success(verifiableCredentialContract)
    }
}