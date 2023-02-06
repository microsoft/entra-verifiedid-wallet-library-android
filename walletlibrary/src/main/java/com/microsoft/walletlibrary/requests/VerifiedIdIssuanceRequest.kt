/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests

import com.microsoft.walletlibrary.requests.requirements.Requirement
import com.microsoft.walletlibrary.requests.styles.RequesterStyle
import com.microsoft.walletlibrary.requests.styles.VerifiedIDStyle

/**
 * VerifiedIdIssuanceRequest handles information contained in an issuance request like the visual
 * styling of requester, requirements needed in order to complete the request and information about
 * trust model of requester like domain url and verification status.
 * */
@Suppress("UNCHECKED_CAST")
class VerifiedIdIssuanceRequest(
    // Attributes describing the requester (eg. name, logo).
    override val requesterStyle: RequesterStyle,

    // Information describing the requirements needed to complete the flow.
    override val requirement: Requirement,

    // Root of trust of the requester (eg. linked domains).
    override val rootOfTrust: RootOfTrust,

    // Attributes describing the Verified ID (eg. name, issuer, logo, background and text colors).
    val verifiedIdStyle: VerifiedIDStyle? = null
) : VerifiedIdRequest {

    // Indicates whether issuance request is satisfied on client side.
    override fun isSatisfied(): Boolean {
        TODO("Not yet implemented")
    }

    // Completes the request and returns a VerifiedID if successful.
    override suspend fun <VerifiedId> complete(): Result<VerifiedId> {
        TODO("Not yet implemented")
    }

    // Cancels the request with an optional message.
    override fun cancel(message: String?): Result<Void> {
        TODO("Not yet implemented")
    }
}