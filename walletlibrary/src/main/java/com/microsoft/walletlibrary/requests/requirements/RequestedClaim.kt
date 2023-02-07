/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests.requirements

import com.microsoft.did.sdk.credential.service.models.attestations.ClaimAttestation

data class RequestedClaim(
    internal val indexed: Boolean,

    // Value of the claim.
    val claim: String,

    // Indicated if claim is required or optional.
    val required: Boolean = false,
) {
    constructor(claimAttestation: ClaimAttestation) : this(
        false,
        claimAttestation.claim,
        claimAttestation.required
    )
}