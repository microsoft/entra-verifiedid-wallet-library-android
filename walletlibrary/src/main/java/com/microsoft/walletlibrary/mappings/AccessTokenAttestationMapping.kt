package com.microsoft.walletlibrary.mappings

import com.microsoft.did.sdk.credential.service.models.attestations.AccessTokenAttestation
import com.microsoft.walletlibrary.requests.requirements.AccessTokenRequirement

// Maps AccessTokenAttestation object from VC SDK to AccessTokenRequirement in library
fun AccessTokenAttestation.toAccessTokenRequirement(): AccessTokenRequirement {
    return AccessTokenRequirement(
        "",
        this.configuration,
        this.redirectUri,
        this.resourceId,
        this.scope,
        this.claims.map { it.toRequestedClaim() },
        this.encrypted,
        this.required
    )
}