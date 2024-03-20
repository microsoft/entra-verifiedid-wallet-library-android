/*---------------------------------------------------------------------------------------------
*  Copyright (c) Microsoft Corporation. All rights reserved.
*  Licensed under the MIT License. See License.txt in the project root for license information.
*--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests.handlers

import com.microsoft.walletlibrary.requests.requirements.Requirement
import com.microsoft.walletlibrary.verifiedid.VerifiedIdSerializer

/**
 * Visitor and builder used by RequestProcessors to serialize Requirements.
 */
sealed interface RequestProcessorSerializer {
    /**
     * Processes and serializes this requirement using Requirement.serialize
     * note: Requirement.Serialize must be called and is expected to call this method on any child requirements before returning
     */
    fun <SerializedFormat: Any> serialize(
        requirement: Requirement,
        verifiedIdSerializer: VerifiedIdSerializer<SerializedFormat>
    ): Void
}
