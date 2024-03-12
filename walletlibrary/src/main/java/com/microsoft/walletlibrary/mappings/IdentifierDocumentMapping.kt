package com.microsoft.walletlibrary.mappings

import com.microsoft.walletlibrary.did.sdk.identifier.models.identifierdocument.IdentifierDocument
import com.nimbusds.jose.jwk.JWK

internal fun IdentifierDocument.getJwk(id: String): JWK? {
    if (verificationMethod.isNullOrEmpty()) return null
    for (publicKey in verificationMethod) {
        if (publicKey.id == id) {
            return publicKey.publicKeyJwk
        }
    }
    return null
}