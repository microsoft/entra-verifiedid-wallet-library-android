package com.microsoft.walletlibrary.did.sdk.credential.service.validators

import com.microsoft.walletlibrary.BooleanProvider
import com.microsoft.walletlibrary.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.walletlibrary.did.sdk.identifier.models.identifierdocument.IdentifierDocument
import com.microsoft.walletlibrary.did.sdk.identifier.models.identifierdocument.IdentifierDocumentPublicKey
import com.microsoft.walletlibrary.did.sdk.identifier.resolvers.Resolver
import com.microsoft.walletlibrary.did.sdk.util.controlflow.ValidatorException
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.KeyType
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail

class JwtValidatorTest {

    private val mockedIdentifierDocument: IdentifierDocument = mockk()

    private val mockedIdentifierDocumentPublicKey: IdentifierDocumentPublicKey = mockk()

    private val mockedJwsToken: JwsToken = mockk()

    private val mockedResolver: Resolver = mockk()

    private val mockedPublicKeyJwk: JWK = mockk()

    private val validator: JwtValidator

    private val expectedDid: String = "did:test:4235"
    private val expectedKid: String = "$expectedDid#kidTest2353"

    init {
        validator = JwtValidator(mockedResolver, BooleanProvider { true })
        setUpResolver()
        mockkObject(JwsToken)
    }

    private fun setUpResolver() {
        every { mockedIdentifierDocument.verificationMethod } returns listOf(mockedIdentifierDocumentPublicKey)
        every { mockedIdentifierDocumentPublicKey.publicKeyJwk } returns mockedPublicKeyJwk
    }

    @Test
    fun `valid signature is validated successfully`() {
        coEvery { mockedResolver.resolve(expectedDid) } returns Result.success(mockedIdentifierDocument)
        every { mockedJwsToken.verify(listOf(mockedPublicKeyJwk)) } returns true
        every { mockedJwsToken.keyId } returns expectedKid
        every { mockedIdentifierDocumentPublicKey.id } returns expectedKid
        every { mockedPublicKeyJwk.keyType } returns KeyType.EC
        runBlocking {
            val actualValidationResult = validator.verifySignature(mockedJwsToken)
            assertTrue(actualValidationResult)
        }
    }

    @Test
    fun `invalid signature fails successfully`() {
        coEvery { mockedResolver.resolve(expectedDid) } returns Result.success(mockedIdentifierDocument)
        every { mockedJwsToken.verify(listOf(mockedPublicKeyJwk)) } returns false
        every { mockedJwsToken.keyId } returns expectedKid
        every { mockedIdentifierDocumentPublicKey.id } returns expectedKid
        every { mockedPublicKeyJwk.keyType } returns KeyType.EC
        runBlocking {
            val actualValidationResult = validator.verifySignature(mockedJwsToken)
            assertFalse(actualValidationResult)
        }
    }

    @Test
    fun `throws when no key id specified`() {
        coEvery { mockedResolver.resolve(expectedDid) } returns Result.success(mockedIdentifierDocument)
        every { mockedJwsToken.verify(listOf(mockedPublicKeyJwk)) } returns true
        every { mockedJwsToken.keyId } returns null
        runBlocking {
            try {
                validator.verifySignature(mockedJwsToken)
                fail()
            } catch (exception: Exception) {
                assertThat(exception).isInstanceOf(ValidatorException::class.java)
            }
        }
    }

    @Test
    fun `throws when unable to resolve identifier document`() {
        val expectedException = ValidatorException("test")
        coEvery { mockedResolver.resolve(expectedDid) } returns Result.failure(expectedException)
        every { mockedJwsToken.verify(listOf(mockedPublicKeyJwk)) } returns true
        every { mockedJwsToken.keyId } returns expectedKid
        runBlocking {
            try {
                validator.verifySignature(mockedJwsToken)
                fail()
            } catch (exception: Exception) {
                assertThat(exception).isInstanceOf(ValidatorException::class.java)
            }
        }
    }

    @Test
    fun `rejects verification method when DID does not match requested DID`() {
        coEvery { mockedResolver.resolve(expectedDid) } returns Result.success(mockedIdentifierDocument)
        every { mockedJwsToken.verify(listOf(mockedPublicKeyJwk)) } returns true
        every { mockedJwsToken.keyId } returns expectedKid
        every { mockedIdentifierDocument.verificationMethod } returns listOf(mockedIdentifierDocumentPublicKey)
        every { mockedIdentifierDocumentPublicKey.id } returns "did:attacker:123#kidTest2353"
        every { mockedIdentifierDocumentPublicKey.publicKeyJwk } returns mockedPublicKeyJwk
        every { mockedPublicKeyJwk.keyType } returns KeyType.EC

        runBlocking {
            try {
                validator.verifySignature(mockedJwsToken)
                fail("Expected mismatched DID verification method to be rejected")
            } catch (exception: Exception) {
                assertThat(exception).isInstanceOf(ValidatorException::class.java)
                assertThat(exception.message).contains("No public key found in identifier document matching DID")
            }
        }
    }

    @Test
    fun `legacy resolver allows malformed DID in header and verification method`() {
        val malformedDid = "did:web:example.com:..:evil"
        val malformedKid = "$malformedDid#kidTest2353"
        val legacyValidator = JwtValidator(mockedResolver, BooleanProvider { false })
        coEvery { mockedResolver.resolve(malformedDid) } returns Result.success(mockedIdentifierDocument)
        every { mockedJwsToken.verify(listOf(mockedPublicKeyJwk)) } returns true
        every { mockedJwsToken.keyId } returns malformedKid
        every { mockedIdentifierDocumentPublicKey.id } returns malformedKid
        every { mockedPublicKeyJwk.keyType } returns KeyType.EC

        val actualValidationResult = runBlocking {
            legacyValidator.verifySignature(mockedJwsToken)
        }

        assertTrue(actualValidationResult)
    }

    @Test
    fun `legacy resolver preserves false result when no key id matches`() {
        val legacyValidator = JwtValidator(mockedResolver, BooleanProvider { false })
        coEvery { mockedResolver.resolve(expectedDid) } returns Result.success(mockedIdentifierDocument)
        every { mockedJwsToken.keyId } returns expectedKid
        every { mockedIdentifierDocumentPublicKey.id } returns "$expectedDid#different-key"
        every { mockedJwsToken.verify(emptyList()) } returns false

        val actualValidationResult = runBlocking {
            legacyValidator.verifySignature(mockedJwsToken)
        }

        assertFalse(actualValidationResult)
        verify { mockedJwsToken.verify(emptyList()) }
    }

    @Test
    fun `provider change affects next verification`() {
        var hardeningEnabled = true
        val dynamicValidator = JwtValidator(
            mockedResolver,
            BooleanProvider { hardeningEnabled }
        )
        val malformedDid = "did:web:example.com:..:evil"
        val malformedKid = "$malformedDid#kidTest2353"
        every { mockedJwsToken.keyId } returns malformedKid
        every { mockedIdentifierDocumentPublicKey.id } returns malformedKid

        val hardenedResult = runBlocking {
            runCatching { dynamicValidator.verifySignature(mockedJwsToken) }
        }

        hardeningEnabled = false
        coEvery { mockedResolver.resolve(malformedDid) } returns Result.success(mockedIdentifierDocument)
        every { mockedJwsToken.verify(listOf(mockedPublicKeyJwk)) } returns true
        val legacyResult = runBlocking {
            dynamicValidator.verifySignature(mockedJwsToken)
        }

        assertThat(hardenedResult.exceptionOrNull()).isInstanceOf(ValidatorException::class.java)
        assertTrue(legacyResult)
    }
}