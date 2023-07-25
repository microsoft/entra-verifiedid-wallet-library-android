/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.wrapper

import com.microsoft.walletlibrary.did.sdk.VerifiableCredentialSdk
import com.microsoft.walletlibrary.did.sdk.credential.service.IssuanceRequest
import com.microsoft.walletlibrary.did.sdk.credential.service.IssuanceResponse
import com.microsoft.walletlibrary.did.sdk.credential.service.models.issuancecallback.IssuanceCompletionResponse
import com.microsoft.walletlibrary.did.sdk.util.controlflow.NetworkException
import com.microsoft.walletlibrary.did.sdk.util.controlflow.Result
import com.microsoft.walletlibrary.mappings.issuance.addRequirements
import com.microsoft.walletlibrary.requests.requirements.Requirement
import com.microsoft.walletlibrary.util.VerifiedIdResponseCompletionException
import com.microsoft.walletlibrary.util.WalletLibraryException
import com.microsoft.walletlibrary.util.WalletLibraryLogger
import com.microsoft.walletlibrary.verifiedid.VerifiableCredential
import com.microsoft.walletlibrary.verifiedid.VerifiedId

/**
 * Wrapper class to wrap the completion of Issuance Request via VC SDK, map the received VerifiableCredential to VerifiedId and return it.
 */
object VerifiedIdRequester {
    // sends the issuance response to VC SDK and returns a VerifiedId if successful.
    internal suspend fun sendIssuanceResponse(
        issuanceRequest: IssuanceRequest,
        requirement: Requirement,
        requestState: String? = null,
        issuanceCallbackUrl: String? = null
    ): VerifiedId {
        val issuanceResponse = IssuanceResponse(issuanceRequest)
        issuanceResponse.addRequirements(requirement)
        when (val result = VerifiableCredentialSdk.issuanceService.sendResponse(issuanceResponse)) {
            is Result.Success -> {
                try {
                    val issuanceCompletionResponse = requestState?.let {
                        IssuanceCompletionResponse(
                            IssuanceCompletionResponse.IssuanceCompletionCode.ISSUANCE_SUCCESSFUL,
                            it,
                            null
                        )
                    }
                    sendIssuanceCallback(issuanceCompletionResponse, issuanceCallbackUrl)
                } catch (exception: WalletLibraryException) {
                    WalletLibraryLogger.e(
                        "Unable to send issuance callback after issuance completes",
                        exception
                    )
                }
                return VerifiableCredential(result.payload, issuanceRequest.contract)
            }
            is Result.Failure -> {
                val details = when (result.payload) {
                    is NetworkException -> IssuanceCompletionResponse.IssuanceCompletionErrorDetails.ISSUANCE_SERVICE_ERROR
                    else -> IssuanceCompletionResponse.IssuanceCompletionErrorDetails.UNSPECIFIED_ERROR
                }
                try {
                    val issuanceCompletionResponse = requestState?.let {
                        IssuanceCompletionResponse(
                            IssuanceCompletionResponse.IssuanceCompletionCode.ISSUANCE_FAILED,
                            it,
                            details
                        )
                    }
                    sendIssuanceCallback(issuanceCompletionResponse, issuanceCallbackUrl)
                } catch (exception: WalletLibraryException) {
                    WalletLibraryLogger.e(
                        "Unable to send issuance callback after issuance fails",
                        exception
                    )
                }
                throw VerifiedIdResponseCompletionException(
                    "Unable to complete issuance response",
                    result.payload
                )
            }
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