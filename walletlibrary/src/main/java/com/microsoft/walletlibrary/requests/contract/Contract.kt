package com.microsoft.walletlibrary.requests.contract

import com.microsoft.walletlibrary.requests.requirements.AccessTokenRequirement
import com.microsoft.walletlibrary.requests.requirements.IdTokenRequirement
import com.microsoft.walletlibrary.requests.requirements.SelfAttestedClaimRequirement
import com.microsoft.walletlibrary.requests.requirements.VerifiedIdRequirement

data class Contract(val raw: String) {
    val verifiedIdRequirements: List<VerifiedIdRequirement> = mutableListOf()
    val idTokenRequirements: List<IdTokenRequirement> = mutableListOf()
    val selfAttestedClaimRequirements: List<SelfAttestedClaimRequirement> = mutableListOf()
    val accessTokenRequirements: List<AccessTokenRequirement> = mutableListOf()
}