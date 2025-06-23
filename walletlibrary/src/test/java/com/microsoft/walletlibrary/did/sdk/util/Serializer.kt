// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.did.sdk.util

import com.microsoft.walletlibrary.did.sdk.di.SdkModule
import kotlinx.serialization.modules.SerializersModule

// Keep in sync with `fun defaultJsonSerializer()` in SdkModule
val defaultTestSerializer = SdkModule().defaultJsonSerializer(
    SerializersModule {
    }
)