package com.microsoft.walletlibrarydemo.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class EncodedVerifiedId(
    @PrimaryKey
    val vcId: String,

    val verifiedId: String
)