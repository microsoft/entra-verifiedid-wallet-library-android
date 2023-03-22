/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests.handlers

import com.microsoft.walletlibrary.mappings.issuance.toVerifiedIdStyle
import com.microsoft.walletlibrary.requests.ManifestIssuanceRequest
import com.microsoft.walletlibrary.requests.OpenIdPresentationRequest
import com.microsoft.walletlibrary.requests.VerifiedIdRequest
import com.microsoft.walletlibrary.requests.VerifiedIdRequestContent
import com.microsoft.walletlibrary.requests.input.VerifiedIdRequestURL
import com.microsoft.walletlibrary.requests.rawrequests.OpenIdRawRequest
import com.microsoft.walletlibrary.requests.rawrequests.RawManifest
import com.microsoft.walletlibrary.requests.rawrequests.RawRequest
import com.microsoft.walletlibrary.requests.rawrequests.RequestType
import com.microsoft.walletlibrary.requests.requirements.VerifiedIdRequirement
import com.microsoft.walletlibrary.util.InputCastingException
import com.microsoft.walletlibrary.util.RequirementCastingException
import com.microsoft.walletlibrary.util.UnSupportedProtocolException
import com.microsoft.walletlibrary.verifiedid.VerifiedId
import com.microsoft.walletlibrary.wrapper.ManifestResolver

/**
 * OIDC protocol specific implementation of RequestHandler. It can handle OpenID raw request and returns a VerifiedIdRequest.
 */
internal class OpenIdRequestHandler: RequestHandler {

    override suspend fun handleRequest(rawRequest: RawRequest): VerifiedIdRequest<*> {
        if (rawRequest !is OpenIdRawRequest)
            throw UnSupportedProtocolException("Received a raw request of unsupported protocol")
        val requestContent = rawRequest.mapToRequestContent()
        if (rawRequest.requestType == RequestType.ISSUANCE)
            return handleIssuanceRequest(requestContent)
        return handlePresentationRequest(requestContent, rawRequest)
    }

    private fun handlePresentationRequest(
        requestContent: VerifiedIdRequestContent,
        rawRequest: OpenIdRawRequest
    ): VerifiedIdRequest<Unit> {
        return OpenIdPresentationRequest(
            requestContent.requesterStyle,
            requestContent.requirement,
            requestContent.rootOfTrust,
            rawRequest
        )
    }

    private suspend fun handleIssuanceRequest(requestContent: VerifiedIdRequestContent): VerifiedIdRequest<VerifiedId> {
        validateRequirement(requestContent)
        val contractUrl =
            ((requestContent.requirement as VerifiedIdRequirement).issuanceOptions.first() as VerifiedIdRequestURL).url
        val rawManifest = getIssuanceRequest(contractUrl.toString())
        val issuanceRequestContent = rawManifest.mapToRequestContent()
        requestContent.injectedIdToken?.let {
            issuanceRequestContent.addRequirementsForIdTokenHint(
                it
            )
        }
        return ManifestIssuanceRequest(
            issuanceRequestContent.requesterStyle,
            issuanceRequestContent.requirement,
            issuanceRequestContent.rootOfTrust,
            rawManifest.rawRequest.contract.display.toVerifiedIdStyle(),
            rawManifest
        )
    }

    private fun validateRequirement(requestContent: VerifiedIdRequestContent) {
        if (requestContent.requirement !is VerifiedIdRequirement)
            throw RequirementCastingException("Requirement is not the expected VerifiedId Requirement")
        if ((requestContent.requirement as VerifiedIdRequirement).issuanceOptions.first() !is VerifiedIdRequestURL)
            throw InputCastingException("VerifiedId Input is not the expected VerifiedIdRequestURL type")
    }

    private suspend fun getIssuanceRequest(contractUrl: String): RawManifest {
        return ManifestResolver.getIssuanceRequest(contractUrl)
    }
}