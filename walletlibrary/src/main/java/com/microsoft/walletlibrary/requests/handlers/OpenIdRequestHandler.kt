/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests.handlers

import com.microsoft.walletlibrary.requests.OpenIdPresentationRequest
import com.microsoft.walletlibrary.requests.VerifiedIdRequest
import com.microsoft.walletlibrary.requests.VerifiedIdRequestContent
import com.microsoft.walletlibrary.requests.input.VerifiedIdRequestURL
import com.microsoft.walletlibrary.requests.rawrequests.OpenIdRawRequest
import com.microsoft.walletlibrary.requests.rawrequests.RawRequest
import com.microsoft.walletlibrary.requests.rawrequests.RequestType
import com.microsoft.walletlibrary.requests.requirements.VerifiedIdRequirement
import com.microsoft.walletlibrary.util.InputCastingException
import com.microsoft.walletlibrary.util.RequirementCastingException
import com.microsoft.walletlibrary.util.UnSupportedProtocolException
import com.microsoft.walletlibrary.wrapper.ContractResolver

/**
 * OIDC protocol specific implementation of RequestHandler. It can handle OpenID raw request and returns a VerifiedIdRequest.
 */
internal class OpenIdRequestHandler : RequestHandler {

    override suspend fun handleRequest(rawRequest: RawRequest): VerifiedIdRequest {
        if ((rawRequest !is OpenIdRawRequest))
            throw UnSupportedProtocolException("Received a raw request of unsupported protocol")
        val requestContent = rawRequest.handleRawRequest()
        return when (rawRequest.requestType) {
            RequestType.PRESENTATION -> handlePresentationRequest(requestContent)
            RequestType.ISSUANCE -> handleIssuanceRequest(requestContent)
        }
    }

    private fun handlePresentationRequest(requestContent: VerifiedIdRequestContent): VerifiedIdRequest {
        return OpenIdPresentationRequest(
            requestContent.requesterStyle,
            requestContent.requirement,
            requestContent.rootOfTrust
        )
    }

    private suspend fun handleIssuanceRequest(requestContent: VerifiedIdRequestContent): VerifiedIdRequest {
        if (requestContent.requirement !is VerifiedIdRequirement)
            throw RequirementCastingException("Requirement is not the expected VerifiedId Requirement")
        if (requestContent.requirement.issuanceOptions.first() !is VerifiedIdRequestURL)
            throw InputCastingException("VerifiedId Input is not the expected VerifiedIdRequestURL type")
        val contractUrl = (requestContent.requirement.issuanceOptions.first() as VerifiedIdRequestURL).url
        return ContractResolver.getIssuanceRequest(contractUrl.toString())
    }
}