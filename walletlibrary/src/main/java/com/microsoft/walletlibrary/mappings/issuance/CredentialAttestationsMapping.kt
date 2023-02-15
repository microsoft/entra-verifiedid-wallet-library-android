/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.mappings.issuance

import com.microsoft.did.sdk.credential.service.models.attestations.CredentialAttestations
import com.microsoft.walletlibrary.requests.requirements.GroupRequirement
import com.microsoft.walletlibrary.requests.requirements.GroupRequirementOperator
import com.microsoft.walletlibrary.requests.requirements.Requirement
import com.microsoft.walletlibrary.util.UnsupportedRequirementTypeException

fun CredentialAttestations.toRequirement(): Requirement {
    val requirementList = mutableListOf<Requirement>()
    if (this.idTokens.isNotEmpty())
        requirementList.addAll(this.idTokens.map { it.toIdTokenRequirement() })
    if (this.accessTokens.isNotEmpty())
        requirementList.addAll(this.accessTokens.map { it.toAccessTokenRequirement() })
    if (this.presentations.isNotEmpty())
        requirementList.addAll(this.presentations.map { it.toVerifiedIdRequirement() })
    if (this.selfIssued.claims.isNotEmpty())
        requirementList.addAll(listOf(this.selfIssued.toSelfAttestedClaimRequirement()))
    if (requirementList.isEmpty())
        throw UnsupportedRequirementTypeException("Requirement type is not supported")
    return if (requirementList.size > 1) {
        val groupRequirement = GroupRequirement()
        groupRequirement.required = true
        groupRequirement.requirements.addAll(requirementList)
        groupRequirement.requirementOperator = GroupRequirementOperator.ALL
        groupRequirement
    } else
        requirementList.first()
}