// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.did.sdk.backup.content.microsoft2024

import com.microsoft.walletlibrary.did.sdk.backup.content.UnprotectedBackupData
import com.microsoft.walletlibrary.did.sdk.backup.content.microsoft2020.Microsoft2020UnprotectedBackupData
import com.microsoft.walletlibrary.did.sdk.backup.content.microsoft2020.RawIdentity
import com.microsoft.walletlibrary.did.sdk.backup.content.microsoft2020.VcMetadata
import com.microsoft.walletlibrary.did.sdk.backup.content.microsoft2020.WalletMetadata
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(Microsoft2020UnprotectedBackupData.MICROSOFT_BACKUP_TYPE)
internal data class Microsoft2024UnProtectedBackupData(
    val vcs: Map<String, String>,
    val vcsMetaInf: Map<String, VcMetadata>,
    val metaInf: WalletMetadata,
    val identifiers: List<RawIdentity>
) : UnprotectedBackupData() {
    override val type: String
        get() = MICROSOFT_BACKUP_TYPE

    companion object {
        const val MICROSOFT_BACKUP_TYPE = "MicrosoftWallet2024"
    }
}