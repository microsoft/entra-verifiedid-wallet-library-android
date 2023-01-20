package com.microsoft.walletlibrary.requests.contract

import com.microsoft.walletlibrary.requests.contract.attributes.RequesterAttributes
import com.microsoft.walletlibrary.requests.contract.attributes.VerifiedIdAttributes
import com.microsoft.walletlibrary.requests.requirements.AccessTokenRequirement
import com.microsoft.walletlibrary.requests.requirements.IdTokenRequirement
import com.microsoft.walletlibrary.requests.requirements.SelfAttestedClaimRequirement
import com.microsoft.walletlibrary.requests.requirements.VerifiedIdRequirement

data class Contract(
    val raw: String,
    val rootOfTrust: RootOfTrust,
    val requesterAttributes: RequesterAttributes
) {
    val verifiedIdRequirements: List<VerifiedIdRequirement> = emptyList()
    val idTokenRequirements: List<IdTokenRequirement> = emptyList()
    val selfAttestedClaimRequirements: List<SelfAttestedClaimRequirement> = emptyList()
    val accessTokenRequirements: List<AccessTokenRequirement> = emptyList()
    val verifiedIdAttributes: VerifiedIdAttributes? = null
}