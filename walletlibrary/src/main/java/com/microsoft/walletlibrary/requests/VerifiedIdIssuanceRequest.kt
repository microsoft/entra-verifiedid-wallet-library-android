/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests

/**
 * VerifiedIdIssuanceRequest handles information contained in an issuance request like the visual
 * styling of requester and VerifiedID, requirements needed in order to complete the request and information about
 * trust model of requester like domain url and verification status.
 */
interface VerifiedIdIssuanceRequest: VerifiedIdRequest {

    // Completes the request and returns a VerifiedID if successful.
    override suspend fun <VerifiedId> complete(): Result<VerifiedId>
}