/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.did.sdk.crypto

import com.microsoft.walletlibrary.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.walletlibrary.did.sdk.util.Constants.SEED_BYTES
import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.jwk.JWK
import java.security.Key
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.PublicKey
import java.security.SecureRandom
import java.security.Signature
import javax.crypto.Cipher
import javax.crypto.Cipher.DECRYPT_MODE
import javax.crypto.Cipher.ENCRYPT_MODE
import javax.crypto.Mac
import javax.crypto.SecretKey

internal object CryptoOperations {

    fun sign(digest: String, signingKey: JWK, alg: String, keyId: String): String {
        val token = JwsToken(digest.toString(), JWSAlgorithm(alg))
        // adding kid value to header.
        val header = JWSHeader.Builder(JWSAlgorithm(alg))
            .type(JOSEObjectType.JWT)
            .keyID(keyId)
            .build()
        token.sign(signingKey, header)
        return token.serialize()
    }

    fun verify(digest: ByteArray, signature: ByteArray, publicKey: PublicKey, alg: SigningAlgorithm): Boolean {
        val verifier = if (alg.provider == null) Signature.getInstance(alg.name) else Signature.getInstance(alg.name, alg.provider)
        verifier.apply {
            initVerify(publicKey)
            update(digest)
            if (alg.spec != null) setParameter(alg.spec)
        }
        return verifier.verify(signature)
    }

    fun digest(preImage: ByteArray, alg: DigestAlgorithm): ByteArray {
        val messageDigest =
            if (alg.provider == null) MessageDigest.getInstance(alg.name) else MessageDigest.getInstance(alg.name, alg.provider)
        return messageDigest.digest(preImage)
    }

    fun encrypt(plainText: ByteArray, key: SecretKey, alg: CipherAlgorithm): ByteArray {
        val cipher = if (alg.provider == null) Cipher.getInstance(alg.name) else Cipher.getInstance(alg.name, alg.provider)
        cipher.init(ENCRYPT_MODE, key)
        return cipher.doFinal(plainText)
    }

    fun decrypt(cipherText: ByteArray, key: SecretKey, alg: CipherAlgorithm): ByteArray {
        val cipher = if (alg.provider == null) Cipher.getInstance(alg.name) else Cipher.getInstance(alg.name, alg.provider)
        cipher.init(DECRYPT_MODE, key)
        return cipher.doFinal(cipherText)
    }

    fun computeMac(payload: ByteArray, key: SecretKey, alg: MacAlgorithm): ByteArray {
        val mac = if (alg.provider == null) Mac.getInstance(alg.name) else Mac.getInstance(alg.name, alg.provider)
        mac.init(key)
        return mac.doFinal(payload)
    }

    fun generateKeyPair(alg: KeyGenAlgorithm): KeyPair {
        val keyGen =
            if (alg.provider == null) KeyPairGenerator.getInstance(alg.name) else KeyPairGenerator.getInstance(alg.name, alg.provider)
        keyGen.initialize(alg.spec)
        return keyGen.genKeyPair()
    }

    inline fun <reified T : Key> generateKey(alg: PrivateKeyFactoryAlgorithm): T {
        val factory = if (alg.provider == null) KeyFactory.getInstance(alg.name) else KeyFactory.getInstance(alg.name, alg.provider)
        return factory.generatePrivate(alg.keySpec) as T
    }

    inline fun <reified T : Key> generateKey(alg: PublicKeyFactoryAlgorithm): T {
        val factory = if (alg.provider == null) KeyFactory.getInstance(alg.name) else KeyFactory.getInstance(alg.name, alg.provider)
        return factory.generatePublic(alg.keySpec) as T
    }

    fun generateSeed(): ByteArray {
        val secureRandom = SecureRandom()
        return secureRandom.generateSeed(SEED_BYTES)
    }
}