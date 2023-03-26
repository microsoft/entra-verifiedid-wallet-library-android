/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.wrapper

import com.microsoft.did.sdk.VerifiableCredentialSdk
import com.microsoft.did.sdk.credential.service.models.issuancecallback.IssuanceCompletionResponse
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.walletlibrary.requests.rawrequests.RawManifest
import com.microsoft.walletlibrary.util.VerifiedIdRequestFetchException

/**
 * Wrapper class to wrap the get Issuance Request from VC SDK and return a raw request.
 */
internal object ManifestResolver {

    // Fetches the issuance request from VC SDK using the url and converts it to raw request.
    suspend fun getIssuanceRequest(uri: String, requestState: String?, issuanceCallbackUrl: String?): RawManifest {
        return when (val issuanceRequestResult =
            VerifiableCredentialSdk.issuanceService.getRequest(uri)) {
            is Result.Success -> {
                val request = issuanceRequestResult.payload
                RawManifest(request)
            }
            is Result.Failure -> {
                val issuanceCompletionResponse = requestState?.let {
                    IssuanceCompletionResponse(
                        IssuanceCompletionResponse.IssuanceCompletionCode.ISSUANCE_FAILED,
                        it,
                        IssuanceCompletionResponse.IssuanceCompletionErrorDetails.FETCH_CONTRACT_ERROR,
                    )
                }
                if (issuanceCompletionResponse != null && issuanceCallbackUrl != null)
                    VerifiedIdCompletionCallBack.sendIssuanceCompletionResponse(issuanceCompletionResponse, issuanceCallbackUrl)
                throw VerifiedIdRequestFetchException(
                    "Unable to fetch issuance request",
                    issuanceRequestResult.payload
                )
            }
        }
    }
}