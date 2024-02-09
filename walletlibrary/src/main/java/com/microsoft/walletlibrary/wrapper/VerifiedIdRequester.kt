/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.wrapper

import com.microsoft.walletlibrary.did.sdk.VerifiableCredentialSdk
import com.microsoft.walletlibrary.did.sdk.credential.service.IssuanceRequest
import com.microsoft.walletlibrary.did.sdk.credential.service.IssuanceResponse
import com.microsoft.walletlibrary.did.sdk.credential.service.models.issuancecallback.IssuanceCompletionResponse
import com.microsoft.walletlibrary.mappings.issuance.addRequirements
import com.microsoft.walletlibrary.requests.requirements.Requirement
import com.microsoft.walletlibrary.util.VerifiedIdResponseCompletionException
import com.microsoft.walletlibrary.verifiedid.VerifiableCredential
import com.microsoft.walletlibrary.verifiedid.VerifiedId

/**
 * Wrapper class to wrap the completion of Issuance Request via VC SDK, map the received VerifiableCredential to VerifiedId and return it.
 */
object VerifiedIdRequester {
    // sends the issuance response to VC SDK and returns a VerifiedId if successful.
    internal suspend fun sendIssuanceResponse(
        issuanceRequest: IssuanceRequest,
        requirement: Requirement
    ): VerifiedId {
        val issuanceResponse = IssuanceResponse(issuanceRequest)
        issuanceResponse.addRequirements(requirement)
        val result = VerifiableCredentialSdk.issuanceService.sendResponse(issuanceResponse)
        if (result.isSuccess) {
            return VerifiableCredential(result.getOrThrow(), issuanceRequest.contract)
        } else {
            throw VerifiedIdResponseCompletionException(
                "Unable to complete issuance response",
                result.exceptionOrNull()
            )
        }
    }

    internal suspend fun sendIssuanceCallback(
        issuanceCompletionResponse: IssuanceCompletionResponse?,
        issuanceCallbackUrl: String?
    ) {
        if (issuanceCompletionResponse != null && issuanceCallbackUrl != null) {
            VerifiedIdCompletionCallBack.sendIssuanceCompletionResponse(
                issuanceCompletionResponse,
                issuanceCallbackUrl
            )
        }
    }
}