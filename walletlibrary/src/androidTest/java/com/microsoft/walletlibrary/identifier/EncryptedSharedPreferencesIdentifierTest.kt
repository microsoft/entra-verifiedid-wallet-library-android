// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.identifier

import androidx.test.platform.app.InstrumentationRegistry
import com.microsoft.walletlibrary.did.sdk.crypto.CryptoOperations
import com.microsoft.walletlibrary.did.sdk.crypto.KeyGenAlgorithm
import com.microsoft.walletlibrary.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.walletlibrary.did.sdk.crypto.keyStore.toPrivateJwk
import com.microsoft.walletlibrary.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.nimbusds.jose.JWSObject
import com.nimbusds.jose.jwk.KeyUse
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EncryptedSharedPreferencesIdentifierTest {

    private val keyStore = EncryptedKeyStore(InstrumentationRegistry.getInstrumentation().targetContext)

    @Test
    fun sign() {
        // Arrange
        val privateKey = CryptoOperations.generateKeyPair(KeyGenAlgorithm.Secp256k1).toPrivateJwk("keyReferenceTest1", KeyUse.SIGNATURE)
        keyStore.storeKey("keyReferenceTest1", privateKey)
        val encryptedSharedPreferencesIdentifier = EncryptedSharedPreferencesIdentifier(
            id = "id",
            algorithm = "ES256K",
            method = "method",
            keyReference = "keyReferenceTest1",
            cryptoOperations = CryptoOperations,
            keyStore = keyStore
        )
        val testData = "{\"iss\":\"joe\",\n" +
            " \"exp\":1300819380,\n" +
            " \"http://example.com/is_root\":true}"
        val publicKey = keyStore.getKey("keyReferenceTest1").toPublicJWK()

        // Act
        val signedData = encryptedSharedPreferencesIdentifier.sign(testData)

        // Assert
        val jwsObject = JWSObject.parse(signedData)
        val token = JwsToken(jwsObject)
        assertThat(publicKey).isNotNull
        assertThat(token.verify(listOf(publicKey))).isTrue
    }
}