/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.mappings.issuance

import com.microsoft.did.sdk.credential.service.models.attestations.SelfIssuedAttestation
import com.microsoft.walletlibrary.requests.requirements.GroupRequirement
import com.microsoft.walletlibrary.requests.requirements.GroupRequirementOperator
import com.microsoft.walletlibrary.requests.requirements.Requirement
import com.microsoft.walletlibrary.requests.requirements.SelfAttestedClaimRequirement

/**
 * Maps SelfIssuedAttestation object from VC SDK to SelfAttestedClaimRequirement in library
 */
internal fun SelfIssuedAttestation.toRequirement(): Requirement {
    val claims = this.claims
    if (claims.size == 1) {
        val required = this.required || claims.first().required
        return SelfAttestedClaimRequirement("", claims.first().claim, this.encrypted, required)
    }
    val requirements = claims.map {
        val required = it.required
        SelfAttestedClaimRequirement("", it.claim, this.encrypted, required)
    }
    return GroupRequirement(this.required, requirements, GroupRequirementOperator.ALL)
}