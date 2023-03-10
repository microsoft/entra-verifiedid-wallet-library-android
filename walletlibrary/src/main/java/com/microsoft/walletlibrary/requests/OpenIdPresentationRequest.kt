/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests

import com.microsoft.walletlibrary.requests.requirements.Requirement
import com.microsoft.walletlibrary.requests.styles.RequesterStyle

/**
 * Presentation request specific to OpenId protocol.
 */
internal class OpenIdPresentationRequest(
    // Attributes describing the requester (eg. name, logo).
    override val requesterStyle: RequesterStyle,

    // Information describing the requirements needed to complete the flow.
    override val requirement: Requirement,

    // Root of trust of the requester (eg. linked domains).
    override val rootOfTrust: RootOfTrust
) : VerifiedIdPresentationRequest {
    override fun isSatisfied(): Boolean {
        TODO("Not yet implemented")
    }

    // Completes the request and returns nothing if successful.
    override suspend fun complete(): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun cancel(message: String?): Result<Void> {
        TODO("Not yet implemented")
    }
}