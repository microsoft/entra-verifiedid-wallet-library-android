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
    override val presentationRequest: PresentationRequest,
    override val requestType: RequestType = RequestType.PRESENTATION,
    override val rawRequest: Any
): OpenIdRawRequest {
    override fun mapToPresentationRequestContent() : PresentationRequestContent {
        if (presentationRequest.content.state.isNullOrEmpty())
            throw MissingRequestStateException("Request State is missing in presentation request")
        if (presentationRequest.content.redirectUrl.isEmpty())
            throw MissingCallbackUrlException("Callback url is missing in presentation request")
        val requirementsList = presentationRequest.getPresentationDefinitions().map { it.toRequirement() }
        val requirement = if (requirementsList.size == 1) {
            requirementsList.first()
        } else {
            GroupRequirement(
                true,
                requirementsList.toMutableList(),
                GroupRequirementOperator.ALL
            )
        }
        return PresentationRequestContent(
            presentationRequest.getRequesterStyle(),
            requirement,
            presentationRequest.linkedDomainResult.toRootOfTrust(),
            presentationRequest.content.idTokenHint?.let {
                InjectedIdToken(
                    it,
                    presentationRequest.content.pinDetails?.toPinRequirement()
                )
            },
            presentationRequest.content.redirectUrl,
            presentationRequest.content.state
        )
    }
}