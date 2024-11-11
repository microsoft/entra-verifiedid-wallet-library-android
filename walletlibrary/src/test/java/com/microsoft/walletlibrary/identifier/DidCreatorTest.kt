// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.identifier

import android.util.Base64
import com.microsoft.walletlibrary.did.sdk.util.Constants
import com.nimbusds.jose.jwk.JWK
import io.mockk.every
import io.mockk.spyk
import org.assertj.core.api.Assertions
import org.erdtman.jcs.JsonCanonicalizer
import org.junit.Test
import java.text.ParseException

class DidCreatorTest {

    @Test
    fun createDid_passP256KeysAndDidJwkMethod_returnsDid() {
        // Arrange
        val jwkString =
            """{"crv": "P-256","kty": "EC","x": "acbIQiuMs3i8_uszEjJ2tpTtRM4EU3yz91PH6CdH2V0","y": "_KcyLj9vWMptnmKtm46GqDz8wf74I5LKgrl2GzH3nSE"}"""
        val jwk = spyk(JWK.parse(jwkString), recordPrivateCalls = true)
        every { jwk.keyID } returns "randomKeyID"

        // Act
        val actualDid =
            DidCreator.createDid(jwk, "did:jwk")

        // Assert
        val didJwk = "did:jwk:eyJjcnYiOiJQLTI1NiIsImt0eSI6IkVDIiwieCI6ImFjYklRaXVNczNpOF91c3pFakoydHBUdFJNNEVVM3l6OTFQSDZDZEgyVjAiLCJ5IjoiX0tjeUxqOXZXTXB0bm1LdG00NkdxRHo4d2Y3NEk1TEtncmwyR3pIM25TRSJ9"
        Assertions.assertThat(actualDid).isEqualTo(didJwk)

        val didJwkString = didJwk.split("did:jwk:")[1]
        val utf8Encoded = Base64.decode(didJwkString, Constants.BASE64_URL_SAFE).decodeToString()
        Assertions.assertThat(utf8Encoded).contains("acbIQiuMs3i8_uszEjJ2tpTtRM4EU3yz91PH6CdH2V0")
    }

    @Test
    fun createHolderIdentifier_useInvalidJwk_throwsException() {
        // Arrange
        val jwkString =
            """{"crv": "P-256","kty": "EC","x": "acbIQiuMs3i8_uszEjJ2tpTtRM43yz91PH6CdH2V0","y": "_KcyLj9vWMptnmKtm46GqDz8wf74I5LKgrl2GzH3nSE"}"""

        Assertions.assertThatThrownBy {
            val jwk = spyk(JWK.parse(jwkString), recordPrivateCalls = true)
            every { jwk.keyID } returns "randomKeyID"

            // Act & Assert
            DidCreator.createDid(jwk, "did:jwk")
        }
            .isInstanceOf(ParseException::class.java)
            .hasMessage("Invalid EC JWK: The 'x' and 'y' public coordinates are not on the P-256 curve")
    }
}