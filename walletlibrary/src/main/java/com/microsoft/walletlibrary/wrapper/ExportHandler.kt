// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.wrapper

import com.microsoft.walletlibrary.did.sdk.VerifiableCredentialSdk
import com.microsoft.walletlibrary.did.sdk.backup.UnprotectedBackup
import com.microsoft.walletlibrary.did.sdk.backup.container.ProtectionMethod
import com.microsoft.walletlibrary.did.sdk.backup.content.ProtectedBackupData
import com.microsoft.walletlibrary.util.VerifiedIdResult
import com.microsoft.walletlibrary.util.getResult

object ExportHandler {

    suspend fun exportBackup(unprotectedBackup: UnprotectedBackup, protectionMethod: ProtectionMethod): VerifiedIdResult<ProtectedBackupData> {
        return getResult {
            when(val backupResult = VerifiableCredentialSdk.backupService.exportBackup(unprotectedBackup, protectionMethod)) {
                is com.microsoft.walletlibrary.did.sdk.util.controlflow.Result.Success -> {
                    backupResult.payload
                }
                is com.microsoft.walletlibrary.did.sdk.util.controlflow.Result.Failure -> {
                    throw backupResult.payload
                }
            }

        }
    }
}