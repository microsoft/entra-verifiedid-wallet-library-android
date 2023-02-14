package com.microsoft.walletlibrary.requests.rawrequests

import com.microsoft.did.sdk.credential.service.IssuanceRequest
import com.microsoft.did.sdk.credential.service.PresentationRequest
import com.microsoft.did.sdk.credential.service.Request
import com.microsoft.walletlibrary.mappings.toOpenIdIssuanceRequest
import com.microsoft.walletlibrary.mappings.toOpenIdPresentationRequest
import com.microsoft.walletlibrary.requests.VerifiedIdRequest
import com.microsoft.walletlibrary.util.UnExpectedRequestTypeException

internal class VerifiedIdOpenIdJwtRawRequest(
    override val requestType: RequestType, override val rawRequest: Request
): OpenIdRawRequest {
    override fun handleRawRequest(): VerifiedIdRequest {
        return when (rawRequest) {
            is IssuanceRequest -> rawRequest.toOpenIdIssuanceRequest()
            is PresentationRequest -> rawRequest.toOpenIdPresentationRequest()
            else -> throw UnExpectedRequestTypeException("Provided Request is not the expected Presentation or Issuance Request")
        }
    }
}