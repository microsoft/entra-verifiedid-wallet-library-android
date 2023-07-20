// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.did.sdk.identifier.models.identifierdocument

import com.microsoft.walletlibrary.did.sdk.crypto.protocols.jose.serialization.JwkSerializer
import com.nimbusds.jose.jwk.JWK
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class IdentifierMetadata(
    @SerialName("operationPublicKeys")
    val operationPublicKeys: List<IdentifierDocumentPublicKey>? = null,
    @Serializable(with = JwkSerializer::class)
    val recoveryKey: JWK? = null
)