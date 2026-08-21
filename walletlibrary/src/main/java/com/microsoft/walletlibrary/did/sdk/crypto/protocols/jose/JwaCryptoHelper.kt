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

    internal fun isSyntacticallyValidDid(did: String): Boolean {
        if (!did.startsWith("did:")) return false

        val segments = did.removePrefix("did:").split(":")
        if (segments.size < 2) return false

        val method = segments.first()
        if (!method.matches(Regex("^[a-zA-Z0-9]+$"))) return false

        return segments.drop(1).all { segment ->
            segment.isNotBlank() &&
                segment != "." &&
                segment != ".." &&
                !segment.contains('/') &&
                !segment.contains('?') &&
                !segment.contains('#') &&
                !segment.any { it.isWhitespace() } &&
                segment.matches(Regex("^[A-Za-z0-9._%-]+$"))
        }
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
}