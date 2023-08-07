// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.requests

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject

@Serializer(forClass = VerifiedIdRequest::class)
object VerifiedIdRequestSerializer : KSerializer<VerifiedIdRequest<*>> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("verifiedIdRequest") {
        element("manifestIssuanceRequest", ManifestIssuanceRequest.serializer().descriptor)
        element("openIdPresentationRequest", OpenIdPresentationRequest.serializer().descriptor)
    }

    override fun deserialize(decoder: Decoder): VerifiedIdRequest<*> {
        require(decoder is JsonDecoder)
        val input = decoder.decodeJsonElement()
        return if (input is JsonObject && "verifiedIdStyle" in input)
            decoder.json.decodeFromJsonElement(ManifestIssuanceRequest.serializer(), input)
        else
            decoder.json.decodeFromJsonElement(OpenIdPresentationRequest.serializer(), input)
    }

    override fun serialize(encoder: Encoder, value: VerifiedIdRequest<*>) {
        when (value) {
            is ManifestIssuanceRequest -> encoder.encodeSerializableValue(ManifestIssuanceRequest.serializer(), value)
            is OpenIdPresentationRequest -> encoder.encodeSerializableValue(OpenIdPresentationRequest.serializer(), value)
        }
    }
}
