/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.did.sdk.datasource.network.credentialOperations

import com.microsoft.walletlibrary.did.sdk.credential.service.models.oidc.PresentationRequestContent
import com.microsoft.walletlibrary.did.sdk.credential.service.validators.JwtValidator
import com.microsoft.walletlibrary.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.walletlibrary.did.sdk.datasource.network.GetNetworkOperation
import com.microsoft.walletlibrary.did.sdk.datasource.network.apis.HttpAgentApiProvider
import com.microsoft.walletlibrary.did.sdk.util.controlflow.DidInHeaderAndPayloadNotMatching
import com.microsoft.walletlibrary.did.sdk.util.controlflow.ExpiredTokenException
import com.microsoft.walletlibrary.did.sdk.util.controlflow.InvalidSignatureException
import com.microsoft.walletlibrary.did.sdk.util.controlflow.NotFoundException
import com.microsoft.walletlibrary.util.http.httpagent.IResponse
import kotlinx.serialization.json.Json

//TODO("improve onSuccess method to create receipt when this is spec'd out")
internal class FetchPresentationRequestNetworkOperation(
    private val url: String,
    private val apiProvider: HttpAgentApiProvider,
    private val jwtValidator: JwtValidator,
    private val serializer: Json
) : GetNetworkOperation<PresentationRequestContent>() {
    override val call: suspend () -> Result<IResponse> = { apiProvider.presentationApis.getRequest(url) }

    override suspend fun toResult(response: IResponse): Result<PresentationRequestContent> {
        val jwsTokenString = response.body.decodeToString()
        return verifyAndUnwrapPresentationRequest(jwsTokenString)
    }

    override fun onFailure(exception: Throwable): Result<Nothing> {
        return super.onFailure(exception).onFailure {
            if (it is NotFoundException) {
                val expiredTokenException = ExpiredTokenException(exception.message ?: "", false)
                expiredTokenException.apply {
                    correlationVector = it.correlationVector
                    errorBody = it.errorBody
                    errorCode = it.errorCode
                    innerErrorCodes = it.innerErrorCodes
                }
                return Result.failure(expiredTokenException)
            }
        }
    }

    private suspend fun verifyAndUnwrapPresentationRequest(jwsTokenString: String): Result<PresentationRequestContent> {
        val jwsToken = JwsToken.deserialize(jwsTokenString)
        val presentationRequestContent = serializer.decodeFromString(PresentationRequestContent.serializer(), jwsToken.content())
        if (!jwtValidator.verifySignature(jwsToken))
            throw InvalidSignatureException("Signature is not valid on Presentation Request.")
        if (!jwtValidator.validateDidInHeaderAndPayload(jwsToken, presentationRequestContent.clientId))
            throw DidInHeaderAndPayloadNotMatching("DID used to sign the presentation request doesn't match the DID in presentation request.")
        return Result.success(presentationRequestContent)
    }
}