// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.did.sdk.backup.content

import com.microsoft.walletlibrary.did.sdk.backup.UnprotectedBackup

interface BackupProcessor {
    suspend fun export(backup: UnprotectedBackup): UnprotectedBackupData

    suspend fun import(backupData: UnprotectedBackupData): UnprotectedBackup
}