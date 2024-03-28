/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests.requirements

import com.microsoft.walletlibrary.requests.handlers.RequestProcessorSerializer
import com.microsoft.walletlibrary.util.VerifiedIdResult
import com.microsoft.walletlibrary.verifiedid.VerifiedIdSerializer

/**
 * Represents the necessary information required in order to complete a Verified ID request (issuance or presentation).
 */
interface Requirement {
    // Indicates whether the requirement is required or optional.
    val required: Boolean

    // Validates the requirement and throws an exception if the requirement is invalid or not fulfilled.
    fun validate(): VerifiedIdResult<Unit>

    /**
     * Serializes the requirement into its raw format.
     * If this requirement is composed or an aggregate of other requirements, MUST call the protocolSerializer's serialize function on all used requirements.
     * returns the raw format for a given SerializedFormat type (if it has output).
     */
    @Throws
    suspend fun <T> serialize(
        protocolSerializer: RequestProcessorSerializer<T>,
        verifiedIdSerializer: VerifiedIdSerializer<T>
    ): T?
}
