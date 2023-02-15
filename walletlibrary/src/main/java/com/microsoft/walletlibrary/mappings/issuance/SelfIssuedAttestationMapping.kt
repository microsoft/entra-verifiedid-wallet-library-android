/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.mappings.issuance

import com.microsoft.did.sdk.credential.service.models.attestations.SelfIssuedAttestation
import com.microsoft.walletlibrary.requests.requirements.SelfAttestedClaimRequirement

/**
 * Maps SelfIssuedAttestation object from VC SDK to SelfAttestedClaimRequirement in library
 */
internal fun SelfIssuedAttestation.toSelfAttestedClaimRequirement(): SelfAttestedClaimRequirement {
    return SelfAttestedClaimRequirement(
        "",
        this.claims.map { it.toClaimRequirement() },
        this.encrypted,
        this.required
    )
}