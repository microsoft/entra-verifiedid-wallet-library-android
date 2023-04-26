// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.backup.content

import com.microsoft.did.sdk.backup.UnprotectedBackup

internal interface BackupProcessor {
    suspend fun export(backup: UnprotectedBackup): UnprotectedBackupData

    suspend fun import(backupData: UnprotectedBackupData): UnprotectedBackup
}