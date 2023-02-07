package com.microsoft.walletlibrary.mappings

import com.microsoft.did.sdk.credential.service.models.attestations.SelfIssuedAttestation
import com.microsoft.walletlibrary.requests.requirements.SelfAttestedClaimRequirement

fun SelfIssuedAttestation.toSelfAttestedClaimRequirement(): SelfAttestedClaimRequirement {
    return SelfAttestedClaimRequirement(
            "",
    this.claims.map { it.toClaimRequirement() },
    this.encrypted,
    this.required
    )
}