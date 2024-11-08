// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.identifier

import androidx.test.platform.app.InstrumentationRegistry
import com.microsoft.walletlibrary.did.sdk.crypto.KeyGenAlgorithm
import com.microsoft.walletlibrary.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.KeyUse
import io.mockk.every
import io.mockk.spyk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import java.text.ParseException

class HolderIdentifierCreatorTest {
    private val keyStore =
        EncryptedKeyStore(InstrumentationRegistry.getInstrumentation().targetContext)

    @Test
    fun createHolderIdentifier_passES256AlgorithmAndDidJwkMethod_returnsHolderIdentifierWithValidValues() {
        // Arrange
        val holderIdentifierCreator =
            spyk(HolderIdentifierCreator(keyStore), recordPrivateCalls = true)
        val jwkString =
            """{"crv": "P-256","kty": "EC","x": "acbIQiuMs3i8_uszEjJ2tpTtRM4EU3yz91PH6CdH2V0","y": "_KcyLj9vWMptnmKtm46GqDz8wf74I5LKgrl2GzH3nSE"}"""
        val jwk = spyk(JWK.parse(jwkString), recordPrivateCalls = true)
        every {
            holderIdentifierCreator["generateAndStoreKeyPair"](
                any<String>(),
                any<KeyGenAlgorithm>(),
                any<KeyUse>()
            )
        } returns jwk
        every { jwk.keyID } returns "randomKeyID"

        // Act
        val actualEncryptedSharedPreferencesIdentifier =
            holderIdentifierCreator.createHolderIdentifier("main", "ES256", "did:jwk")

        // Assert
        assertThat(actualEncryptedSharedPreferencesIdentifier.algorithm).isEqualTo("ES256")
        assertThat(actualEncryptedSharedPreferencesIdentifier.method).isEqualTo("did:jwk")
        assertThat(actualEncryptedSharedPreferencesIdentifier.keyReference).isEqualTo("main")
        assertThat(actualEncryptedSharedPreferencesIdentifier.id).isEqualTo("did:jwk:eyJjcnYiOiJQLTI1NiIsImt0eSI6IkVDIiwieCI6ImFjYklRaXVNczNpOF91c3pFakoydHBUdFJNNEVVM3l6OTFQSDZDZEgyVjAiLCJ5IjoiX0tjeUxqOXZXTXB0bm1LdG00NkdxRHo4d2Y3NEk1TEtncmwyR3pIM25TRSJ9")
    }

    @Test
    fun createHolderIdentifier_useInvalidJwk_throwsException() {
        // Arrange
        val holderIdentifierCreator =
            spyk(HolderIdentifierCreator(keyStore), recordPrivateCalls = true)
        val jwkString =
            """{"crv": "P-256","kty": "EC","x": "acbIQiuMs3i8_uszEjJ2tpTtRM43yz91PH6CdH2V0","y": "_KcyLj9vWMptnmKtm46GqDz8wf74I5LKgrl2GzH3nSE"}"""

        assertThatThrownBy {
            val jwk = spyk(JWK.parse(jwkString), recordPrivateCalls = true)
            every {
                holderIdentifierCreator["generateAndStoreKeyPair"](
                    any<String>(),
                    any<KeyGenAlgorithm>(),
                    any<KeyUse>()
                )
            } returns jwk
            every { jwk.keyID } returns "randomKeyID"

            // Act & Assert
            holderIdentifierCreator.createHolderIdentifier("main", "ES256", "did:jwk")
        }
            .isInstanceOf(ParseException::class.java)
            .hasMessage("Invalid EC JWK: The 'x' and 'y' public coordinates are not on the P-256 curve")
    }
}