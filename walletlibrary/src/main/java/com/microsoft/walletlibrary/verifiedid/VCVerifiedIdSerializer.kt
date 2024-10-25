// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.verifiedid

import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.VerifiableCredentialContract

object VCVerifiedIdSerializer : VerifiedIdSerializer<com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredential> {
    /**
     * Serialize the given verifiedID into the SerializedFormat
     */
    override fun serialize(verifiedId: VerifiedId): com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredential {
        return when (verifiedId) {
            is VerifiableCredential -> {
                verifiedId.raw
            }
            is OpenId4VciVerifiedId -> {
                verifiedId.raw
            }

            else -> {
                throw VerifiedIdSerializer.VerifiedIdSerializationNotSupported()
            }
        }
    }

    fun deserialize(
        rawVc: com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredential,
        contract: VerifiableCredentialContract? = null
    ): VerifiedId {
        return VerifiableCredential(rawVc, contract, rawVc.contents.vc.type)
    }
}