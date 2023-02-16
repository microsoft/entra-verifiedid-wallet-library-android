/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests.handlers

import com.microsoft.walletlibrary.requests.VerifiedIdRequest
import com.microsoft.walletlibrary.requests.rawrequests.RawRequest

/**
 * An implementation of RequestHandler is protocol specific. It can handle and process the raw request and returns a VerifiedIdRequest.
 */
internal interface RequestHandler {

    // Handle and process the provided raw request and returns a VerifiedIdRequest.
    suspend fun handleRequest(rawRequest: RawRequest): VerifiedIdRequest
}