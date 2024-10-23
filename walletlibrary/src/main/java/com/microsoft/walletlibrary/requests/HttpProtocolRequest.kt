/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests

interface HttpProtocolRequest {
    // Sets additional headers for this request's response
    fun setAdditionalHeaders(headers: Map<String, String>)
}