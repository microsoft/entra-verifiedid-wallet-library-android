package com.microsoft.walletlibrary.mappings

import com.microsoft.did.sdk.credential.service.models.attestations.ClaimAttestation
import com.microsoft.walletlibrary.requests.requirements.ClaimRequirement
import com.microsoft.walletlibrary.requests.requirements.RequestedClaim

fun ClaimAttestation.toRequestedClaim(): RequestedClaim {
    return RequestedClaim(false, this.claim, this.required)
}

fun ClaimAttestation.toClaimRequirement(): ClaimRequirement {
    return ClaimRequirement(this.claim, this.required, this.type)
}