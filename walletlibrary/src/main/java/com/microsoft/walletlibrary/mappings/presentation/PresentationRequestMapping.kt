/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.mappings.presentation

import com.microsoft.did.sdk.credential.service.PresentationRequest
import com.microsoft.walletlibrary.requests.styles.Logo
import com.microsoft.walletlibrary.requests.styles.OpenIdVerifierStyle
import com.microsoft.walletlibrary.requests.styles.RequesterStyle

/**
 * Maps PresentationRequest object from VC SDK to RequesterStyle and OpenIdPresentationRequest in library.
 */
internal fun PresentationRequest.getRequesterStyle(): RequesterStyle {
    val registration = this.content.registration
    return OpenIdVerifierStyle(
        this.entityName,
        "",
        Logo(registration.logoUri, registration.logoData, "")
    )
}
