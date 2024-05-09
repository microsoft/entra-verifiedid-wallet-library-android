// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.verifiedid

import com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredential
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject

object VerifiableCredentialSerializer : KSerializer<VerifiedId> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("verifiedId") {
        element("verifiableCredential", com.microsoft.walletlibrary.verifiedid.VerifiableCredential.serializer().descriptor)
        element("openId4VciVerifiedId", OpenId4VciVerifiedId.serializer().descriptor)
    }

    override fun deserialize(decoder: Decoder): VerifiedId {
        val json = ((decoder as JsonDecoder).decodeJsonElement() as JsonObject)
        return VerifiableCredential(parseRaw(json))
    }

    override fun serialize(encoder: Encoder, value: VerifiedId) {
        TODO("Not yet implemented")
    }

    private fun parseRaw(json: JsonObject): VerifiableCredential {
        val raw = json["raw"] ?: throw VerifiedIdSerializer.VerifiedIdSerializationNotSupported()
        return try {
            Json.decodeFromString(VerifiableCredential.serializer(), raw.toString())
        } catch (e: Exception) {
            throw VerifiedIdSerializer.VerifiedIdSerializationNotSupported()
        }
    }
}