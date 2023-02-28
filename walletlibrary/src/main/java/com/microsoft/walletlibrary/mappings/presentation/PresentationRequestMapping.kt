/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.mappings.presentation

import com.microsoft.did.sdk.credential.service.PresentationRequest
import com.microsoft.walletlibrary.mappings.toRootOfTrust
import com.microsoft.walletlibrary.requests.OpenIdPresentationRequest
import com.microsoft.walletlibrary.requests.styles.Logo
import com.microsoft.walletlibrary.requests.styles.OpenIdRequesterStyle

/**
 * Maps PresentationRequest object from VC SDK to RequesterStyle and OpenIdPresentationRequest in library.
 */
internal fun PresentationRequest.getRequesterStyle(): OpenIdRequesterStyle {
    val registration = this.content.registration
    return OpenIdRequesterStyle(
        this.entityName,
        "",
        Logo(registration.logoUri, registration.logoData, "")
    )
}

internal fun PresentationRequest.toOpenIdPresentationRequest(): OpenIdPresentationRequest {
    return OpenIdPresentationRequest(
        this.getRequesterStyle(),
        this.getPresentationDefinition().toRequirement(),
        this.linkedDomainResult.toRootOfTrust()
    )
}