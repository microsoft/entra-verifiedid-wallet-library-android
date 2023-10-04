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

/**
 * Contents of an OpenID Self-Issued Token Response.
 *
 * @see [OpenID Spec](https://openid.net/specs/openid-connect-core-1_0.html#JWTRequests)
 */
@Serializable
internal data class PresentationResponseClaims(
    @Serializable(with = VPTokenResponseSerializer::class)
    @SerialName("_vp_token")
    val vpToken: List<VpTokenInResponse>,

    var nonce: String = ""
    ) : OidcResponseClaims()

internal class VPTokenResponseSerializer : JsonTransformingSerializer<List<VpTokenInResponse>>(
    ListSerializer(VpTokenInResponse.serializer())
) {
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