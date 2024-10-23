package com.microsoft.walletlibrary.verifiedid

object StringVerifiedIdSerializer : VerifiedIdSerializer<String> {
    /**
     * Serialize the given verifiedID into the SerializedFormat
     */
    override fun serialize(verifiedId: VerifiedId): String {
        when (verifiedId) {
            is VerifiableCredential -> {
                return verifiedId.raw.raw
            }
            else -> {
                throw VerifiedIdSerializer.VerifiedIdSerializationNotSupported()
            }
        }
    }
}