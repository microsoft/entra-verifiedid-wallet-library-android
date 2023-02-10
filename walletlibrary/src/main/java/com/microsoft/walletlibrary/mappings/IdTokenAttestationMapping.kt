package com.microsoft.walletlibrary.mappings

import com.microsoft.did.sdk.credential.service.models.attestations.IdTokenAttestation
import com.microsoft.walletlibrary.requests.requirements.IdTokenRequirement

/**
 * Maps IdTokenAttestation object from VC SDK to IdTokenRequirement in library
 */
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