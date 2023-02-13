/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.mappings

import android.net.Uri
import com.microsoft.did.sdk.credential.service.models.attestations.PresentationAttestation
import com.microsoft.walletlibrary.requests.input.VerifiedIdRequestURL
import com.microsoft.walletlibrary.requests.requirements.VerifiedIdRequirement

/**
 * Maps PresentationAttestation object from VC SDK to VerifiedIdRequirement in library
 */
internal fun PresentationAttestation.toVerifiedIdRequirement(): VerifiedIdRequirement {
    return VerifiedIdRequirement(
        "",
        listOf(this.credentialType),
        this.encrypted,
        this.required,
        "",
        issuanceOptions = this.contracts.map { VerifiedIdRequestURL(Uri.parse(it)) }
    )
}