/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.did.sdk.credential.models

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonTransformingSerializer

/**
 * Deserialises [CredentialStatusDescriptor] from either W3C VC shape: an object
 * (`"credentialStatus": {...}`) or an array (`"credentialStatus": [{...}]`). The wallet acts on a
 * single descriptor, so the array form keeps the first entry rather than failing to deserialise.
 */
internal object CredentialStatusDescriptorSerializer :
    JsonTransformingSerializer<CredentialStatusDescriptor>(CredentialStatusDescriptor.serializer()) {

    override fun transformDeserialize(element: JsonElement): JsonElement {
        if (element !is JsonArray) return element
        return element.firstOrNull() ?: JsonNull
    }
}
