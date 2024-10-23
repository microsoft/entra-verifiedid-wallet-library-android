// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.did.sdk.crypto.protocols.jose.jws

import androidx.test.platform.app.InstrumentationRegistry
import com.microsoft.walletlibrary.did.sdk.crypto.CryptoOperations
import com.microsoft.walletlibrary.did.sdk.crypto.KeyGenAlgorithm
import com.microsoft.walletlibrary.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.walletlibrary.did.sdk.crypto.keyStore.toPrivateJwk
import com.microsoft.walletlibrary.identifier.EncryptedSharedPreferencesIdentifier
import com.nimbusds.jose.JWSObject
import com.nimbusds.jose.jwk.KeyUse
import org.assertj.core.api.Assertions
import org.junit.Test

class JwsTokenTest {

    private val keyStore = EncryptedKeyStore(InstrumentationRegistry.getInstrumentation().targetContext)

    @Test
    fun signToken_passHolderIdentifier_SignsToken() {
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
        val testDataString = "{\"iss\":\"joe\",\n" +
            " \"exp\":1300819380,\n" +
            " \"http://example.com/is_root\":true}"
        val jwsToken = JwsToken(JWSObject.parse(testDataString))

        // Act
        jwsToken.sign(encryptedSharedPreferencesIdentifier)

        // Assert
        val publicKey = keyStore.getKey("keyReferenceTest1").toPublicJWK()
        Assertions.assertThat(publicKey).isNotNull
        Assertions.assertThat(jwsToken.verify(listOf(publicKey))).isTrue
    }
}