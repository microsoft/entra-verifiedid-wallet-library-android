package com.microsoft.walletlibrary.util

import android.util.Base64
import com.microsoft.walletlibrary.did.sdk.VerifiableCredentialSdk
import com.microsoft.walletlibrary.did.sdk.crypto.CryptoOperations
import com.microsoft.walletlibrary.did.sdk.crypto.DigestAlgorithm
import com.microsoft.walletlibrary.did.sdk.util.Constants
import com.microsoft.walletlibrary.did.sdk.util.controlflow.Result
import com.microsoft.walletlibrary.did.sdk.util.log.SdkLog
import kotlinx.coroutines.runBlocking
import java.security.SecureRandom

class NonceProcessor {

    /**
     * Generates a random string that is used to associate a client session with an ID Token, and to mitigate replay
     * attacks.
     * @return Hash of DID appended to random string
     */
    fun getNonce(): String {
        return generateSecureRandomString() + "." + getDidHash()
    }

    private fun getDidHash(): String {
        val did = runBlocking {
            when (val result = VerifiableCredentialSdk.identifierService.getMasterIdentifier()) {
                is Result.Success -> result.payload.id
                is Result.Failure -> {
                    SdkLog.e("Could not get DID", result.payload)
                    ""
                }
            }
        }
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