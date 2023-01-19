package com.microsoft.walletlibrary.requests.requirements

import com.microsoft.did.sdk.credential.service.models.attestations.AccessTokenAttestation

data class AccessTokenRequirement(
    val id: String,
    val encrypted: Boolean,
    val required: Boolean,
    val configuration: String,
    val client_id: String,
    val resourceId: String,
    val scope: String
) {
    constructor(accessTokenAttestation: AccessTokenAttestation) : this(
        "",
        accessTokenAttestation.encrypted,
        accessTokenAttestation.required,
        accessTokenAttestation.configuration,
        "",
        accessTokenAttestation.resourceId,
        accessTokenAttestation.scope
    )
}