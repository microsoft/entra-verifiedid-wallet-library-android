package com.microsoft.walletlibrary.mappings

import com.microsoft.did.sdk.credential.service.models.attestations.CredentialAttestations
import com.microsoft.walletlibrary.requests.requirements.GroupRequirement
import com.microsoft.walletlibrary.requests.requirements.GroupRequirementOperator
import com.microsoft.walletlibrary.requests.requirements.Requirement
import com.microsoft.walletlibrary.util.MissingRequirementException

internal fun CredentialAttestations.toRequirement(): Requirement {
    val requirements = mutableListOf<Requirement>()
    if (this.idTokens.isNotEmpty())
        requirements.addAll(this.idTokens.map { it.toIdTokenRequirement() })
    if (this.accessTokens.isNotEmpty())
        requirements.addAll(this.accessTokens.map { it.toAccessTokenRequirement() })
    if (this.presentations.isNotEmpty())
        requirements.addAll(this.presentations.map { it.toVerifiedIdRequirement() })
    if (this.selfIssued.claims.isNotEmpty())
        requirements.addAll(listOf(this.selfIssued.toSelfAttestedClaimRequirement()))
    if (requirements.isEmpty())
        throw MissingRequirementException("There is no requirement in the request")
    return if (requirements.size > 1)
        GroupRequirement(true, requirements, GroupRequirementOperator.ALL)
    else
        requirements.first()
}