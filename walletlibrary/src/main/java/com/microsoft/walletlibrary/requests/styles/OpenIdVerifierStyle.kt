/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests.styles

/**
 * Holds information describing look and feel of a requester.
 */
data class OpenIdVerifierStyle(
    // Name of the requester.
    override val requester: String,

    // Logo of the requester.
    val verifierLogo: VerifiedIdLogo? = null
): RequesterStyle