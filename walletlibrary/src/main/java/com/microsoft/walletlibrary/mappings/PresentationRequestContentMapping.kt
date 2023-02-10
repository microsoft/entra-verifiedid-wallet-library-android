package com.microsoft.walletlibrary.mappings

import com.microsoft.did.sdk.credential.service.models.oidc.PresentationRequestContent
import com.microsoft.walletlibrary.requests.styles.Logo
import com.microsoft.walletlibrary.requests.styles.RequesterStyle

fun PresentationRequestContent.toRequesterStyle(): RequesterStyle {
    val registration = this.registration
    return RequesterStyle(
        registration.clientName,
        "",
        Logo(registration.logoUri, registration.logoData, "")
    )
}