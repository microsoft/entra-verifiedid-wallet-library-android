package com.microsoft.walletlibrary.mappings

import com.microsoft.did.sdk.credential.service.models.attestations.SelfIssuedAttestation
import com.microsoft.walletlibrary.requests.requirements.SelfAttestedClaimRequirement

// Maps SelfIssuedAttestation object from VC SDK to SelfAttestedClaimRequirement in library
fun SelfIssuedAttestation.toSelfAttestedClaimRequirement(): SelfAttestedClaimRequirement {
    return SelfAttestedClaimRequirement(
        "",
        this.claims.map { it.toClaimRequirement() },
        this.encrypted,
        this.required
    )
}