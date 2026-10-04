package com.microsoft.walletlibrary.did.sdk.credential.service.validators

import com.microsoft.walletlibrary.WalletLibraryFlightProvider
import com.microsoft.walletlibrary.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.walletlibrary.did.sdk.identifier.models.identifierdocument.IdentifierDocument
import com.microsoft.walletlibrary.did.sdk.identifier.models.identifierdocument.IdentifierDocumentPublicKey
import com.microsoft.walletlibrary.did.sdk.identifier.resolvers.Resolver
import com.microsoft.walletlibrary.did.sdk.util.controlflow.ValidatorException
import com.microsoft.walletlibrary.util.CapturingWalletLibraryLogConsumer
import com.microsoft.walletlibrary.util.WalletLibraryLogger
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.KeyType
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
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
        validator = JwtValidator(mockedResolver, WalletLibraryFlightProvider { false })
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
        val logConsumer = CapturingWalletLibraryLogConsumer()
        WalletLibraryLogger.addConsumer(logConsumer)

        try {
            runBlocking {
                try {
                    validator.verifySignature(mockedJwsToken)
                    fail("Expected mismatched DID verification method to be rejected")
                } catch (exception: Exception) {
                    assertThat(exception).isInstanceOf(ValidatorException::class.java)
                    assertThat(exception.message).contains("No public key found in identifier document matching DID")
                }
            }
            assertThat(logConsumer.events).containsExactly(
                CapturingWalletLibraryLogConsumer.Event(
                    "DIDResolverHardeningCheck",
                    mapOf(
                        "check" to "verification_method_id",
                        "hardening_enabled" to "true",
                        "outcome" to "rejected"
                    )
                )
            )
        } finally {
            WalletLibraryLogger.CONSUMERS.remove(logConsumer)
        }
    }

    @Test
    fun `legacy resolver still rejects malformed DID in header`() {
        val malformedDid = "did:web:example.com:..:evil"
        val malformedKid = "$malformedDid#kidTest2353"
        val legacyValidator = JwtValidator(mockedResolver, WalletLibraryFlightProvider { true })
        every { mockedJwsToken.keyId } returns malformedKid

        val result = runBlocking {
            runCatching { legacyValidator.verifySignature(mockedJwsToken) }
        }

        assertThat(result.exceptionOrNull()).isInstanceOf(ValidatorException::class.java)
    }

    @Test
    fun `provider change affects next verification`() {
        var useLegacyDidResolver = false
        val dynamicValidator = JwtValidator(
            mockedResolver,
            WalletLibraryFlightProvider { useLegacyDidResolver }
        )
        coEvery { mockedResolver.resolve(expectedDid) } returns Result.success(mockedIdentifierDocument)
        every { mockedJwsToken.keyId } returns expectedKid
        every { mockedIdentifierDocumentPublicKey.id } returns "did:attacker:123#kidTest2353"
        every { mockedJwsToken.verify(listOf(mockedPublicKeyJwk)) } returns true
        val logConsumer = CapturingWalletLibraryLogConsumer()
        WalletLibraryLogger.addConsumer(logConsumer)

        try {
            val hardenedResult = runBlocking {
                runCatching { dynamicValidator.verifySignature(mockedJwsToken) }
            }

            useLegacyDidResolver = true
            val legacyResult = runBlocking {
                dynamicValidator.verifySignature(mockedJwsToken)
            }

            assertThat(hardenedResult.exceptionOrNull()).isInstanceOf(ValidatorException::class.java)
            assertTrue(legacyResult)
            assertThat(logConsumer.events).containsExactly(
                CapturingWalletLibraryLogConsumer.Event(
                    "DIDResolverHardeningCheck",
                    mapOf(
                        "check" to "verification_method_id",
                        "hardening_enabled" to "true",
                        "outcome" to "rejected"
                    )
                ),
                CapturingWalletLibraryLogConsumer.Event(
                    "DIDResolverHardeningCheck",
                    mapOf(
                        "check" to "verification_method_id",
                        "hardening_enabled" to "false",
                        "outcome" to "accepted"
                    )
                )
            )
        } finally {
            WalletLibraryLogger.CONSUMERS.remove(logConsumer)
        }
    }
}