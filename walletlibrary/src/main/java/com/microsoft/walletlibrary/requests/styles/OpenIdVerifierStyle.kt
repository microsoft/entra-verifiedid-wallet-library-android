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
data class OpenIdVerifierStyle(
    // Name of the requester.
    override val name: String,

    // Did of the requester.
    val did: String,

    // Logo of the requester.
    val verifierLogo: VerifiedIdLogo? = null
) : RequesterStyle