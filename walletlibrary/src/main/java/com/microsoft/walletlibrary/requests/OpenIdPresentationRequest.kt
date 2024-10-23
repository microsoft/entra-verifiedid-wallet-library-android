/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests

import com.microsoft.walletlibrary.requests.rawrequests.OpenIdProcessedRequest
import com.microsoft.walletlibrary.requests.requirements.Requirement
import com.microsoft.walletlibrary.requests.serializer.PresentationExchangeResponseBuilder
import com.microsoft.walletlibrary.requests.styles.RequesterStyle
import com.microsoft.walletlibrary.util.LibraryConfiguration
import com.microsoft.walletlibrary.util.PreviewFeatureFlags
import com.microsoft.walletlibrary.util.UserCanceledException
import com.microsoft.walletlibrary.util.VerifiedIdExceptions
import com.microsoft.walletlibrary.util.VerifiedIdResult
import com.microsoft.walletlibrary.util.getResult
import com.microsoft.walletlibrary.verifiedid.StringVerifiedIdSerializer
import com.microsoft.walletlibrary.wrapper.OpenIdResponder

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
        // TODO("Add logging")
        return !validationResult.isFailure
    }

    // Sets additional headers to include in the response
    override fun setAdditionalHeaders(headers: Map<String, String>) {
        additionalHeaders = headers
    }

    // Completes the presentation request and returns Result with success status if successful.
    override suspend fun complete(): VerifiedIdResult<Unit> {
        return getResult {
            if (libraryConfiguration.isPreviewFeatureEnabled(PreviewFeatureFlags.FEATURE_FLAG_PRESENTATION_EXCHANGE_SERIALIZATION_SUPPORT)) {
                val builder = PresentationExchangeResponseBuilder(libraryConfiguration)
                builder.serialize(requirement, StringVerifiedIdSerializer)
                val vpTokens = builder.buildVpTokens(
                    request.presentationRequest.content.clientId,
                    request.presentationRequest.content.nonce
                )
                val idToken = builder.buildIdToken(
                    request.presentationRequest.getPresentationDefinitions().first().id,
                    request.presentationRequest.content.clientId,
                    request.presentationRequest.content.nonce,
                )

                val result = if (vpTokens.size > 1) {
                    libraryConfiguration.httpAgentApiProvider.presentationApis.sendResponses(
                        request.presentationRequest.content.redirectUrl,
                        idToken,
                        vpTokens,
                        request.presentationRequest.content.state,
                        additionalHeaders
                    )
                } else {
                    libraryConfiguration.httpAgentApiProvider.presentationApis.sendResponse(
                        request.presentationRequest.content.redirectUrl,
                        idToken,
                        vpTokens.first(),
                        request.presentationRequest.content.state,
                        additionalHeaders
                    )
                }
                result.exceptionOrNull()?.let {
                    throw it
                }
            } else {
                OpenIdResponder.sendPresentationResponse(
                    request.presentationRequest,
                    requirement,
                    additionalHeaders,
                    libraryConfiguration.serializer
                )
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
}