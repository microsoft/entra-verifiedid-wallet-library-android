/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests

import com.microsoft.walletlibrary.requests.rawrequests.RawManifest
import com.microsoft.walletlibrary.requests.requirements.Requirement
import com.microsoft.walletlibrary.requests.styles.RequesterStyle
import com.microsoft.walletlibrary.requests.styles.VerifiedIDStyle
import com.microsoft.walletlibrary.util.RequirementValidationException
import com.microsoft.walletlibrary.util.WalletLibraryException
import com.microsoft.walletlibrary.verifiedid.VerifiedId
import com.microsoft.walletlibrary.wrapper.VerifiedIdRequester

/**
 * Issuance request specific to Manifest/Contract.
 */
class ManifestIssuanceRequest(
    // Attributes describing the requester (eg. name, logo).
    override val requesterStyle: RequesterStyle,

    // Information describing the requirements needed to complete the flow.
    override var requirement: Requirement,

    // Root of trust of the requester (eg. linked domains).
    override val rootOfTrust: RootOfTrust,

    // Attributes describing the Verified ID (eg. name, issuer, logo, background and text colors).
    val verifiedIdStyle: VerifiedIDStyle,

    val request: RawManifest
): VerifiedIdIssuanceRequest {
    override suspend fun complete(): Result<VerifiedId> {
        return try {
            val verifiedId =
                VerifiedIdRequester.sendIssuanceResponse(request.rawRequest, requirement)
            Result.success(verifiedId)
        } catch (exception: WalletLibraryException) {
            Result.failure(exception)
        }
    }

    override fun isSatisfied(): Boolean {
        try {
            requirement.validate()
        } catch (exception: RequirementValidationException) {
            return false
        }
        return true
    }

    override fun cancel(message: String?): Result<Void> {
        TODO("Not yet implemented")
    }
}