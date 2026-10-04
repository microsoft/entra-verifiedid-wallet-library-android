/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.wrapper

import com.microsoft.walletlibrary.did.sdk.VerifiableCredentialSdk
import com.microsoft.walletlibrary.did.sdk.credential.service.PresentationRequest
import com.microsoft.walletlibrary.did.sdk.credential.service.models.oidc.PresentationRequestContent
import com.microsoft.walletlibrary.did.sdk.util.controlflow.Result
import com.microsoft.walletlibrary.requests.rawrequests.OpenIdProcessedRequest
import com.microsoft.walletlibrary.requests.rawrequests.RequestType
import com.microsoft.walletlibrary.requests.rawrequests.VerifiedIdOpenIdJwtRawRequest
import com.microsoft.walletlibrary.util.VerifiedIdRequestFetchException
import com.nimbusds.jose.JWSObject
import java.text.ParseException

/**
 * Wrapper class to wrap the get Presentation Request from VC SDK and return a raw request.
 */
object OpenIdResolver {

    // Fetches the presentation request from VC SDK using the url and converts it to raw request.
    internal suspend fun getRequest(uri: String, preferHeaders: List<String>): OpenIdProcessedRequest {
        val didResolverHardeningEnabled =
            VerifiableCredentialSdk.jwtValidator.snapshotDidResolverHardeningEnabled()
        val presentationRequestResult = VerifiableCredentialSdk.presentationService.getRequest(
            uri,
            preferHeaders,
            didResolverHardeningEnabled
        )
        return handleRequestResult(
            presentationRequestResult,
            emptyMap(),
            didResolverHardeningEnabled
        )
    }

    internal suspend fun validateRequest(requestContent: PresentationRequestContent, rawRequest: Map<String, Any>): OpenIdProcessedRequest {
        val didResolverHardeningEnabled =
            VerifiableCredentialSdk.jwtValidator.snapshotDidResolverHardeningEnabled()
        val presentationRequestResult = VerifiableCredentialSdk.presentationService.validateRequest(
            requestContent,
            didResolverHardeningEnabled
        )
        return handleRequestResult(
            presentationRequestResult,
            rawRequest,
            didResolverHardeningEnabled
        )
    }

    internal suspend fun validateSignedRequest(jwsTokenString: String): OpenIdProcessedRequest {
        return validateSignedRequest(
            jwsTokenString,
            VerifiableCredentialSdk.jwtValidator.snapshotDidResolverHardeningEnabled()
        )
    }

    internal suspend fun validateSignedRequest(
        jwsTokenString: String,
        didResolverHardeningEnabled: Boolean
    ): OpenIdProcessedRequest {
        val rawRequest = try {
            JWSObject.parse(jwsTokenString).payload.toJSONObject()
        } catch (exception: ParseException) {
            throw VerifiedIdRequestFetchException(
                "Unable to parse signed presentation request",
                exception
            )
        } ?: throw VerifiedIdRequestFetchException("Signed presentation request payload is not a JSON object")
        val presentationRequestResult =
            VerifiableCredentialSdk.presentationService.validateSignedRequest(
                jwsTokenString,
                didResolverHardeningEnabled
            )
        return handleRequestResult(
            presentationRequestResult,
            rawRequest,
            didResolverHardeningEnabled
        )
    }

    private fun handleRequestResult(
        presentationRequestResult: Result<PresentationRequest>,
        rawRequest: Map<String, Any>,
        didResolverHardeningEnabled: Boolean
    ): OpenIdProcessedRequest {
        when (presentationRequestResult) {
            is Result.Success -> {
                val request = presentationRequestResult.payload
                val requestType = getRequestType(request)
                return VerifiedIdOpenIdJwtRawRequest(
                    request,
                    requestType,
                    rawRequest,
                    didResolverHardeningEnabled
                )
            }
            is Result.Failure -> {
                throw VerifiedIdRequestFetchException(
                    "Unable to fetch presentation request",
                    presentationRequestResult.payload
                )
            }
            else -> {
                throw VerifiedIdRequestFetchException("Unable to fetch presentation request")
            }
        }
    }

    private fun getRequestType(request: PresentationRequest): RequestType {
        return if (request.content.prompt == com.microsoft.walletlibrary.util.Constants.PURE_ISSUANCE_FLOW_VALUE)
            RequestType.ISSUANCE
        else
            RequestType.PRESENTATION
    }
}