/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.mappings.issuance

import com.microsoft.did.sdk.credential.service.models.attestations.AccessTokenAttestation
import com.microsoft.walletlibrary.requests.requirements.AccessTokenRequirement

/**
 * Maps AccessTokenAttestation object from VC SDK to AccessTokenRequirement in library
 */
internal fun AccessTokenAttestation.toAccessTokenRequirement(): AccessTokenRequirement {
    return AccessTokenRequirement(
        "",
        this.configuration,
        this.redirectUri,
        this.resourceId,
        this.scope,
        this.claims.map { it.toRequestedClaim() },
        this.encrypted,
        this.required
    )
}