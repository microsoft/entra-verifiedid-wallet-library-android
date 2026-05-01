/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests

import com.microsoft.walletlibrary.requests.styles.VerifiedIdStyle
import com.microsoft.walletlibrary.util.VerifiedIdResult
import com.microsoft.walletlibrary.verifiedid.VerifiedId

/**
 * VerifiedIdIssuanceRequest handles information contained in an issuance request like the visual
 * styling of requester and VerifiedID, requirements needed in order to complete the request and information about
 * trust model of requester like domain url and verification status.
 */
interface VerifiedIdIssuanceRequest : VerifiedIdRequest<VerifiedId> {
    val continuation: Continuation?

    // Attributes describing the Verified ID (eg. name, issuer, logo, background and text colors).
    val verifiedIdStyle: VerifiedIdStyle

    /**
     * The credential issuer endpoint URL.
     *
     * For legacy manifest issuance, this may also be the endpoint where the issuance response
     * is sent. For OpenID4VCI, prefer [credentialEndpoint] as the credential request POST target.
     */
    val credentialIssuer: String?
        get() = null

    /**
     * The credential endpoint URL where the credential request is POSTed.
     *
     * This is available for OpenID4VCI issuance. It will be null for legacy manifest issuance.
     */
    val credentialEndpoint: String?
        get() = null

    // Completes the request and returns a VerifiedID if successful.
    override suspend fun complete(): VerifiedIdResult<VerifiedId>
}