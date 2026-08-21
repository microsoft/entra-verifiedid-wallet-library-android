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
            val did = matches.groupValues[1]
            val fragment = matches.groupValues[2]
            if (did.isNotBlank() && !isSyntacticallyValidDid(did)) {
                throw ValidatorException("JWS key id contains an invalid DID: $did")
            }
            Pair(
                if (did.isNotBlank()) {
                    did
                } else {
                    null
                }, fragment
            )
        } else matches
    }

    // Reject DID values containing characters that could be used to manipulate resolver request
    // construction (path traversal, query/path injection, whitespace) instead of a valid DID.
    private fun isSyntacticallyValidDid(did: String): Boolean {
        return Regex("^did:[a-zA-Z0-9]+:[A-Za-z0-9._%-]+(?::[A-Za-z0-9._%-]+)*$").matches(did)
    }
}