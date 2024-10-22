package com.microsoft.walletlibrary.verifiedid

object StringVerifiedIdSerializer : VerifiedIdSerializer<String> {
    /**
     * Serialize the given verifiedID into the SerializedFormat
     */
    override fun serialize(verifiedId: VerifiedId): String {
        when (verifiedId) {
            is VerifiableCredential -> {
                return serializer.encodeToString(
                    com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredential.serializer(),
                    verifiedId.raw
                )
            }
            else -> {
                throw VerifiedIdSerializer.VerifiedIdSerializationNotSupported()
            }
        }
    }
}