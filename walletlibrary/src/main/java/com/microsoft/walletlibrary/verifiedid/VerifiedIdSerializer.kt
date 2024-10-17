/*---------------------------------------------------------------------------------------------
*  Copyright (c) Microsoft Corporation. All rights reserved.
*  Licensed under the MIT License. See License.txt in the project root for license information.
*--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.verifiedid

/**
 * Defines the behavior of serializing a Verified ID
 */
sealed interface VerifiedIdSerializer<SerializedFormat> {

    class VerifiedIdSerializationNotSupported: Error() {

    }

    /**
     * Serialize the given verifiedID into the SerializedFormat
     */
    @Throws
    fun serialize(verifiedId: VerifiedId): SerializedFormat
}
