@file:UseSerializers(JwkSerializer::class)

package com.microsoft.walletlibrary.did.sdk.backup.content.microsoft2024

import com.microsoft.walletlibrary.did.sdk.crypto.protocols.jose.serialization.JwkSerializer
import com.nimbusds.jose.jwk.JWK
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
internal data class RawHolderIdentifier(
    val id: String,
    val didMethod: String,
    val algorithm: String,
    val keys: List<JWK>,
    val keyReference: String? = null,
    val identifierType: String
)