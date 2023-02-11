package com.microsoft.walletlibrary.mappings

import com.microsoft.did.sdk.credential.service.IssuanceRequest
import com.microsoft.did.sdk.credential.service.PresentationRequest
import com.microsoft.walletlibrary.requests.OpenIdIssuanceRequest
import com.microsoft.walletlibrary.requests.OpenIdPresentationRequest
import com.microsoft.walletlibrary.requests.VerifiedIdIssuanceRequest
import com.microsoft.walletlibrary.requests.VerifiedIdPresentationRequest
import com.microsoft.walletlibrary.requests.rawrequests.OpenIdRawRequest
import com.microsoft.walletlibrary.requests.styles.Logo
import com.microsoft.walletlibrary.requests.styles.RequesterStyle

fun OpenIdRawRequest.toVerifiedIdIssuanceRequest(): VerifiedIdIssuanceRequest {
    val issuanceRequest = this.rawRequest as IssuanceRequest
    val logo = issuanceRequest.contract.display.card.logo
    return OpenIdIssuanceRequest(
        RequesterStyle(
            issuanceRequest.entityName,
            "",
            Logo(logo?.uri, logo?.image, logo?.description ?: "")
        ),
        issuanceRequest.getAttestations().selfIssued.toSelfAttestedClaimRequirement(),
        issuanceRequest.linkedDomainResult.toRootOfTrust(),
        issuanceRequest.contract.display.toVerifiedIdStyle()
    )
}

fun OpenIdRawRequest.toVerifiedIdPresentationRequest(): VerifiedIdPresentationRequest {
    val presentationRequest = this.rawRequest as PresentationRequest
    val registration = presentationRequest.content.registration
    val credentialPresentationInputDescriptor =
        presentationRequest.content.claims.vpTokenInRequest.presentationDefinition.credentialPresentationInputDescriptors.first()
    return OpenIdPresentationRequest(
        RequesterStyle(
            presentationRequest.entityName,
            "",
            Logo(registration.logoUri, registration.logoData, "")
        ),
        credentialPresentationInputDescriptor.toVerifiedIdRequirement(),
        presentationRequest.linkedDomainResult.toRootOfTrust()
    )
}