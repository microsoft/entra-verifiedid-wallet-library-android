// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.did.sdk

import android.net.Uri
import com.microsoft.walletlibrary.did.sdk.credential.service.PresentationRequest
import com.microsoft.walletlibrary.did.sdk.credential.service.PresentationResponse
import com.microsoft.walletlibrary.did.sdk.credential.service.models.oidc.PresentationRequestContent
import com.microsoft.walletlibrary.did.sdk.credential.service.protectors.PresentationResponseFormatter
import com.microsoft.walletlibrary.did.sdk.credential.service.validators.JwtValidator
import com.microsoft.walletlibrary.did.sdk.credential.service.validators.PresentationRequestValidator
import com.microsoft.walletlibrary.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.walletlibrary.did.sdk.datasource.network.apis.HttpAgentApiProvider
import com.microsoft.walletlibrary.did.sdk.datasource.network.credentialOperations.FetchPresentationRequestNetworkOperation
import com.microsoft.walletlibrary.did.sdk.datasource.network.credentialOperations.SendPresentationResponseNetworkOperation
import com.microsoft.walletlibrary.did.sdk.datasource.network.credentialOperations.SendPresentationResponsesNetworkOperation
import com.microsoft.walletlibrary.did.sdk.identifier.models.Identifier
import com.microsoft.walletlibrary.did.sdk.identifier.resolvers.RootOfTrustResolver
import com.microsoft.walletlibrary.did.sdk.util.Constants
import com.microsoft.walletlibrary.did.sdk.util.DidDeepLinkUtil
import com.microsoft.walletlibrary.did.sdk.util.controlflow.InvalidSignatureException
import com.microsoft.walletlibrary.did.sdk.util.controlflow.PresentationException
import com.microsoft.walletlibrary.did.sdk.util.controlflow.Result
import com.microsoft.walletlibrary.did.sdk.util.controlflow.runResultTry
import com.microsoft.walletlibrary.did.sdk.util.controlflow.toSDK
import com.microsoft.walletlibrary.did.sdk.util.logTime
import com.microsoft.walletlibrary.requests.rawrequests.OpenIdRawRequest
import com.nimbusds.jose.JWSObject
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class PresentationService @Inject constructor(
    private val identifierService: IdentifierService,
    private val linkedDomainsService: LinkedDomainsService,
    private val serializer: Json,
    private val jwtValidator: JwtValidator,
    private val presentationRequestValidator: PresentationRequestValidator,
    private val apiProvider: HttpAgentApiProvider,
    private val presentationResponseFormatter: PresentationResponseFormatter
) {

    suspend fun getRequest(
        stringUri: String,
        rootOfTrustResolver: RootOfTrustResolver? = null,
        preferHeaders: List<String>
    ): Result<Pair<PresentationRequest, OpenIdRawRequest>> {
        return runResultTry {
            logTime("Presentation getRequest") {
                val uri = verifyUri(stringUri)
                val (presentationRequestContent, rawContent) = getPresentationRequestContent(uri, preferHeaders).abortOnError()
                val request = validateRequest(presentationRequestContent, rootOfTrustResolver).abortOnError()
                return@logTime Result.Success(Pair(request, rawContent))
            }
        }
    }

    internal suspend fun validateRequest(
        presentationRequestContent: PresentationRequestContent,
        rootOfTrustResolver: RootOfTrustResolver?
    ): Result<PresentationRequest> {
        return runResultTry {
            logTime("Presentation validateRequest") {
                val linkedDomainResult =
                    linkedDomainsService.fetchAndVerifyLinkedDomains(presentationRequestContent.clientId, rootOfTrustResolver).toSDK().abortOnError()
                val request = PresentationRequest(presentationRequestContent, linkedDomainResult)
                isRequestValid(request).abortOnError()
                Result.Success(request)
            }
        }
    }

    private fun verifyUri(uri: String): Uri {
        val url = Uri.parse(uri)
        if (!DidDeepLinkUtil.isDidDeepLink(url)) {
            throw PresentationException("Request Protocol not supported.")
        }
        return url
    }

    private suspend fun getPresentationRequestContent(uri: Uri, preferHeaders: List<String>): Result<Pair<PresentationRequestContent, OpenIdRawRequest>> {
        val requestParameter = uri.getQueryParameter("request")
        if (requestParameter != null)
            return verifyAndUnwrapPresentationRequestFromQueryParam(requestParameter)
        val requestUriParameter = uri.getQueryParameter("request_uri")
        if (requestUriParameter != null)
            return fetchRequest(requestUriParameter, preferHeaders).toSDK()
        return Result.Failure(PresentationException("No query parameter 'request' nor 'request_uri' is passed."))
    }

    private suspend fun isRequestValid(request: PresentationRequest): Result<Unit> {
        return runResultTry {
            presentationRequestValidator.validate(request)
            Result.Success(Unit)
        }
    }

    private suspend fun verifyAndUnwrapPresentationRequestFromQueryParam(jwsTokenString: String): Result<Pair<PresentationRequestContent, OpenIdRawRequest>> {
        val jwsObject = JWSObject.parse(jwsTokenString)
        val jwsToken = JwsToken(jwsObject)
        if (!jwtValidator.verifySignature(jwsToken))
            throw InvalidSignatureException("Signature is not valid on Presentation Request.")
        return Result.Success(Pair(serializer.decodeFromString(PresentationRequestContent.serializer(), jwsToken.content()), jwsObject.payload.toJSONObject()))
    }

    private suspend fun fetchRequest(url: String, preferHeaders: List<String>) =
        FetchPresentationRequestNetworkOperation(url, apiProvider, jwtValidator, serializer, preferHeaders).fire()

    /**
     * Send a Presentation Response.
     *
     * @param presentationRequest request being responded to
     * @param response PresentationResponse to be formed, signed, and sent.
     */
    suspend fun sendResponse(
        presentationRequest: PresentationRequest,
        response: List<PresentationResponse>,
        additionalHeaders: Map<String, String>?
    ): Result<Unit> {
        return runResultTry {
            logTime("Presentation sendResponse") {
                val masterIdentifier = identifierService.getMasterIdentifier().abortOnError()
                formAndSendResponse(presentationRequest, response, masterIdentifier,
                    additionalHeaders = additionalHeaders).abortOnError()
            }
            Result.Success(Unit)
        }
    }

    private suspend fun formAndSendResponse(
        presentationRequest: PresentationRequest,
        response: List<PresentationResponse>,
        responder: Identifier,
        expiryInSeconds: Int = Constants.DEFAULT_EXPIRATION_IN_SECONDS,
        additionalHeaders: Map<String, String>?
    ): Result<Unit> {
        // split on number of responses
        if (response.size > 1) {
            val (idToken, vpToken) = presentationResponseFormatter.formatResponses(
                request = presentationRequest,
                presentationResponses = response,
                responder = responder,
                expiryInSeconds = expiryInSeconds
            )
            return SendPresentationResponsesNetworkOperation(
                presentationRequest.content.redirectUrl,
                idToken,
                vpToken,
                presentationRequest.content.state,
                apiProvider,
                additionalHeaders
            ).fire().toSDK()

        } else {
            val (idToken, vpToken) = presentationResponseFormatter.formatResponse(
                request = presentationRequest,
                presentationResponse = response.first(),
                responder = responder,
                expiryInSeconds = expiryInSeconds
            )
            return SendPresentationResponseNetworkOperation(
                presentationRequest.content.redirectUrl,
                idToken,
                vpToken,
                presentationRequest.content.state,
                apiProvider,
                additionalHeaders
            ).fire().toSDK()
        }
    }
}