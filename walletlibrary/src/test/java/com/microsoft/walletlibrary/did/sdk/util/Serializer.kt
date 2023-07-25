// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.did.sdk.util

import com.microsoft.walletlibrary.did.sdk.backup.content.microsoft2020.TestVcMetaData
import com.microsoft.walletlibrary.did.sdk.backup.content.microsoft2020.VcMetadata
import com.microsoft.walletlibrary.did.sdk.di.SdkModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

// Keep in sync with `fun defaultJsonSerializer()` in SdkModule
val defaultTestSerializer = SdkModule().defaultJsonSerializer(
    SerializersModule {
        polymorphic(VcMetadata::class) {
            subclass(TestVcMetaData::class)
        }
    }
)