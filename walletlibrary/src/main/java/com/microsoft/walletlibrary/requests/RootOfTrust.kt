/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests

import kotlinx.serialization.Serializable

/**
 * Information about trust model of requester like domain url and verification status.
 */
@Serializable
data class RootOfTrust(
    // Source of root of trust (eg. well-known endpoint url).
    val source: String,

    // Result of verification of source.
    val verified: Boolean = false
)