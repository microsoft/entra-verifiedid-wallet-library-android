/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.wrapper

import com.microsoft.did.sdk.VerifiableCredentialSdk
import com.microsoft.did.sdk.credential.service.IssuanceRequest
import com.microsoft.did.sdk.credential.service.IssuanceResponse
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.walletlibrary.mappings.issuance.addRequirements
import com.microsoft.walletlibrary.requests.requirements.Requirement
import com.microsoft.walletlibrary.util.VerifiedIdResponseCompletionException
import com.microsoft.walletlibrary.verifiedid.VerifiableCredential
import com.microsoft.walletlibrary.verifiedid.VerifiedId

/**
 * Wrapper class to wrap the completion of Issuance Request via VC SDK, map the received VerifiableCredential to VerifiedId and return it.
 */
object VerifiedIdRequester {
    internal suspend fun sendIssuanceResponse(
        issuanceRequest: IssuanceRequest,
        requirement: Requirement
    ): VerifiedId {
        val issuanceResponse = IssuanceResponse(issuanceRequest)
        issuanceResponse.addRequirements(requirement)
        when (val result = VerifiableCredentialSdk.issuanceService.sendResponse(issuanceResponse)) {
            is Result.Success -> return VerifiableCredential(result.payload, issuanceRequest.contract)
            is Result.Failure -> {
                throw VerifiedIdResponseCompletionException(
                    "Unable to complete issuance response",
                    result.payload
                )
            }
        }
    }
}