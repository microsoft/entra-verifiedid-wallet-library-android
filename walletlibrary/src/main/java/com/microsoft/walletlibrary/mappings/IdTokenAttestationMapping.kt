package com.microsoft.walletlibrary.mappings

import com.microsoft.did.sdk.credential.service.models.attestations.ClaimAttestation
import com.microsoft.did.sdk.credential.service.models.attestations.IdTokenAttestation
import com.microsoft.walletlibrary.requests.requirements.IdTokenRequirement
import com.microsoft.walletlibrary.requests.requirements.RequestedClaim

fun IdTokenAttestation.toIdTokenRequirement(): IdTokenRequirement {
    return IdTokenRequirement(
        "",
        this.configuration,
        this.client_id,
        this.redirect_uri,
        this.scope,
        "",
        this.claims.map { it.toRequestedClaim() },
        this.encrypted,
        this.required
    )
}