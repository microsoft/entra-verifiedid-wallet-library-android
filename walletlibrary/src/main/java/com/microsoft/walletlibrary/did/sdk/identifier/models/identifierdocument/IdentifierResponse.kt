// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.did.sdk.identifier.models.identifierdocument

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("document")
internal data class IdentifierResponse(
    @SerialName("identifierDocument")
    val identifierDocument: IdentifierDocument,
    @SerialName("@context")
    val context: String = "https://www.w3.org/ns/did-resolution/v1"
) {
    @SerialName("methodMetadata")
    val identifierMetadata: IdentifierMetadata? = null
}