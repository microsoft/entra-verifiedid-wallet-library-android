// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.did.sdk.backup

import com.microsoft.walletlibrary.did.sdk.backup.container.jwe.JwePasswordProtectedBackupData
import com.microsoft.walletlibrary.did.sdk.backup.content.ProtectedBackupData
import com.microsoft.walletlibrary.did.sdk.backup.content.microsoft2020.Microsoft2020UnprotectedBackupData
import com.microsoft.walletlibrary.did.sdk.crypto.protocols.jose.jwe.JweToken
import com.microsoft.walletlibrary.did.sdk.util.controlflow.UnknownBackupFormatException
import com.microsoft.walletlibrary.did.sdk.util.controlflow.UnknownProtectionMethodException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class BackupParser @Inject constructor() {
    fun parseBackup(jweString: String): ProtectedBackupData {
        val token = JweToken.deserialize(jweString)
        val cty = token.contentType
        // for now we only know microsoft password, fail early.
        if (cty != Microsoft2020UnprotectedBackupData.MICROSOFT_BACKUP_TYPE) {
            throw UnknownBackupFormatException("Backup of an unknown format: $cty")
        }
        val alg = token.getKeyAlgorithm()
        if (alg.name.startsWith("PBE")) {
            return JwePasswordProtectedBackupData(token)
        } else {
            throw UnknownProtectionMethodException("Unknown backup protection method: $alg")
        }
    }
}