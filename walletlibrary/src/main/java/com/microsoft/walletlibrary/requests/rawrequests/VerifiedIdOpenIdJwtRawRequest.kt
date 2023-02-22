package com.microsoft.walletlibrary.requests.rawrequests

import com.microsoft.did.sdk.credential.service.PresentationRequest
import com.microsoft.walletlibrary.mappings.presentation.getRequesterStyle
import com.microsoft.walletlibrary.mappings.presentation.toRequirement
import com.microsoft.walletlibrary.mappings.toRootOfTrust
import com.microsoft.walletlibrary.requests.VerifiedIdRequestContent

internal class VerifiedIdOpenIdJwtRawRequest(
    override val requestType: RequestType, override val rawRequest: PresentationRequest
): OpenIdRawRequest {
    override fun mapToRequestContent(): VerifiedIdRequestContent {
        return VerifiedIdRequestContent(
            this.rawRequest.getRequesterStyle(),
            this.rawRequest.getPresentationDefinition().toRequirement(),
            this.rawRequest.linkedDomainResult.toRootOfTrust()
        )
    }
}