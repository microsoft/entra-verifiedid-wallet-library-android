/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests

import com.microsoft.walletlibrary.requests.requirements.Requirement
import com.microsoft.walletlibrary.requests.styles.RequesterStyle

/**
 * VerifiedIdRequest handles information contained in a request like the visual styling of requester,
 * requirements needed in order to complete a request and information about trust model of requester
 * like domain url and verification status.
 */
interface VerifiedIdRequest<out T> {
    // Attributes describing the requester (eg. name, logo).
    val requesterStyle: RequesterStyle

    // Information describing the requirements needed to complete the flow.
    val requirement: Requirement

    // Root of trust of the requester (eg. linked domains).
    val rootOfTrust: RootOfTrust

    // Indicates whether request is satisfied on client side.
    fun isSatisfied(): Boolean

    // Completes the request and returns a generic object if successful.
    suspend fun complete(): Result<T>

    // Cancels the request with an optional message.
    suspend fun cancel(message: String?): Result<Unit>
}