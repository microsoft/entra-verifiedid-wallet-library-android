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
 * Deserialises [CredentialStatusDescriptor] from either of the two W3C VC shapes:
 *
 *  - Object form (the common case Entra emits today):
 *    `"credentialStatus": { "id": "...", "type": "..." }`
 *  - Array form (allowed by the W3C VC Data Model so an issuer can attach multiple
 *    purposes — e.g. one revocation list and one suspension list):
 *    `"credentialStatus": [ { ... }, { ... } ]`
 *
 * Today the wallet only acts on a single descriptor, so for the array case we keep the
 * first entry. This prevents the whole VC from failing to deserialise if an issuer ever
 * starts emitting the array form. When suspension support is added, this should be
 * upgraded to expose the full list.
 */
internal object CredentialStatusDescriptorSerializer :
    JsonTransformingSerializer<CredentialStatusDescriptor>(CredentialStatusDescriptor.serializer()) {

    override fun transformDeserialize(element: JsonElement): JsonElement {
        if (element !is JsonArray) return element
        return element.firstOrNull() ?: JsonNull
    }
}
