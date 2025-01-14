// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.did.sdk.backup.content.microsoft2024

import com.microsoft.walletlibrary.did.sdk.backup.UnprotectedBackup
import com.microsoft.walletlibrary.did.sdk.backup.content.microsoft2020.VcMetadata
import com.microsoft.walletlibrary.did.sdk.backup.content.microsoft2020.WalletMetadata

class Microsoft2024UnprotectedBackup(
    val walletMetadata: WalletMetadata,
    val verifiableCredentials: List<Pair<String, VcMetadata>>
) : UnprotectedBackup()