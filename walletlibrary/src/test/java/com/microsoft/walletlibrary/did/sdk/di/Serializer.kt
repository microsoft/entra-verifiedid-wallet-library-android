// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.did.sdk.di

import kotlinx.serialization.modules.SerializersModule

val defaultTestSerializer = SdkModule().defaultJsonSerializer(
    SerializersModule {
    }
)