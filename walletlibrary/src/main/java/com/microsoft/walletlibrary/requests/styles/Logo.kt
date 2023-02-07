/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests.styles

/**
 * Holds information describing a logo.
 */
data class Logo(
    // If image needs to be fetched, service will use this property
    var uri: String? = null,

    // Else if image is in svg or base64 format, service will use this property
    var image: String? = null,

    // Description used for alt text or voice over
    val description: String
)