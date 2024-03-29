/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests.styles

import android.net.Uri
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

/**
 * Holds information describing a logo.
 */
@Serializable
data class VerifiedIdLogo(
    @Contextual
    // If image needs to be fetched, service will use this property.
    var url: String? = null,

    // Description used for alt text or voice over.
    val altText: String? = null
)