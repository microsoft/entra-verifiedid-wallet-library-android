/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.did.sdk.credential.service.models.attestations

import com.microsoft.walletlibrary.did.sdk.util.Constants.DEFAULT_VP_EXPIRATION_IN_SECONDS
import kotlinx.serialization.Serializable

/**
 * Data Container to represent a Verifiable Presentation Request.
 * Verifiable Presentation is a wrapper around a Verifiable Credential.
 * Must have either issuers property or contracts property
 */
@Serializable
internal data class PresentationAttestation(
    // The type of the verifiable credential that is being requested.
    val credentialType: String,

    // A list of issuers that requester will accept.
    val issuers: List<AcceptedIssuer> = emptyList(),

    // A list of contracts if user does not have requested credential.
    val contracts: List<String> = emptyList(),

    // True, if presentation is required.
    val required: Boolean = false,

    val encrypted: Boolean = false,

    // How long the requested Verifiable Presentation should be valid for.
    val validityInterval: Int = DEFAULT_VP_EXPIRATION_IN_SECONDS,

    val claims: List<ClaimAttestation> = emptyList()
)