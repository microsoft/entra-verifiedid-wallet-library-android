// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.did.sdk.identifier.resolvers

import com.microsoft.walletlibrary.WalletLibraryFlightProvider
import com.microsoft.walletlibrary.did.sdk.datasource.repository.IdentifierRepository
import com.microsoft.walletlibrary.did.sdk.identifier.models.identifierdocument.IdentifierResponse
import com.microsoft.walletlibrary.did.sdk.util.controlflow.LocalNetworkException
import com.microsoft.walletlibrary.did.sdk.util.controlflow.ResolverException
import com.microsoft.walletlibrary.did.sdk.util.defaultTestSerializer
import com.microsoft.walletlibrary.util.NetworkingException
import com.microsoft.walletlibrary.util.VerifiedIdExceptions
import com.microsoft.walletlibrary.util.WalletLibraryEventRecorder
import com.microsoft.walletlibrary.util.WalletLibraryLogger
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import kotlin.Result as KotlinResult

class ResolverTest {
    private val identifierRepository: IdentifierRepository = mockk()
    private val expectedIdentifierDocumentString =
        """{"@context":"https://www.w3.org/ns/did-resolution/v1","didDocument":{"id":"did:ion:EiBGMAR43ZWai9JKvh31ShjvNaX0dYRYI0XtLnZIa88CqQ?-ion-initial-state=eyJkZWx0YV9oYXNoIjoiRWlCYllRMEViTmNtMnJkc2dBMndJM0FRQTRWRlFSUTVYSnk5QzFTOWV3elFBQSIsInJlY292ZXJ5X2tleSI6eyJrdHkiOiJFQyIsImNydiI6InNlY3AyNTZrMSIsIngiOiJNRGE4djdNNC1ETi1WeTZaWVlsUFluUHR3dFRNanZFOUgzLXdYWDJWSktjIiwieSI6IkJDcG1SNnBla0p2QnFFYnBINExwbnVMd1NKTEdkck5QcjNBaFdJR21iWDQifSwicmVjb3ZlcnlfY29tbWl0bWVudCI6IkVpREM3Y0J2Unp3US1CZkhSQ2x1dG5MQU8wUlZyS3V6c0pTSEFtXzhFTERocVEifQ.eyJ1cGRhdGVfY29tbWl0bWVudCI6IkVpQXNsRml6MWNRU3RranluUy03eERUbGtLOExvZ01FLWc2bkFBXy0wREVyWGciLCJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJWYThfc2lnbl9JT05fMSIsInR5cGUiOiJFY2RzYVNlY3AyNTZrMVZlcmlmaWNhdGlvbktleTIwMTkiLCJqd2siOnsia3R5IjoiRUMiLCJjcnYiOiJzZWNwMjU2azEiLCJ4IjoiaEEzRWFaanZnR3BpMUFuV21TRXpQaGhlSTU1TXpoZHdEVXRYY0FTMzZGayIsInkiOiJFNDRwSnA3X21kNlo2LXlEbmdPSzlMWWFXX0xUQzhCWGdTVk1RQ1plTnJBIn0sInVzYWdlIjpbIm9wcyIsImF1dGgiLCJnZW5lcmFsIl19XX19XX0","@context":["https://www.w3.org/ns/did/v1",{"@base":"did:ion:EiBGMAR43ZWai9JKvh31ShjvNaX0dYRYI0XtLnZIa88CqQ?-ion-initial-state=eyJkZWx0YV9oYXNoIjoiRWlCYllRMEViTmNtMnJkc2dBMndJM0FRQTRWRlFSUTVYSnk5QzFTOWV3elFBQSIsInJlY292ZXJ5X2tleSI6eyJrdHkiOiJFQyIsImNydiI6InNlY3AyNTZrMSIsIngiOiJNRGE4djdNNC1ETi1WeTZaWVlsUFluUHR3dFRNanZFOUgzLXdYWDJWSktjIiwieSI6IkJDcG1SNnBla0p2QnFFYnBINExwbnVMd1NKTEdkck5QcjNBaFdJR21iWDQifSwicmVjb3ZlcnlfY29tbWl0bWVudCI6IkVpREM3Y0J2Unp3US1CZkhSQ2x1dG5MQU8wUlZyS3V6c0pTSEFtXzhFTERocVEifQ.eyJ1cGRhdGVfY29tbWl0bWVudCI6IkVpQXNsRml6MWNRU3RranluUy03eERUbGtLOExvZ01FLWc2bkFBXy0wREVyWGciLCJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJWYThfc2lnbl9JT05fMSIsInR5cGUiOiJFY2RzYVNlY3AyNTZrMVZlcmlmaWNhdGlvbktleTIwMTkiLCJqd2siOnsia3R5IjoiRUMiLCJjcnYiOiJzZWNwMjU2azEiLCJ4IjoiaEEzRWFaanZnR3BpMUFuV21TRXpQaGhlSTU1TXpoZHdEVXRYY0FTMzZGayIsInkiOiJFNDRwSnA3X21kNlo2LXlEbmdPSzlMWWFXX0xUQzhCWGdTVk1RQ1plTnJBIn0sInVzYWdlIjpbIm9wcyIsImF1dGgiLCJnZW5lcmFsIl19XX19XX0"}],"verificationMethod":[{"id":"#Va8_sign_ION_1","controller":"","type":"EcdsaSecp256k1VerificationKey2019","publicKeyJwk":{"kty":"EC","crv":"secp256k1","x":"hA3EaZjvgGpi1AnWmSEzPhheI55MzhdwDUtXcAS36Fk","y":"E44pJp7_md6Z6-yDngOK9LYaW_LTC8BXgSVMQCZeNrA"}}],"authentication":["#Va8_sign_ION_1"]},"methodMetadata":{"operationPublicKeys":[{"id":"#Va8_sign_ION_1","controller":"","type":"EcdsaSecp256k1VerificationKey2019","publicKeyJwk":{"kty":"EC","crv":"secp256k1","x":"hA3EaZjvgGpi1AnWmSEzPhheI55MzhdwDUtXcAS36Fk","y":"E44pJp7_md6Z6-yDngOK9LYaW_LTC8BXgSVMQCZeNrA"}}],"recoveryKey":{"kty":"EC","crv":"secp256k1","x":"MDa8v7M4-DN-Vy6ZYYlPYnPtwtTMjvE9H3-wXX2VJKc","y":"BCpmR6pekJvBqEbpH4LpnuLwSJLGdrNPr3AhWIGmbX4"}},"resolverMetadata":{"driverId":"did:ion","driver":"HttpDriver","retrieved":"2020-05-31T09:20:41.162Z","duration":"118.4098ms"}}"""
    private val expectedIdentifierResponse =
        defaultTestSerializer.decodeFromString(IdentifierResponse.serializer(), expectedIdentifierDocumentString)
    private val expectedIdentifier = expectedIdentifierResponse.didDocument.id
    private val invalidIdentifier = "invalid-did"

    @Test
    fun successfulResolutionTest() {
        val resolver = Resolver("", identifierRepository, WalletLibraryFlightProvider { false })
        coEvery { identifierRepository.resolveIdentifier("", expectedIdentifier) } returns KotlinResult.success(expectedIdentifierResponse)
        runBlocking {
            val actualIdentifierDocument = resolver.resolve(expectedIdentifier)
            assertThat(actualIdentifierDocument.isSuccess).isEqualTo(true)
            assertThat(actualIdentifierDocument.getOrNull()?.id).isEqualTo(expectedIdentifier)
        }
    }

    @Test
    fun failedResolutionInvalidIdTest() {
        val resolver = Resolver("", identifierRepository, WalletLibraryFlightProvider { false })
        coEvery { identifierRepository.resolveIdentifier("", invalidIdentifier) } returns KotlinResult.failure(
            NetworkingException(
                "Not Found",
                VerifiedIdExceptions.NETWORKING_EXCEPTION.value,
                retryable = true
            )
        )
        runBlocking {
            val actualResult = resolver.resolve(invalidIdentifier)
            assertThat(actualResult.isFailure).isEqualTo(true)
            assertThat(actualResult.exceptionOrNull()).isInstanceOf(ResolverException::class.java)
            assertThat(actualResult.exceptionOrNull()?.cause).isInstanceOf(NetworkingException::class.java)
        }
    }

    @Test
    fun failedResolutionNetworkConnectionTest() {
        val resolver = Resolver("invalidUrl", identifierRepository, WalletLibraryFlightProvider { false })
        coEvery {
            identifierRepository.resolveIdentifier(
                "invalidUrl",
                expectedIdentifier
            )
        } returns KotlinResult.failure(LocalNetworkException("Failed to send request."))
        runBlocking {
            val actualResult = resolver.resolve(expectedIdentifier)
            assertThat(actualResult.isFailure).isEqualTo(true)
            assertThat(actualResult.exceptionOrNull()).isInstanceOf(ResolverException::class.java)
            assertThat(actualResult.exceptionOrNull()?.cause).isInstanceOf(LocalNetworkException::class.java)
        }
    }

    @Test
    fun failedResolutionMismatchedDocumentIdTest() {
        val resolver = Resolver("", identifierRepository, WalletLibraryFlightProvider { false })
        val mismatchedId = "did:ion:another-id"
        val mismatchedDocument = expectedIdentifierResponse.copy(didDocument = expectedIdentifierResponse.didDocument.copy(id = mismatchedId))
        coEvery { identifierRepository.resolveIdentifier("", expectedIdentifier) } returns KotlinResult.success(mismatchedDocument)
        val logConsumer = WalletLibraryEventRecorder()
        WalletLibraryLogger.addConsumer(logConsumer)

        try {
            runBlocking {
                val actualResult = resolver.resolve(expectedIdentifier)
                assertThat(actualResult.isFailure).isEqualTo(true)
                assertThat(actualResult.exceptionOrNull()).isInstanceOf(ResolverException::class.java)
                assertThat(actualResult.exceptionOrNull()?.cause?.message).contains("does not match requested identifier")
            }
            assertThat(logConsumer.events).containsExactly(
                WalletLibraryEventRecorder.Event(
                    "DIDResolverHardeningCheck",
                    mapOf(
                        "check" to "resolved_document_id",
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
    fun mismatchedDocumentIdReturnsFailureRatherThanThrowing() {
        val resolver = Resolver("", identifierRepository, WalletLibraryFlightProvider { false })
        val mismatchedDocument = expectedIdentifierResponse.copy(
            didDocument = expectedIdentifierResponse.didDocument.copy(id = "did:ion:another-id")
        )
        coEvery { identifierRepository.resolveIdentifier("", expectedIdentifier) } returns KotlinResult.success(mismatchedDocument)

        val actualResult = runBlocking { resolver.resolve(expectedIdentifier) }

        assertThat(actualResult.isFailure).isTrue()
        assertThat(actualResult.exceptionOrNull()).isInstanceOf(ResolverException::class.java)
    }

    @Test
    fun mismatchedDocumentIdIsAllowedWhenHardeningDisabled() {
        val resolver = Resolver("", identifierRepository, WalletLibraryFlightProvider { true })
        val mismatchedDocument = expectedIdentifierResponse.copy(
            didDocument = expectedIdentifierResponse.didDocument.copy(id = "did:ion:another-id")
        )
        coEvery { identifierRepository.resolveIdentifier("", expectedIdentifier) } returns KotlinResult.success(mismatchedDocument)
        val logConsumer = WalletLibraryEventRecorder()
        WalletLibraryLogger.addConsumer(logConsumer)

        try {
            val actualResult = runBlocking { resolver.resolve(expectedIdentifier) }

            assertThat(actualResult.isSuccess).isTrue()
            assertThat(actualResult.getOrNull()?.id).isEqualTo("did:ion:another-id")
            assertThat(logConsumer.events).containsExactly(
                WalletLibraryEventRecorder.Event(
                    "DIDResolverHardeningCheck",
                    mapOf(
                        "check" to "resolved_document_id",
                        "hardening_enabled" to "false",
                        "outcome" to "accepted"
                    )
                )
            )
        } finally {
            WalletLibraryLogger.CONSUMERS.remove(logConsumer)
        }
    }

    @Test
    fun legacyResolverProviderChangeAffectsNextResolution() {
        var useLegacyDidResolver = false
        var providerReads = 0
        val resolver = Resolver(
            "",
            identifierRepository,
            WalletLibraryFlightProvider {
                providerReads++
                useLegacyDidResolver
            }
        )
        val mismatchedDocument = expectedIdentifierResponse.copy(
            didDocument = expectedIdentifierResponse.didDocument.copy(id = "did:ion:another-id")
        )
        coEvery {
            identifierRepository.resolveIdentifier("", expectedIdentifier)
        } returns KotlinResult.success(mismatchedDocument)

        val hardenedResult = runBlocking { resolver.resolve(expectedIdentifier) }
        useLegacyDidResolver = true
        val legacyResult = runBlocking { resolver.resolve(expectedIdentifier) }

        assertThat(hardenedResult.isFailure).isTrue()
        assertThat(legacyResult.getOrNull()?.id).isEqualTo("did:ion:another-id")
        assertThat(providerReads).isEqualTo(2)
    }
}