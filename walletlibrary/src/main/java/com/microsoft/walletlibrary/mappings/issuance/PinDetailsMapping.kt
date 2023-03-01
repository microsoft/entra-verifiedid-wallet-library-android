package com.microsoft.walletlibrary.mappings.issuance

import com.microsoft.did.sdk.credential.service.models.oidc.PinDetails
import com.microsoft.walletlibrary.requests.requirements.PinRequirement

/**
 * Maps PinDetails object from VC SDK to PinRequirement in library
 */
internal fun PinDetails.toPinRequirement(): PinRequirement {
    return PinRequirement(length, type, true)
}