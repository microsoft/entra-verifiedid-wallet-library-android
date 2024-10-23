package com.microsoft.walletlibrary.verifiedid

object StringVerifiedIdSerializer : VerifiedIdSerializer<String> {
    /**
     * Serialize the given verifiedID into the SerializedFormat
     */
    override fun serialize(verifiedId: VerifiedId): String {
        return when (verifiedId) {
            is VerifiableCredential -> {
                verifiedId.raw.raw
            }

            is OpenId4VciVerifiedId -> {
                verifiedId.raw.raw
            }

            else -> {
                throw VerifiedIdSerializer.VerifiedIdSerializationNotSupported()
            }
        }
    }
}