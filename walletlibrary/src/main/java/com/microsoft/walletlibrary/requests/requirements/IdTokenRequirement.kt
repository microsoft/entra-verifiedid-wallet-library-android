package com.microsoft.walletlibrary.requests.requirements

import com.microsoft.did.sdk.credential.service.models.attestations.IdTokenAttestation

data class IdTokenRequirement(
    val id: String,
    val encrypted: Boolean,
    val required: Boolean,
    val configuration: String,
    val client_id: String,
    val redirect_uri: String,
    val scope: String,
    val nonce: String
) {
    constructor(idTokenAttestation: IdTokenAttestation) : this(
        "",
        idTokenAttestation.encrypted,
        idTokenAttestation.required,
        idTokenAttestation.configuration,
        "",
        idTokenAttestation.redirect_uri,
        idTokenAttestation.scope,
        ""
    )
}