package com.microsoft.walletlibrary.did.sdk.backup.content.microsoft2020

import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.display.DisplayContract
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("BaseVC")
abstract class VcMetadata {
    abstract val displayContract: DisplayContract
}
