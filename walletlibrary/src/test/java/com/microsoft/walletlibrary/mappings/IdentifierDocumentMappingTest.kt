package com.microsoft.walletlibrary.mappings

import com.microsoft.walletlibrary.did.sdk.identifier.models.identifierdocument.IdentifierDocument
import com.microsoft.walletlibrary.did.sdk.identifier.models.identifierdocument.IdentifierDocumentPublicKey
import com.nimbusds.jose.jwk.JWK
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class IdentifierDocumentMappingTest {
    private val mockIdentifierDocumentPublicKey: IdentifierDocumentPublicKey = mockk()
    private val mockPublicKeyJwk: JWK = mockk()
    private val mockIdentifierDocument: IdentifierDocument = mockk()

    @Test
    fun getJwkFromIdentifierDocument_KidHasMatchingKeyId_ReturnsJwk() {
        // Arrange
<<<<<<< HEAD
        val kid = "signingKey-1"
=======
        val kid = "#signingKey-1"
>>>>>>> logirvin/facecheck-v2
        every { mockIdentifierDocument.verificationMethod } returns listOf(mockIdentifierDocumentPublicKey)
        every { mockIdentifierDocumentPublicKey.id } returns "#signingKey-1"
        every { mockIdentifierDocumentPublicKey.publicKeyJwk } returns mockPublicKeyJwk

        // Act
        val actualResult = mockIdentifierDocument.getJwk(kid)

        //Assert
        assertThat(actualResult).isNotNull
        assertThat(actualResult).isEqualTo(mockPublicKeyJwk)
    }

    @Test
    fun getJwkFromIdentifierDocument_KidHasNoMatchingKeyId_ReturnsNull() {
        // Arrange
        val kid = "#nonMatchingKey-1"
        every { mockIdentifierDocument.verificationMethod } returns listOf(mockIdentifierDocumentPublicKey)
        every { mockIdentifierDocumentPublicKey.id } returns "#signingKey-1"
        every { mockIdentifierDocumentPublicKey.publicKeyJwk } returns mockPublicKeyJwk

        // Act
        val actualResult = mockIdentifierDocument.getJwk(kid)

        //Assert
        assertThat(actualResult).isNull()
    }

    @Test
    fun getJwkFromIdentifierDocument_DocumentHasEmptyKeyList_ReturnsNull() {
        // Arrange
        val kid = "#nonMatchingKey-1"
        every { mockIdentifierDocument.verificationMethod } returns emptyList()

        // Act
        val actualResult = mockIdentifierDocument.getJwk(kid)

        //Assert
        assertThat(actualResult).isNull()
    }

    @Test
    fun getJwkFromIdentifierDocument_DocumentHasNullVerificationMethod_ReturnsNull() {
        // Arrange
        val kid = "#nonMatchingKey-1"
        every { mockIdentifierDocument.verificationMethod } returns null

        // Act
        val actualResult = mockIdentifierDocument.getJwk(kid)

        //Assert
        assertThat(actualResult).isNull()
    }
}