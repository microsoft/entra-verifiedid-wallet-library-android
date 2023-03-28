package com.microsoft.walletlibrary.util

import com.microsoft.walletlibrary.verifiedid.VerifiableCredential
import com.microsoft.walletlibrary.verifiedid.VerifiedId
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

val defaultTestSerializer = Json {
    serializersModule = SerializersModule {
        polymorphic(VerifiedId::class) {
            subclass(VerifiableCredential::class)
        }
    }
    ignoreUnknownKeys = true
    isLenient = true
}