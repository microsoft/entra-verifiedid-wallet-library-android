// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.did.sdk.backup.container.jwe

import com.microsoft.walletlibrary.did.sdk.backup.content.ProtectedBackupData
import com.microsoft.walletlibrary.did.sdk.crypto.protocols.jose.jwe.JweToken

data class JwePasswordProtectedBackupData constructor(
    val jweToken: JweToken,
) : ProtectedBackupData() {

    override fun serialize(): String {
        return jweToken.serialize()
    }
}