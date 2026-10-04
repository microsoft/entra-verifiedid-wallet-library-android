package com.microsoft.walletlibrary.mappings

import com.microsoft.walletlibrary.did.sdk.crypto.protocols.jose.JwaCryptoHelper
import com.microsoft.walletlibrary.did.sdk.identifier.models.identifierdocument.IdentifierDocument
import com.nimbusds.jose.jwk.JWK

internal fun IdentifierDocument.getJwk(
    id: String,
    expectedDid: String,
    validateDid: Boolean
): JWK? {
    if (verificationMethod.isNullOrEmpty()) return null
    for (publicKey in verificationMethod) {
        val (verificationMethodDid, verificationMethodKeyId) =
            JwaCryptoHelper.extractDidAndKeyId(publicKey.id, validateDid)
        if (verificationMethodKeyId == id &&
            (!validateDid || verificationMethodDid == null || verificationMethodDid == expectedDid)
        ) {
            return publicKey.publicKeyJwk
        }
    }
    return null
}