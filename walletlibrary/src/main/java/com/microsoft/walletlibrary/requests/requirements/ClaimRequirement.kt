package com.microsoft.walletlibrary.requests.requirements

import com.microsoft.did.sdk.credential.service.models.attestations.ClaimAttestation

data class ClaimRequirement (
    val claim: String,

    val required: Boolean = false,

    var type: String = ""
) {
    constructor(claimAttestation: ClaimAttestation) : this(claimAttestation.claim, claimAttestation.required, claimAttestation.type)
}