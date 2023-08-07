/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests.styles

import kotlinx.serialization.Serializable

/**
 * Holds information describing look and feel of a requester.
 */
@Serializable
data class VerifiedIdManifestIssuerStyle(
    // Name of the requester.
    override val name: String,

    // The title of the request.
    val requestTitle: String? = null,

    // The instructions on the request.
    val requestInstructions: String? = null
): RequesterStyle