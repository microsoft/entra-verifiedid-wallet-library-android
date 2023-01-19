package com.microsoft.walletlibrary.requests.requirements

import com.microsoft.did.sdk.credential.service.models.attestations.SelfIssuedAttestation

data class SelfAttestedClaimRequirement(
    val id: String,
    val required: Boolean,
    val encrypted: Boolean,
    val claim: List<ClaimRequirement>
) {
    constructor(selfIssuedAttestation: SelfIssuedAttestation) : this(
        "",
        selfIssuedAttestation.required,
        selfIssuedAttestation.encrypted,
        selfIssuedAttestation.claims.map { ClaimRequirement(it) }
    )
}