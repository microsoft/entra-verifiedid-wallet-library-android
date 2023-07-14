/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests

import com.microsoft.walletlibrary.util.VerifiedIdResult
import com.microsoft.walletlibrary.verifiedid.VerifiedId

/**
 * VerifiedIdIssuanceRequest handles information contained in an issuance request like the visual
 * styling of requester and VerifiedID, requirements needed in order to complete the request and information about
 * trust model of requester like domain url and verification status.
 */
interface VerifiedIdIssuanceRequest: VerifiedIdRequest<VerifiedId> {

    // Completes the request and returns a VerifiedID if successful.
    override suspend fun complete(): VerifiedIdResult<VerifiedId>
}