/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests.rawrequests

import com.microsoft.walletlibrary.requests.VerifiedIdRequest

/**
 * Represents the OpenID raw request and the request type.
 */
internal interface OpenIdRawRequest: RawRequest {
    fun handleRawRequest(): VerifiedIdRequest
}
