package com.microsoft.walletlibrary.mappings

import com.microsoft.walletlibrary.requests.styles.Logo

fun com.microsoft.did.sdk.credential.service.models.contracts.display.Logo.toLogo(): Logo {
    return Logo(this.uri, this.image, this.description)
}