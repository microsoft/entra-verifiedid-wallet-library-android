/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests

import com.microsoft.walletlibrary.requests.requirements.Requirement
import com.microsoft.walletlibrary.requests.styles.RequesterStyle
import com.microsoft.walletlibrary.util.VerifiedIdResult

/**
 * VerifiedIdRequest handles information contained in a request like the visual styling of requester,
 * requirements needed in order to complete a request and information about trust model of requester
 * like domain url and verification status.
 */
interface VerifiedIdRequest<out T>: VerifiedIdPartialRequest {
    // Indicates whether request is satisfied on client side.
    fun isSatisfied(): Boolean

    // Completes the request and returns a generic object if successful.
    suspend fun complete(): VerifiedIdResult<T>

    // Cancels the request with an optional message.
    suspend fun cancel(message: String? = null): VerifiedIdResult<Unit>
}