// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.identifier

import com.microsoft.walletlibrary.did.sdk.crypto.CryptoOperations
import com.microsoft.walletlibrary.did.sdk.crypto.KeyGenAlgorithm
import com.microsoft.walletlibrary.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.walletlibrary.did.sdk.crypto.keyStore.toPrivateJwk
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.KeyUse
import java.util.UUID

internal class HolderIdentifierCreator(private val encryptedKeyStore: EncryptedKeyStore) {

    fun createHolderIdentifier(
        algorithm: String,
        didMethod: String,
        keyId: String? = null
    ): EncryptedSharedPreferencesIdentifier {
        val signingPublicKeyJwk = keyId?.let { fetchKey(it).toPublicJWK() } ?: generateKeyPairAndStorePrivateKey(algorithm)
        val did = DidCreator.createDid(signingPublicKeyJwk, didMethod)
        // Key Reference is always 0 for did:jwk DIDs.
        return EncryptedSharedPreferencesIdentifier(
            did,
            algorithm,
            didMethod,
            "0",
            encryptedKeyStore,
            signingPublicKeyJwk.keyID
        )
    }

    private fun mapJWAToKeyGenAlgorithm(jwa: String): KeyGenAlgorithm {
        return when (jwa) {
            "ES256" -> KeyGenAlgorithm.P256
            else -> throw IllegalArgumentException("Unsupported algorithm")
        }
    }

    /**
     * Generates a new KeyPair and stores it in the keyStore.
     *
     * @return returns the public Key in JWK format
     */
    private fun generateKeyPairAndStorePrivateKey(
        algorithm: String,
        use: KeyUse = KeyUse.SIGNATURE
    ): JWK {
        val keyGenAlgorithm = mapJWAToKeyGenAlgorithm(algorithm)
        val keyId = generateRandomKeyId()
        val privateKey =
            CryptoOperations.generateKeyPair(keyGenAlgorithm).toPrivateJwk(keyId, use, Curve.P_256)
        encryptedKeyStore.storeKey(keyId, privateKey)
        return privateKey.toPublicJWK()
    }

    private fun generateRandomKeyId(): String {
        return UUID.randomUUID().toString().replace("-", "")
    }

    /**
     * Fetches the key from keyStore.
     *
     * @return returns the public Key in JWK format
     */
    private fun fetchKey(keyReference: String): JWK {
        return encryptedKeyStore.getKey(keyReference)
    }
}