/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary

import com.microsoft.walletlibrary.requests.VerifiedIdRequest

/**
 * VerifiedIdClient is configured by builder and is used to create requests.
 * */
class VerifiedIdClient {

    // Creates an issuance or presentation request based on the provided input.
    suspend fun createRequest(verifiedIdClientInput: VerifiedIdClientInput): VerifiedIdRequest {
        TODO("Not yet implemented")
    }
}