// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.identifier

import android.util.Base64
import com.microsoft.walletlibrary.did.sdk.crypto.CryptoOperations
import com.microsoft.walletlibrary.did.sdk.crypto.KeyGenAlgorithm
import com.microsoft.walletlibrary.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.walletlibrary.did.sdk.crypto.keyStore.toPrivateJwk
import com.microsoft.walletlibrary.did.sdk.util.Constants
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.KeyUse
import org.erdtman.jcs.JsonCanonicalizer
import java.util.UUID

internal class HolderIdentifierCreator(private val encryptedKeyStore: EncryptedKeyStore) {

    fun createHolderIdentifier(algorithm: String, didMethod: String): EncryptedSharedPreferencesIdentifier {
        val signingPublicKeyJwk = generateAndStoreKeyPair()
        val didJwk = createDidJwk(signingPublicKeyJwk)
        return EncryptedSharedPreferencesIdentifier(
            didJwk,
            algorithm,
            didMethod,
            signingPublicKeyJwk.keyID,
            encryptedKeyStore
        )
    }

    /**
     * Generates a new KeyPair and stores it in the keyStore.
     *
     * @return returns the public Key in JWK format
     */
    private fun generateAndStoreKeyPair(keyGenAlgorithm: KeyGenAlgorithm = KeyGenAlgorithm.P256, use: KeyUse = KeyUse.SIGNATURE): JWK {
        val keyId = generateRandomKeyId()
        val privateKey = CryptoOperations.generateKeyPair(keyGenAlgorithm).toPrivateJwk(keyId, use)
        encryptedKeyStore.storeKey(keyId, privateKey)
        return privateKey.toPublicJWK()
    }

    private fun generateRandomKeyId(): String {
        return UUID.randomUUID().toString().replace("-", "")
    }

    private fun createDidJwk(jwk: JWK): String {
        val utf8EncodedJwk = JsonCanonicalizer(jwk.toJSONString()).encodedUTF8
        return "did:jwk:" + Base64.encodeToString(utf8EncodedJwk, Constants.BASE64_URL_SAFE)
    }
}