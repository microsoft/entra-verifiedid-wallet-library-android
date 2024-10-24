/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests

import com.microsoft.walletlibrary.util.VerifiedIdResult

/**
 * VerifiedIdPresentationRequest handles information contained in an presentation request like the visual
 * styling of requester, requirements needed in order to complete the request and information about
 * trust model of requester like domain url and verification status.
 */
interface VerifiedIdPresentationRequest : VerifiedIdRequest<Unit> {

    // Completes the request and returns nothing if successful.
    override suspend fun complete(): VerifiedIdResult<Unit>

    fun getNonce(): String?
}