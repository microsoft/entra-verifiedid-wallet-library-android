// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.identifier

import android.util.Base64
import com.microsoft.walletlibrary.did.sdk.crypto.CryptoOperations
import com.microsoft.walletlibrary.did.sdk.crypto.KeyGenAlgorithm
import com.microsoft.walletlibrary.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.walletlibrary.did.sdk.crypto.keyStore.toPrivateJwk
import com.microsoft.walletlibrary.did.sdk.util.Constants
import com.microsoft.walletlibrary.util.HolderIdentifierCreationException
import com.microsoft.walletlibrary.util.VerifiedIdExceptions
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.KeyUse
import org.erdtman.jcs.JsonCanonicalizer

internal class HolderIdentifierCreator(private val encryptedKeyStore: EncryptedKeyStore) {

    fun createHolderIdentifier(
        keyReference: String,
        algorithm: String,
        didMethod: String
    ): EncryptedSharedPreferencesIdentifier {
        val keyGenAlgorithm = mapJWAToKeyGenAlgorithm(algorithm)
        val signingPublicKeyJwk = generateAndStoreKeyPair(keyReference, keyGenAlgorithm)
        val did = createDid(signingPublicKeyJwk, didMethod)
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

    private fun createDid(jwk: JWK, didMethod: String): String {
        if (didMethod != "did:jwk") {
            throw HolderIdentifierCreationException("Only did:jwk is supported", VerifiedIdExceptions.HOLDER_IDENTIFIER_EXCEPTION.value)
        }
        val utf8EncodedJwk = JsonCanonicalizer(jwk.toJSONString()).encodedUTF8
        return didMethod + ":" + Base64.encodeToString(utf8EncodedJwk, Constants.BASE64_URL_SAFE)
    }
}