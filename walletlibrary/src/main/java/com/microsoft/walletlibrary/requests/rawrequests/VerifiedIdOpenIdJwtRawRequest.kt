/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests.rawrequests

import com.microsoft.did.sdk.credential.service.PresentationRequest
import com.microsoft.walletlibrary.mappings.issuance.toPinRequirement
import com.microsoft.walletlibrary.mappings.presentation.getRequesterStyle
import com.microsoft.walletlibrary.mappings.presentation.toRequirement
import com.microsoft.walletlibrary.mappings.toRootOfTrust
import com.microsoft.walletlibrary.requests.InjectedIdToken
import com.microsoft.walletlibrary.requests.PresentationRequestContent

internal class VerifiedIdOpenIdJwtRawRequest(
    override val rawRequest: PresentationRequest,
    override val requestType: RequestType = RequestType.PRESENTATION
): OpenIdRawRequest {
    override fun mapToPresentationRequestContent(): PresentationRequestContent {
        return PresentationRequestContent(
            rawRequest.getRequesterStyle(),
            rawRequest.getPresentationDefinition().toRequirement(),
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