/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests.handlers

import com.microsoft.walletlibrary.did.sdk.identifier.resolvers.RootOfTrustResolver
import com.microsoft.walletlibrary.mappings.issuance.toVerifiedIdStyle
import com.microsoft.walletlibrary.requests.ManifestIssuanceRequest
import com.microsoft.walletlibrary.requests.OpenIdPresentationRequest
import com.microsoft.walletlibrary.requests.PresentationRequestContent
import com.microsoft.walletlibrary.requests.VerifiedIdRequest
import com.microsoft.walletlibrary.requests.input.VerifiedIdRequestURL
import com.microsoft.walletlibrary.requests.rawrequests.OpenIdRawRequest
import com.microsoft.walletlibrary.requests.rawrequests.RawManifest
import com.microsoft.walletlibrary.requests.rawrequests.RawRequest
import com.microsoft.walletlibrary.requests.rawrequests.RequestType
import com.microsoft.walletlibrary.requests.requestProcessorExtensions.RequestProcessorExtension
import com.microsoft.walletlibrary.requests.requirements.VerifiedIdRequirement
import com.microsoft.walletlibrary.util.InputCastingException
import com.microsoft.walletlibrary.util.RequirementCastingException
import com.microsoft.walletlibrary.util.UnSupportedProtocolException
import com.microsoft.walletlibrary.verifiedid.VerifiedId
import com.microsoft.walletlibrary.wrapper.ManifestResolver

/**
 * OIDC protocol specific implementation of RequestProcessor. It can handle OpenID raw request and returns a VerifiedIdRequest.
 */
class OpenIdRequestProcessor: RequestProcessor {

    /**
     * Extensions to this RequestProcessor. All extensions should be called after initial request
     * processing to mutate the request with additional input.
     */
    override var requestProcessors: List<RequestProcessorExtension> = emptyList()

    /**
     * Checks if the input can be processed
     * @param rawRequest A primitive form of the request
     * @return if the input is understood by the processor and can be processed
     */
    override suspend fun canHandleRequest(rawRequest: Any): Boolean {
        // TODO: This and handleRequest need to be refactored to accept a string.
        return rawRequest is OpenIdRawRequest
    }

    override suspend fun handleRequest(rawRequest: Any, rootOfTrustResolver: RootOfTrustResolver?): VerifiedIdRequest<*> {
        if (rawRequest !is OpenIdRawRequest)
            throw UnSupportedProtocolException("Received a raw request of unsupported protocol")
        val presentationRequestContent = rawRequest.mapToPresentationRequestContent()
        var request = if (rawRequest.requestType == RequestType.ISSUANCE)
            handleIssuanceRequest(presentationRequestContent, rootOfTrustResolver)
        else
            handlePresentationRequest(presentationRequestContent, rawRequest)
        this.requestProcessors.forEach { extension ->
            request = extension.parse(rawRequest, request)
        }
        return request
    }

    private fun handlePresentationRequest(
        presentationRequestContent: PresentationRequestContent,
        rawRequest: OpenIdRawRequest
    ): VerifiedIdRequest<Unit> {
        return OpenIdPresentationRequest(
            presentationRequestContent.requesterStyle,
            presentationRequestContent.requirement,
            presentationRequestContent.rootOfTrust,
            rawRequest
        )
    }

    private suspend fun handleIssuanceRequest(
        presentationRequestContent: PresentationRequestContent,
        rootOfTrustResolver: RootOfTrustResolver? = null
    ): VerifiedIdRequest<VerifiedId> {
        validateRequirement(presentationRequestContent)
        val contractUrl =
            ((presentationRequestContent.requirement as VerifiedIdRequirement).issuanceOptions.first() as VerifiedIdRequestURL).url
        val rawManifest = getIssuanceRequest(
            contractUrl.toString(),
            presentationRequestContent.requestState,
            presentationRequestContent.issuanceCallbackUrl,
            rootOfTrustResolver
        )
        val issuanceRequestContent = rawManifest.mapToIssuanceRequestContent()
        presentationRequestContent.injectedIdToken?.let {
            issuanceRequestContent.addRequirementsForIdTokenHint(
                it
            )
        }
        return ManifestIssuanceRequest(
            issuanceRequestContent.requesterStyle,
            issuanceRequestContent.requirement,
            issuanceRequestContent.rootOfTrust,
            rawManifest.rawRequest.contract.display.toVerifiedIdStyle(),
            rawManifest,
            presentationRequestContent.issuanceCallbackUrl,
            presentationRequestContent.requestState
        )
    }

    private fun validateRequirement(requestContent: PresentationRequestContent) {
        if (requestContent.requirement !is VerifiedIdRequirement)
            throw RequirementCastingException("Requirement is not the expected VerifiedId Requirement")
        if ((requestContent.requirement as VerifiedIdRequirement).issuanceOptions.first() !is VerifiedIdRequestURL)
            throw InputCastingException("VerifiedId Input is not the expected VerifiedIdRequestURL type")
    }

    private suspend fun getIssuanceRequest(
        contractUrl: String,
        requestState: String?,
        issuanceCallbackUrl: String?,
        rootOfTrustResolver: RootOfTrustResolver? = null
    ): RawManifest {
        return ManifestResolver.getIssuanceRequest(contractUrl, requestState, issuanceCallbackUrl, rootOfTrustResolver)
    }
}