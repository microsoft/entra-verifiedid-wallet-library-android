/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.did.sdk.credential.service.models.oidc

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonTransformingSerializer

@Serializable
internal data class Claims(
    @Serializable(with = VPTokenRequestSerializer::class)
    @SerialName("vp_token")
    val vpTokensInRequest: List<VpTokenInRequest>
)

internal class VPTokenRequestSerializer : JsonTransformingSerializer<List<VpTokenInRequest>>(ListSerializer(VpTokenInRequest.serializer())) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        return if (element !is JsonArray) {
            JsonArray(listOf(element))
        } else {
            element
        }
    }

    override fun transformSerialize(element: JsonElement): JsonElement {
        val arrayElement = element as JsonArray
        return if (arrayElement.count() == 1) {
            arrayElement.first()
        } else {
            arrayElement
        }
    }
}