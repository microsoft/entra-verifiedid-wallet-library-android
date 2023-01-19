package com.microsoft.walletlibrary.requests.requirements

import com.microsoft.did.sdk.credential.service.models.attestations.PresentationAttestation

data class VerifiedIdRequirement(
    val id: String,
    val encrypted: Boolean,
    val required: Boolean,
    val types: List<String>,
    val acceptedIssuers: List<String>
) {
    var purpose: String? = null
    var credentialIssuanceParams: CredentialIssuanceParams? = null

    constructor(presentationAttestation: PresentationAttestation) : this(
        "",
        presentationAttestation.encrypted,
        presentationAttestation.required,
        listOf( presentationAttestation.credentialType),
        presentationAttestation.issuers.map { it.iss }
    )

    fun getMatches(verifiedIds: List<String>): List<String> {
        return emptyList()
    }
}