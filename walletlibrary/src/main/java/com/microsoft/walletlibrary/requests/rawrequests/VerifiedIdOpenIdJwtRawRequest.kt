/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests.rawrequests

import com.microsoft.walletlibrary.did.sdk.credential.service.PresentationRequest
import com.microsoft.walletlibrary.mappings.issuance.toPinRequirement
import com.microsoft.walletlibrary.mappings.presentation.getRequesterStyle
import com.microsoft.walletlibrary.mappings.presentation.toRequirement
import com.microsoft.walletlibrary.mappings.toRootOfTrust
import com.microsoft.walletlibrary.requests.InjectedIdToken
import com.microsoft.walletlibrary.requests.PresentationRequestContent
import com.microsoft.walletlibrary.requests.requirements.GroupRequirement
import com.microsoft.walletlibrary.requests.requirements.GroupRequirementOperator
import com.microsoft.walletlibrary.util.MissingCallbackUrlException
import com.microsoft.walletlibrary.util.MissingRequestStateException

internal class VerifiedIdOpenIdJwtRawRequest(
    override val rawRequest: PresentationRequest,
    override val requestType: RequestType = RequestType.PRESENTATION
): OpenIdRawRequest {
    override fun mapToPresentationRequestContent(): PresentationRequestContent {
        if (rawRequest.content.state.isNullOrEmpty())
            throw MissingRequestStateException("Request State is missing in presentation request")
        if (rawRequest.content.redirectUrl.isEmpty())
            throw MissingCallbackUrlException("Callback url is missing in presentation request")
        val requirementsList = rawRequest.getPresentationDefinitions().map { it.toRequirement() }
        val requirement = if (requirementsList.size == 1) {
            requirementsList.first()
        } else {
            GroupRequirement(true,
            requirementsList.toMutableList(),
            GroupRequirementOperator.ALL)
        }
        return PresentationRequestContent(
            rawRequest.getRequesterStyle(),
            requirement,
            rawRequest.linkedDomainResult.toRootOfTrust(),
            rawRequest.content.idTokenHint?.let {
                InjectedIdToken(
                    it,
                    rawRequest.content.pinDetails?.toPinRequirement()
                )
            },
            rawRequest.content.redirectUrl,
            rawRequest.content.state
        )
    }
}