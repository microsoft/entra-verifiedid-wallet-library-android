// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.datasource.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
internal data class HolderIdentifierData (
    @PrimaryKey
    val keyId: String,
    val didMethod: String,
    val algorithm: String,
    val keyReference: String
)