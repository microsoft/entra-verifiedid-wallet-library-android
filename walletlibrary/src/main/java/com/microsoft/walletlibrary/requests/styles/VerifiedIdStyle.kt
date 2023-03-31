/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests.styles

import kotlinx.serialization.Serializable

/**
 * Holds information describing look and feel of a Verified Id.
 */
@Serializable
abstract class VerifiedIdStyle {
    // The name of the Verified Id.
    abstract val name: String
}