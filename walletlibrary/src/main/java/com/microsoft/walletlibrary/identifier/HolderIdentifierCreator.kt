// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.identifier

import com.microsoft.walletlibrary.did.sdk.crypto.CryptoOperations
import com.microsoft.walletlibrary.did.sdk.crypto.KeyGenAlgorithm
import com.microsoft.walletlibrary.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.walletlibrary.did.sdk.crypto.keyStore.toPrivateJwk
import com.microsoft.walletlibrary.did.sdk.util.controlflow.KeyStoreException
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.KeyUse

internal class HolderIdentifierCreator(private val encryptedKeyStore: EncryptedKeyStore) {

    fun createHolderIdentifier(
        keyReference: String,
        algorithm: String,
        didMethod: String
    ): EncryptedSharedPreferencesIdentifier {
        val keyGenAlgorithm = mapJWAToKeyGenAlgorithm(algorithm)
        val signingPublicKeyJwk = fetchOrGenerateKey(keyReference, keyGenAlgorithm)
        val did = DidCreator.createDid(signingPublicKeyJwk, didMethod)
        return EncryptedSharedPreferencesIdentifier(
            did,
            algorithm,
            didMethod,
            signingPublicKeyJwk.keyID,
            encryptedKeyStore
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
    private fun generateAndStoreKeyPair(
        keyReference: String,
        keyGenAlgorithm: KeyGenAlgorithm = KeyGenAlgorithm.P256,
        use: KeyUse = KeyUse.SIGNATURE
    ): JWK {
        val privateKey =
            CryptoOperations.generateKeyPair(keyGenAlgorithm).toPrivateJwk(keyReference, use)
        encryptedKeyStore.storeKey(keyReference, privateKey)
        return privateKey.toPublicJWK()
    }

    /**
     * Fetches the key if it exists or generates a new KeyPair and stores it in the keyStore.
     *
     * @return returns the public Key in JWK format
     */
    private fun fetchOrGenerateKey(keyReference: String, keyGenAlgorithm: KeyGenAlgorithm): JWK {
        return try {
            encryptedKeyStore.getKey(keyReference)
        } catch (e: KeyStoreException) {
            generateAndStoreKeyPair(keyReference, keyGenAlgorithm)
        }
    }
}