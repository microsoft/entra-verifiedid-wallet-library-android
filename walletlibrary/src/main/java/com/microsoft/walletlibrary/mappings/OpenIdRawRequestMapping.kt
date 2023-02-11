/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.mappings

import com.microsoft.did.sdk.credential.service.IssuanceRequest
import com.microsoft.did.sdk.credential.service.PresentationRequest
import com.microsoft.walletlibrary.requests.OpenIdIssuanceRequest
import com.microsoft.walletlibrary.requests.OpenIdPresentationRequest
import com.microsoft.walletlibrary.requests.VerifiedIdIssuanceRequest
import com.microsoft.walletlibrary.requests.VerifiedIdPresentationRequest
import com.microsoft.walletlibrary.requests.rawrequests.OpenIdRawRequest

fun OpenIdRawRequest.toVerifiedIdIssuanceRequest(): VerifiedIdIssuanceRequest {
    val issuanceRequest = this.rawRequest as IssuanceRequest
    return OpenIdIssuanceRequest(
        issuanceRequest.toRequesterStyle(),
        issuanceRequest.getAttestations().selfIssued.toSelfAttestedClaimRequirement(),
        issuanceRequest.linkedDomainResult.toRootOfTrust(),
        issuanceRequest.contract.display.toVerifiedIdStyle()
    )
}

fun OpenIdRawRequest.toVerifiedIdPresentationRequest(): VerifiedIdPresentationRequest {
    val presentationRequest = this.rawRequest as PresentationRequest
    val credentialPresentationInputDescriptor =
        presentationRequest.content.claims.vpTokenInRequest.presentationDefinition.credentialPresentationInputDescriptors.first()
    return OpenIdPresentationRequest(
        presentationRequest.toRequesterStyle(),
        credentialPresentationInputDescriptor.toVerifiedIdRequirement(),
        presentationRequest.linkedDomainResult.toRootOfTrust()
    )
}