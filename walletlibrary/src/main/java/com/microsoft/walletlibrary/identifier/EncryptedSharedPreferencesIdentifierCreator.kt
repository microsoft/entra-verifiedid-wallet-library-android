/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.identifier

import com.microsoft.walletlibrary.did.sdk.crypto.CryptoOperations
import com.microsoft.walletlibrary.did.sdk.crypto.KeyGenAlgorithm
import com.microsoft.walletlibrary.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.walletlibrary.did.sdk.crypto.keyStore.toPrivateJwk
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.KeyUse
import java.util.UUID

internal class EncryptedSharedPreferencesIdentifierCreator(private val encryptedKeyStore: EncryptedKeyStore) :
    HolderIdentifierCreator {

    /**
     * Creates a Holder Identifier based on the provided parameters.
     * @param algorithm The algorithm to use for cryptographic operations
     * @param didMethod The method for creating the DID (eg. did:jwk)
     * @param keyId The reference to the key in the keyStore
     * @return The Holder Identifier with the provided parameters.
     */
    override fun createHolderIdentifier(
        algorithm: String,
        didMethod: DidMethod,
        keyId: String?
    ): EncryptedSharedPreferencesIdentifier {
        val keyGenAlgorithm = JsonWebAlgorithm.values().find { it.name == algorithm }?.value
            ?: throw IllegalArgumentException("Unsupported algorithm")
        val signingPublicKeyJwk =
            keyId?.let { fetchKey(it).toPublicJWK() } ?: generateKeyPairAndStorePrivateKey(
                keyGenAlgorithm
            )
        val did = DidCreator.createDid(signingPublicKeyJwk, didMethod)
        // Key Reference is always 0 for did:jwk DIDs.
        return EncryptedSharedPreferencesIdentifier(
            did,
            algorithm,
            didMethod.value,
            "0",
            encryptedKeyStore,
            signingPublicKeyJwk.keyID
        )
    }

    /**
     * Generates a new KeyPair and stores it in the keyStore.
     *
     * @return returns the public Key in JWK format
     */
    private fun generateKeyPairAndStorePrivateKey(
        keyGenAlgorithm: KeyGenAlgorithm = KeyGenAlgorithm.P256,
        use: KeyUse = KeyUse.SIGNATURE
    ): JWK {
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