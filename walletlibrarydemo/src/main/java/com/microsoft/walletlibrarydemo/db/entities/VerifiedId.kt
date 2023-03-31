package com.microsoft.walletlibrarydemo.db.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.microsoft.walletlibrary.verifiedid.VerifiableCredential
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class VerifiedId(
    @PrimaryKey
    val vcId: String,

    @Embedded
    val verifiableCredential: VerifiableCredential
)