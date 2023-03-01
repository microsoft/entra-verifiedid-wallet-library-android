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
 * Maps SelfIssuedAttestation object from VC SDK to SelfAttestedClaimRequirement in library.
 */
internal fun SelfIssuedAttestation.toRequirement(): Requirement {
    val claims = claims
    if (claims.size == 1) {
        val required = required || claims.first().required
        return SelfAttestedClaimRequirement("", claims.first().claim, encrypted, required)
    }
    val requirements = claims.map {
        val required = it.required
        SelfAttestedClaimRequirement("", it.claim, encrypted, required)
    }
    return GroupRequirement(required, requirements, GroupRequirementOperator.ALL)
}