/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.mappings.issuance

import com.microsoft.walletlibrary.did.sdk.credential.service.models.attestations.ClaimAttestation
import com.microsoft.walletlibrary.requests.requirements.ClaimRequirement
import com.microsoft.walletlibrary.requests.requirements.RequestedClaim

/**
 * Maps ClaimAttestation object from VC SDK to RequestedClaim in library
 */
internal fun ClaimAttestation.toRequestedClaim(): RequestedClaim {
    return RequestedClaim(false, this.claim, this.required)
}

// Maps ClaimAttestation object from VC SDK to ClaimRequirement in library
internal fun ClaimAttestation.toClaimRequirement(): ClaimRequirement {
    return ClaimRequirement(this.claim, this.required, this.type)
}