/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.wrapper

import com.microsoft.did.sdk.VerifiableCredentialSdk
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.walletlibrary.requests.rawrequests.OpenIdRawRequest
import com.microsoft.walletlibrary.util.VerifiedIdRequestFetchException

/**
 * Wrapper class to wrap the getRequest call from VC SDK and return a raw request.
 */
object OpenIdForVCResolver {

    // Fetches the request from VC SDK using the url and converts it to raw request.
    suspend fun getRequest(uri: String): OpenIdRawRequest {
        return when (val presentationRequestResult =
            VerifiableCredentialSdk.presentationService.getRequest(uri)) {
            is Result.Success -> {
                OpenIdRawRequest(presentationRequestResult.payload)
            }
            is Result.Failure -> {
                throw VerifiedIdRequestFetchException("", presentationRequestResult.payload)
            }
        }
    }
}