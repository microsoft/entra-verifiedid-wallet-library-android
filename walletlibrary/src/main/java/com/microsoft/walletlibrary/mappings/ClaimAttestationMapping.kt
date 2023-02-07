package com.microsoft.walletlibrary.mappings

import com.microsoft.did.sdk.credential.service.models.attestations.ClaimAttestation
import com.microsoft.walletlibrary.requests.requirements.ClaimRequirement
import com.microsoft.walletlibrary.requests.requirements.RequestedClaim

// Maps ClaimAttestation object from VC SDK to RequestedClaim in library
fun ClaimAttestation.toRequestedClaim(): RequestedClaim {
    return RequestedClaim(false, this.claim, this.required)
}

// Maps ClaimAttestation object from VC SDK to ClaimRequirement in library
fun ClaimAttestation.toClaimRequirement(): ClaimRequirement {
    return ClaimRequirement(this.claim, this.required, this.type)
}