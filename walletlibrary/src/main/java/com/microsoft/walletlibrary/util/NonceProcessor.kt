package com.microsoft.walletlibrary.util

import android.util.Base64
import com.microsoft.walletlibrary.did.sdk.crypto.CryptoOperations
import com.microsoft.walletlibrary.did.sdk.crypto.DigestAlgorithm
import com.microsoft.walletlibrary.did.sdk.util.Constants
import java.security.SecureRandom

internal object NonceProcessor {

    /**
     * Generates a random string that is used to associate a client session with an ID Token, and to mitigate replay
     * attacks.
     * @return Hash of DID appended to random string
     */
    fun getNonce(did: String): String {
        return generateSecureRandomString() + "." + getDidHash(did)
    }

    private fun getDidHash(did: String): String {
        val digest = CryptoOperations.digest(did.toByteArray(), DigestAlgorithm.Sha512)
        return Base64.encodeToString(digest, Constants.BASE64_URL_SAFE)
    }

    private fun generateSecureRandomString(): String {
        val random = SecureRandom()
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return Base64.encodeToString(bytes, Constants.BASE64_URL_SAFE)
    }
}