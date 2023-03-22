/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.wrapper

import com.microsoft.did.sdk.VerifiableCredentialSdk
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.walletlibrary.requests.rawrequests.OpenIdRawRequest
import com.microsoft.walletlibrary.requests.rawrequests.VerifiedIdOpenIdJwtRawRequest
import com.microsoft.walletlibrary.util.VerifiedIdRequestFetchException

/**
 * Wrapper class to wrap the get Presentation Request from VC SDK and return a raw request.
 */
object OpenIdResolver {

    // Fetches the presentation request from VC SDK using the url and converts it to raw request.
    internal suspend fun getRequest(uri: String): OpenIdRawRequest {
        when (val presentationRequestResult =
            VerifiableCredentialSdk.presentationService.getRequest(uri)) {
            is Result.Success -> {
                val request = presentationRequestResult.payload
                return VerifiedIdOpenIdJwtRawRequest(request)
            }
            is Result.Failure -> {
                throw VerifiedIdRequestFetchException(
                    "Unable to fetch presentation request",
                    presentationRequestResult.payload
                )
            }
        }
    }
}