package com.microsoft.walletlibrary.mappings

import com.microsoft.did.sdk.credential.service.models.contracts.display.ClaimDescriptor
import com.microsoft.walletlibrary.requests.styles.ClaimAttributes

fun ClaimDescriptor.toClaimAttributes(): ClaimAttributes {
    return ClaimAttributes(type = this.type, label = this.label)
}