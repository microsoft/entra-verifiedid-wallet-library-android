package com.microsoft.walletlibrary.did.sdk.crypto.protocols.jose

import com.microsoft.walletlibrary.did.sdk.util.controlflow.ValidatorException

internal object JwaCryptoHelper {
    fun extractDidAndKeyId(keyId: String): Pair<String?, String> {
        val match = matchDidAndKeyId(keyId)
        return match ?: throw ValidatorException("JWS contains no key id")
        }

    fun extractDidAndKeyRef(keyId: String): Pair<String?, String> {
        val match = matchDidAndKeyId(keyId)
        return match ?: Pair(null, keyId)
    }

    private fun matchDidAndKeyId(keyId: String): Pair<String?, String>? {
        val matches = Regex("^([^#]*)#(.+)$").matchEntire(keyId)
        return if (matches != null) {
            Pair(
                if (matches.groupValues[1].isNotBlank()) {
                    matches.groupValues[1]
                } else {
                    null
                }, matches.groupValues[2]
            )
        } else matches
    }
}