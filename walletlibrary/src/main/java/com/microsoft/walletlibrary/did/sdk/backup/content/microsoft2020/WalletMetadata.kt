package com.microsoft.walletlibrary.did.sdk.backup.content.microsoft2020

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("BaseWallet")
internal open class WalletMetadata {
    var seed: String = ""
}