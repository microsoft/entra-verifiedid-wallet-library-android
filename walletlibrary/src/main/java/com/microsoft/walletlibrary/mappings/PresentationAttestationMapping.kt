package com.microsoft.walletlibrary.mappings

import com.microsoft.did.sdk.credential.service.models.attestations.PresentationAttestation
import com.microsoft.walletlibrary.requests.requirements.IssuanceOptions
import com.microsoft.walletlibrary.requests.requirements.VerifiedIdRequirement

fun PresentationAttestation.toVerifiedIdRequirement(): VerifiedIdRequirement {
    return VerifiedIdRequirement(
        "",
        listOf(this.credentialType),
        this.issuers.map { it.iss },
        this.encrypted,
        this.required,
        issuanceOptions = IssuanceOptions(this.contracts)
    )
}