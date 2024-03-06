/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.did.sdk.datasource.network.credentialOperations

import com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredential
import com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredentialContent
import com.microsoft.walletlibrary.did.sdk.credential.service.validators.JwtValidator
import com.microsoft.walletlibrary.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.walletlibrary.did.sdk.datasource.network.PostNetworkOperation
import com.microsoft.walletlibrary.did.sdk.datasource.network.apis.HttpAgentApiProvider
import com.microsoft.walletlibrary.did.sdk.util.Constants
import com.microsoft.walletlibrary.did.sdk.util.controlflow.ForbiddenException
import com.microsoft.walletlibrary.did.sdk.util.controlflow.InvalidPinException
import com.microsoft.walletlibrary.did.sdk.util.controlflow.InvalidSignatureException
import com.microsoft.walletlibrary.util.http.httpagent.IResponse
import kotlinx.serialization.json.Json

internal class SendVerifiableCredentialIssuanceRequestNetworkOperation(
    url: String,
    serializedResponse: String,
    private val apiProvider: HttpAgentApiProvider,
    private val jwtValidator: JwtValidator,
    private val serializer: Json
) : PostNetworkOperation<VerifiableCredential>() {
    override val call: suspend () -> Result<IResponse> = { apiProvider.issuanceApis.sendResponse(url, serializedResponse) }
    override suspend fun toResult(response: IResponse): Result<VerifiableCredential> {
        val issuanceResponse = apiProvider.issuanceApis.parseIssuance(response)
        val jwsTokenString = issuanceResponse.vc
        return verifyAndUnWrapIssuanceResponse(jwsTokenString)
    }

    override fun onFailure(exception: Throwable): Result<Nothing> {
        return super.onFailure(exception).onFailure {
            when (it) {
                is ForbiddenException -> {
                    val innerErrorCode = it.innerErrorCodes?.substringBefore(",")
                    if (innerErrorCode == Constants.INVALID_PIN) {
                        val invalidPinException = InvalidPinException(exception.message ?: "", false)
                        invalidPinException.apply {
                            correlationVector = it.correlationVector
                            errorBody = it.errorBody
                            errorCode = it.errorCode
                            innerErrorCodes = it.innerErrorCodes
                        }
                        return Result.failure(invalidPinException)
                    }
                }
            }
        }
    }

    private suspend fun verifyAndUnWrapIssuanceResponse(jwsTokenString: String): Result<VerifiableCredential> {
        val jwsToken = JwsToken.deserialize(jwsTokenString)
        if (!jwtValidator.verifySignature(jwsToken))
            throw InvalidSignatureException("Signature is not Valid on Issuance Response.")
        val verifiableCredentialContent = serializer.decodeFromString(VerifiableCredentialContent.serializer(), jwsToken.content())
        return Result.success(VerifiableCredential(verifiableCredentialContent.jti, jwsTokenString, verifiableCredentialContent))
    }
}