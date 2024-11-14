/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.identifier

import com.nimbusds.jose.jwk.JWK
import io.mockk.every
import io.mockk.spyk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
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
            DidCreator.createDid(jwk, DidMethod.DID_JWK)

        // Assert
        assertThat(actualDid).isEqualTo("did:jwk:eyJjcnYiOiJQLTI1NiIsImt0eSI6IkVDIiwieCI6ImFjYklRaXVNczNpOF91c3pFakoydHBUdFJNNEVVM3l6OTFQSDZDZEgyVjAiLCJ5IjoiX0tjeUxqOXZXTXB0bm1LdG00NkdxRHo4d2Y3NEk1TEtncmwyR3pIM25TRSJ9")
    }

    @Test
    fun createHolderIdentifier_useInvalidJwk_throwsException() {
        // Arrange
        val jwkString =
            """{"crv": "P-256","kty": "EC","x": "acbIQiuMs3i8_uszEjJ2tpTtRM43yz91PH6CdH2V0","y": "_KcyLj9vWMptnmKtm46GqDz8wf74I5LKgrl2GzH3nSE"}"""

        assertThatThrownBy {
            val jwk = spyk(JWK.parse(jwkString), recordPrivateCalls = true)
            every { jwk.keyID } returns "randomKeyID"

            // Act & Assert
            DidCreator.createDid(jwk, DidMethod.DID_JWK)
        }
            .isInstanceOf(ParseException::class.java)
            .hasMessage("Invalid EC JWK: The 'x' and 'y' public coordinates are not on the P-256 curve")
    }
}