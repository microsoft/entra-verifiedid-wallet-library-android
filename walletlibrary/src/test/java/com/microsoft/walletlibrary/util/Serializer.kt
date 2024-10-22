package com.microsoft.walletlibrary.util

import com.microsoft.walletlibrary.requests.styles.BasicVerifiedIdStyle
import com.microsoft.walletlibrary.requests.styles.VerifiedIdStyle
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
        polymorphic(VerifiedIdStyle::class) {
            subclass(BasicVerifiedIdStyle::class)
        }
    }
    ignoreUnknownKeys = true
    isLenient = true
}