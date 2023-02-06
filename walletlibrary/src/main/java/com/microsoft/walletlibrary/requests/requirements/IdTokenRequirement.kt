/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests.requirements

import com.microsoft.did.sdk.credential.service.models.attestations.IdTokenAttestation

/**
 * Represents information that describes Id Token required in order to complete a VerifiedID request
 */
class IdTokenRequirement(
    internal val id: String,

    // Properties used by developers to get access token (configuration, clientId, redirectUri, scope)
    val configuration: String,
    val clientId: String,
    val redirectUri: String,
    val scope: String,

    // Nonce is generated using user DID
    val nonce: String,

    // Specific claims requested from id token
    internal val claims: List<RequestedClaim>,

    // Indicates whether the requirement must be encrypted
    internal val encrypted: Boolean = false,

    // Indicates whether the requirement is required or optional
    override val required: Boolean = false
): Requirement {
    constructor(idTokenAttestation: IdTokenAttestation) : this(
        "",
        idTokenAttestation.configuration,
        idTokenAttestation.client_id,
        idTokenAttestation.redirect_uri,
        idTokenAttestation.scope,
        "",
        idTokenAttestation.claims.map { RequestedClaim(it) },
        idTokenAttestation.encrypted,
        idTokenAttestation.required
    )

    // Validates the requirement and throws an exception if the requirement is invalid or not fulfilled
    override fun validate() {
        TODO("Not yet implemented")
    }

    // Fulfills the requirement in the request with specified value
    fun fulfill(idToken: String) {
        TODO("Not yet implemented")
    }
}