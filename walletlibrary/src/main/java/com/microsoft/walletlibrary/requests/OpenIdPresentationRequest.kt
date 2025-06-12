/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests

import com.microsoft.walletlibrary.networking.entities.VerifiablePresentationResponse
import com.microsoft.walletlibrary.networking.operations.PostVerifiablePresentationNetworkOperation
import com.microsoft.walletlibrary.requests.rawrequests.OpenIdProcessedRequest
import com.microsoft.walletlibrary.requests.requirements.Requirement
import com.microsoft.walletlibrary.requests.serializer.PresentationExchangeResponseBuilder
import com.microsoft.walletlibrary.requests.styles.RequesterStyle
import com.microsoft.walletlibrary.util.LibraryConfiguration
import com.microsoft.walletlibrary.util.PreviewFeatureFlags
import com.microsoft.walletlibrary.util.UserCanceledException
import com.microsoft.walletlibrary.util.VerifiedIdException
import com.microsoft.walletlibrary.util.VerifiedIdExceptions
import com.microsoft.walletlibrary.util.VerifiedIdResult
import com.microsoft.walletlibrary.util.getResult
import com.microsoft.walletlibrary.verifiedid.PresentationVerified
import com.microsoft.walletlibrary.verifiedid.StringVerifiedIdSerializer
import com.microsoft.walletlibrary.wrapper.OpenIdResponder
import kotlinx.coroutines.runBlocking

/**
 * Presentation request specific to OpenId protocol.
 */
internal class OpenIdPresentationRequest(
    // Attributes describing the requester (eg. name, logo).
    override val requesterStyle: RequesterStyle,

    // Information describing the requirements needed to complete the flow.
    override val requirement: Requirement,

    // Root of trust of the requester (eg. linked domains).
    override val rootOfTrust: RootOfTrust,

    val request: OpenIdProcessedRequest,

    private val libraryConfiguration: LibraryConfiguration
) : VerifiedIdPresentationRequest, HttpProtocolRequest {
    private var additionalHeaders = emptyMap<String, String>()

    // Indicates whether presentation request is satisfied on client side.
    override fun isSatisfied(): Boolean {
        val validationResult = requirement.validate()
        //TODO("Add logging")
        return !validationResult.isFailure
    }

    // Sets additional headers to include in the response
    override fun setAdditionalHeaders(headers: Map<String, String>) {
        additionalHeaders = headers
    }

    // Completes the presentation request and returns Result with success status if successful.
    override suspend fun complete(): VerifiedIdResult<PresentationVerified> {
        return getResult {
            if (libraryConfiguration.isPreviewFeatureEnabled(PreviewFeatureFlags.FEATURE_FLAG_PRESENTATION_EXCHANGE_SERIALIZATION_SUPPORT)) {
                sendPresentationRequest()
            } else {
                sendPresentationRequestDeprecated()
            }
        }
    }

    override suspend fun cancel(message: String?): VerifiedIdResult<Unit> {
        return getResult {
            throw UserCanceledException(
                message ?: "User Canceled",
                VerifiedIdExceptions.USER_CANCELED_EXCEPTION.value
            )
        }
    }

    override fun getNonce(): String {
        return request.presentationRequest.content.nonce
    }

    private suspend fun sendPresentationRequest(): VerifiablePresentationResponse {
        val builder = PresentationExchangeResponseBuilder(libraryConfiguration)
        builder.serialize(requirement, StringVerifiedIdSerializer)
        val vpTokens = builder.buildVpTokens(
            request.presentationRequest.content.clientId,
            request.presentationRequest.content.nonce)
        val idToken = builder.buildIdToken(
            request.presentationRequest.getPresentationDefinitions().first().id,
            request.presentationRequest.content.clientId,
            request.presentationRequest.content.nonce,
        )

        PostVerifiablePresentationNetworkOperation(
            request.presentationRequest.content.redirectUrl,
            idToken,
            vpTokens,
            request.presentationRequest.content.state,
            additionalHeaders,
            libraryConfiguration.httpAgentApiProvider,
            libraryConfiguration.serializer
        ).fire()
            .onSuccess { response -> return response }
            .onFailure {
                throw VerifiedIdException(
                    "Failed to send presentation request. ${it.message}",
                    VerifiedIdExceptions.REQUEST_SEND_EXCEPTION.value
                )
            }

        throw VerifiedIdException(
            "Failed to send presentation request.",
            VerifiedIdExceptions.UNSPECIFIED_EXCEPTION.value
        )
    }

    private suspend fun sendPresentationRequestDeprecated(): PresentationVerified {
        return runBlocking {
            OpenIdResponder.sendPresentationResponse(
                request.presentationRequest,
                requirement,
                additionalHeaders,
                libraryConfiguration
            )
            VerifiablePresentationResponse()
        }
    }
}