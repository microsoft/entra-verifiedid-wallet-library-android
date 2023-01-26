package com.microsoft.walletlibrary.requests.contract

import com.microsoft.walletlibrary.requests.contract.attributes.RequesterAttributes
import com.microsoft.walletlibrary.requests.contract.attributes.VerifiedIdAttributes
import com.microsoft.walletlibrary.requests.requirements.AccessTokenRequirement
import com.microsoft.walletlibrary.requests.requirements.IdTokenRequirement
import com.microsoft.walletlibrary.requests.requirements.SelfAttestedClaimRequirement
import com.microsoft.walletlibrary.requests.requirements.VerifiedIdRequirement

data class Contract(
    internal val raw: String,

    // Root of trust of the requester (eg. linked domains)
    val rootOfTrust: RootOfTrust,

    // Attributes describing the requester (eg. name, logo)
    val requesterAttributes: RequesterAttributes,

    // Information describing the Verified IDs required for issuance
    val verifiedIdRequirements: List<VerifiedIdRequirement> = emptyList(),

    // Information describing the id tokens required for issuance
    val idTokenRequirements: List<IdTokenRequirement> = emptyList(),

    // Information describing the self-attested claims required for issuance
    val selfAttestedClaimRequirements: List<SelfAttestedClaimRequirement> = emptyList(),

    // Information describing the access tokens required for issuance
    val accessTokenRequirements: List<AccessTokenRequirement> = emptyList(),

    // Attributes describing the Verified ID (eg. name, issuer, logo, background and text colors)
    val verifiedIdAttributes: VerifiedIdAttributes? = null
)