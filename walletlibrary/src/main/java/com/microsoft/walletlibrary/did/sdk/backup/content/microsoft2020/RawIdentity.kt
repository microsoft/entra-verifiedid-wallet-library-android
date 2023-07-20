@file:UseSerializers(JwkSerializer::class)

package com.microsoft.walletlibrary.did.sdk.backup.content.microsoft2020

import com.microsoft.walletlibrary.did.sdk.crypto.protocols.jose.serialization.JwkSerializer
import com.nimbusds.jose.jwk.JWK
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
internal data class RawIdentity(
    val id: String,
    val name: String,
    val keys: List<JWK>,
    val recoveryKey: String,
    val updateKey: String
)