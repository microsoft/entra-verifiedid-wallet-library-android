package com.microsoft.walletlibrary.requests.contract

import com.microsoft.walletlibrary.requests.RootOfTrust
import com.microsoft.walletlibrary.requests.requirements.AccessTokenRequirement
import com.microsoft.walletlibrary.requests.requirements.IdTokenRequirement
import com.microsoft.walletlibrary.requests.requirements.SelfAttestedClaimRequirement
import com.microsoft.walletlibrary.requests.requirements.VerifiedIdRequirement

data class Contract(val raw: String) {
    val rootOfTrust: RootOfTrust? = null
    var verifiedIdRequirements: List<VerifiedIdRequirement> = mutableListOf()
    var idTokenRequirements: List<IdTokenRequirement> = mutableListOf()
    var selfAttestedClaimRequirements: List<SelfAttestedClaimRequirement> = mutableListOf()
    var accessTokenRequirements: List<AccessTokenRequirement> = mutableListOf()
}