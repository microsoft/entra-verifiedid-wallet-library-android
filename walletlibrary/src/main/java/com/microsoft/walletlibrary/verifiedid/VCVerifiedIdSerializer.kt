// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.verifiedid

import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.VerifiableCredentialContract
import kotlinx.serialization.json.Json

object VCVerifiedIdSerializer : VerifiedIdSerializer<String> {

    private val serializer = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Serialize the given verifiedID into the SerializedFormat
     */
    override fun serialize(verifiedId: VerifiedId): String {
        return when (verifiedId) {
            is VerifiableCredential -> {
                serializer.encodeToString(
                    com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredential.serializer(),
                    verifiedId.raw
                )
            }

            else -> {
                throw VerifiedIdSerializer.VerifiedIdSerializationNotSupported()
            }
        }
    }

    fun deserialize(
        rawVc: String,
        serializedContract: String? = null
    ): VerifiedId {
        val vc = serializer.decodeFromString(
            com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredential.serializer(),
            rawVc
        )
        val contract = serializedContract?.let {
            serializer.decodeFromString(VerifiableCredentialContract.serializer(), it)
        }
        return VerifiableCredential(vc, contract, vc.contents.vc.type)
    }
}