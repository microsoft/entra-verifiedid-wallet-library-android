/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.datasource.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Metadata about a holder identifier and the key that is tied to it.
 */
@Entity
@Serializable
internal data class HolderIdentifierData(
    // The UUID of the private key in the encrypted shared preferences.
    @PrimaryKey
    val keyId: String,
    // The unique identifier of the holder used for the Verified ID.
    val id: String,
    // The DID method used to create the DID.
    val didMethod: String,
    // The algorithm of the key used for cryptographic operations.
    val algorithm: String,
    // The reference to the key that is used for cryptographic operations.
    val keyReference: String
) : HolderIdentifierStoredProperties()