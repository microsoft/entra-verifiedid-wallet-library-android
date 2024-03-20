/*---------------------------------------------------------------------------------------------
*  Copyright (c) Microsoft Corporation. All rights reserved.
*  Licensed under the MIT License. See License.txt in the project root for license information.
*--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.verifiedid

import kotlin.reflect.KClass

/**
 * Defines the behavior of serializing a Verified ID
 */
sealed interface VerifiedIdSerializer<SerializedFormat : Any> {

    val serializedFormat: KClass<SerializedFormat>

    /**
     * Serialize the given verifiedID into the SerializedFormat
     */
    @Throws
    fun serialize(verifiedId: VerifiedId): SerializedFormat
}
