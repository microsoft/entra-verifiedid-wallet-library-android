/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests.rawrequests

import com.microsoft.did.sdk.credential.service.Request

/**
 * Represents the OpenID raw request and the request type.
 */
data class OpenIdRawRequest(
    override val requestType: RequestType,
    override val rawRequest: Request
): RawRequest