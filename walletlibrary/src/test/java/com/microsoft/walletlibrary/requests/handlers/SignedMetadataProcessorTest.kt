package com.microsoft.walletlibrary.requests.handlers

import com.microsoft.walletlibrary.did.sdk.LinkedDomainsService
import com.microsoft.walletlibrary.did.sdk.MockDidMetadata
import com.microsoft.walletlibrary.did.sdk.MockInjectedRootOfTrustResolver
import com.microsoft.walletlibrary.did.sdk.VerifiableCredentialSdk
import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainVerified
import com.microsoft.walletlibrary.did.sdk.credential.service.validators.JwtDomainLinkageCredentialValidator
import com.microsoft.walletlibrary.did.sdk.credential.service.validators.JwtValidator
import com.microsoft.walletlibrary.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.walletlibrary.did.sdk.identifier.models.identifierdocument.DidMetadata
import com.microsoft.walletlibrary.did.sdk.identifier.models.identifierdocument.IdentifierDocument
import com.microsoft.walletlibrary.did.sdk.identifier.resolvers.Resolver
import com.microsoft.walletlibrary.mappings.getJwk
import com.microsoft.walletlibrary.networking.entities.openid4vci.credentialmetadata.SignedMetadataTokenClaims
import com.microsoft.walletlibrary.requests.RootOfTrust
import com.microsoft.walletlibrary.util.IdentifierDocumentResolutionException
import com.microsoft.walletlibrary.util.LibraryConfiguration
import com.microsoft.walletlibrary.util.OpenId4VciValidationException
import com.microsoft.walletlibrary.util.TokenValidationException
import com.microsoft.walletlibrary.util.VerifiedIdExceptions
import com.microsoft.walletlibrary.util.defaultTestSerializer
import com.microsoft.walletlibrary.wrapper.IdentifierDocumentResolver
import com.microsoft.walletlibrary.wrapper.LinkedDomainsResolver
import com.nimbusds.jose.jwk.JWK
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationException
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SignedMetadataProcessorTest {
    private val mockLibraryConfiguration: LibraryConfiguration = mockk()
    private val signedMetadataProcessor = spyk(SignedMetadataProcessor(mockLibraryConfiguration))
    private val signedMetadataString = "testSignedMetadata"
    private val credentialIssuer = "https://validdomain/credential-issuer"
    private val mockIdentifierDocument = mockk<IdentifierDocument>()
    private val mockJwsToken: JwsToken = mockk()
    private val mockJwk = mockk<JWK>()
    private val mockLinkedDomainsService: LinkedDomainsService = mockk()

    init {
        mockkStatic(VerifiableCredentialSdk::class)
        mockkStatic(IdentifierDocumentResolver::class)
        mockkStatic(LinkedDomainsResolver::class)
        mockkStatic("com.microsoft.walletlibrary.mappings.IdentifierDocumentMappingKt")
        mockkStatic("com.microsoft.walletlibrary.mappings.LinkedDomainMappingKt")
        every { VerifiableCredentialSdk.linkedDomainsService } returns mockLinkedDomainsService
        every { mockLibraryConfiguration.serializer } returns defaultTestSerializer
        every { signedMetadataProcessor["deserializeSignedMetadata"](signedMetadataString) } returns mockJwsToken
    }

    @Test
    fun process_KeyIdMissing_ThrowsException() {
        // Arrange
        mockJwsToken(null)

        runBlocking {
            // Act
            val actualResult = runCatching {
                signedMetadataProcessor.process(signedMetadataString, credentialIssuer)
            }

            // Assert
            assertThat(actualResult.isFailure).isTrue
            val actualException = actualResult.exceptionOrNull()
            assertThat(actualException).isInstanceOf(OpenId4VciValidationException::class.java)
            assertThat(actualException?.message).isEqualTo("JWS contains no key id")
            assertThat((actualException as OpenId4VciValidationException).code).isEqualTo(
                VerifiedIdExceptions.MALFORMED_SIGNED_METADATA_EXCEPTION.value
            )
        }
    }

    @Test
    fun process_DidMissingInKid_ThrowsException() {
        // Arrange
        mockJwsToken("#signingKey-1")

        runBlocking {
            // Act
            val actualResult = runCatching {
                signedMetadataProcessor.process(signedMetadataString, credentialIssuer)
            }

            // Assert
            assertThat(actualResult.isFailure).isTrue
            val actualException = actualResult.exceptionOrNull()
            assertThat(actualException).isInstanceOf(OpenId4VciValidationException::class.java)
            assertThat(actualException?.message).isEqualTo("JWS contains no DID")
            assertThat((actualException as OpenId4VciValidationException).code).isEqualTo(
                VerifiedIdExceptions.MALFORMED_SIGNED_METADATA_EXCEPTION.value
            )
        }
    }

    @Test
    fun process_FailResolvingDocument_ThrowsException() {
        // Arrange
        mockJwsToken("did:web:test#signingKey-1")
        coEvery { IdentifierDocumentResolver.resolveIdentifierDocument("did:web:test") } throws IdentifierDocumentResolutionException(
            "Unable to fetch identifier document"
        )

        runBlocking {
            // Act
            val actualResult = runCatching {
                signedMetadataProcessor.process(signedMetadataString, credentialIssuer)
            }

            // Assert
            assertThat(actualResult.isFailure).isTrue
            val actualException = actualResult.exceptionOrNull()
            assertThat(actualException).isInstanceOf(IdentifierDocumentResolutionException::class.java)
            assertThat(actualException?.message).isEqualTo("Unable to fetch identifier document")
        }
    }

    @Test
    fun process_FailGetJwkFromDocument_ThrowsException() {
        // Arrange
        mockIdentifierDocument(null)
        mockJwsToken("did:web:test#signingKey-1")
        coEvery { IdentifierDocumentResolver.resolveIdentifierDocument("did:web:test") } returns mockIdentifierDocument

        runBlocking {
            // Act
            val actualResult = runCatching {
                signedMetadataProcessor.process(signedMetadataString, credentialIssuer)
            }

            // Assert
            assertThat(actualResult.isFailure).isTrue
            val actualException = actualResult.exceptionOrNull()
            assertThat(actualException).isInstanceOf(OpenId4VciValidationException::class.java)
            assertThat(actualException?.message).isEqualTo("JWK with key id signingKey-1 not found in identifier document")
            assertThat((actualException as OpenId4VciValidationException).code).isEqualTo(
                VerifiedIdExceptions.MALFORMED_SIGNED_METADATA_EXCEPTION.value
            )
        }
    }

    @Test
    fun process_FailSignatureVerification_ThrowsException() {
        // Arrange
        mockIdentifierDocument()
        mockJwsToken("did:web:test#signingKey-1", passSignatureVerification = false)
        coEvery { IdentifierDocumentResolver.resolveIdentifierDocument("did:web:test") } returns mockIdentifierDocument

        runBlocking {
            // Act
            val actualResult = runCatching {
                signedMetadataProcessor.process(signedMetadataString, credentialIssuer)
            }

            // Assert
            assertThat(actualResult.isFailure).isTrue
            val actualException = actualResult.exceptionOrNull()
            assertThat(actualException).isInstanceOf(OpenId4VciValidationException::class.java)
            assertThat(actualException?.message).isEqualTo("Invalid signed metadata")
            assertThat((actualException as OpenId4VciValidationException).code).isEqualTo(
                VerifiedIdExceptions.MALFORMED_SIGNED_METADATA_EXCEPTION.value
            )
            val actualInnerException = actualException.innerError
            assertThat(actualInnerException).isInstanceOf(TokenValidationException::class.java)
            assertThat(actualInnerException?.message).isEqualTo("Signature is invalid on Signed metadata")
            assertThat((actualInnerException as TokenValidationException).code).isEqualTo(
                VerifiedIdExceptions.INVALID_SIGNATURE_EXCEPTION.value
            )
        }
    }

    @Test
    fun process_FailSignedMetadataTokenDeserialization_ThrowsException() {
        // Arrange
        mockIdentifierDocument()
        val mockJwsTokenContent = "testContent"
        mockJwsToken("did:web:test#signingKey-1", mockJwsTokenContent)
        coEvery { IdentifierDocumentResolver.resolveIdentifierDocument("did:web:test") } returns mockIdentifierDocument
        every {
            mockLibraryConfiguration.serializer.decodeFromString(
                SignedMetadataTokenClaims.serializer(),
                mockJwsTokenContent
            )
        } throws SerializationException("Mock SerializationException")

        runBlocking {
            // Act
            val actualResult = runCatching {
                signedMetadataProcessor.process(signedMetadataString, credentialIssuer)
            }

            // Assert
            assertThat(actualResult.isFailure).isTrue
            val actualException = actualResult.exceptionOrNull()
            assertThat(actualException).isInstanceOf(OpenId4VciValidationException::class.java)
            assertThat(actualException?.message).isEqualTo("Invalid signed metadata")
            assertThat((actualException as OpenId4VciValidationException).code).isEqualTo(
                VerifiedIdExceptions.MALFORMED_SIGNED_METADATA_EXCEPTION.value
            )
            val actualInnerException = actualException.innerError
            assertThat(actualInnerException).isInstanceOf(SerializationException::class.java)
            assertThat(actualInnerException?.message).isEqualTo("Mock SerializationException")
        }
    }

    @Test
    fun process_FailSignedMetadataTokenValidation_ThrowsException() {
        // Arrange
        mockIdentifierDocument()
        val signedMetadataTokenClaimsString =
            """{"sub":"","iss": "did:web:testissuer","iat": 1707859806}""".trimIndent()
        mockJwsToken("did:web:test#signingKey-1", signedMetadataTokenClaimsString)
        coEvery { IdentifierDocumentResolver.resolveIdentifierDocument("did:web:test") } returns mockIdentifierDocument

        runBlocking {
            // Act
            val actualResult = runCatching {
                signedMetadataProcessor.process(signedMetadataString, credentialIssuer)
            }

            // Assert
            assertThat(actualResult.isFailure).isTrue
            val actualException = actualResult.exceptionOrNull()
            assertThat(actualException).isInstanceOf(OpenId4VciValidationException::class.java)
            assertThat(actualException?.message).isEqualTo("Invalid signed metadata")
            assertThat((actualException as OpenId4VciValidationException).code).isEqualTo(
                VerifiedIdExceptions.MALFORMED_SIGNED_METADATA_EXCEPTION.value
            )
            val actualInnerException = actualException.innerError
            assertThat(actualInnerException).isInstanceOf(TokenValidationException::class.java)
            assertThat(actualInnerException?.message).isEqualTo("Invalid subject property in signed metadata.")
            assertThat((actualInnerException as TokenValidationException).code).isEqualTo(
                VerifiedIdExceptions.INVALID_PROPERTY_EXCEPTION.value
            )
        }
    }

    @Test
    fun process_LinkedDomainsVerifiedByResolver_ReturnsVerifiedRootOfTrust() {
        // Arrange
        mockIdentifierDocument()
        mockkStatic(VerifiableCredentialSdk::class)
        mockkObject(LinkedDomainsResolver)
        val mockedResolver: Resolver = mockk()
        val mockedJwtValidator: JwtValidator = mockk()
        val mockRootOfTrustResolver = spyk(MockInjectedRootOfTrustResolver(), recordPrivateCalls = true)
        val mockedJwtDomainLinkageCredentialValidator =
            JwtDomainLinkageCredentialValidator(mockedJwtValidator, com.microsoft.walletlibrary.did.sdk.di.defaultTestSerializer)
        val linkedDomainsService =
            LinkedDomainsService(mockk(relaxed = true), mockedResolver, mockedJwtDomainLinkageCredentialValidator, mockRootOfTrustResolver)
        every { VerifiableCredentialSdk.linkedDomainsService } answers { linkedDomainsService }
        every { LinkedDomainsResolver["getLinkedDomainsService"]() } answers { linkedDomainsService }
        val signedMetadataTokenClaimsString =
            """{"sub":"$credentialIssuer","iss": "${MockDidMetadata.VALID_DOMAIN_DID.value}","iat": 1707859806}""".trimIndent()
        mockJwsToken("${MockDidMetadata.VALID_DOMAIN_DID.value}#signingKey-1", signedMetadataTokenClaimsString)
        coEvery { IdentifierDocumentResolver.resolveIdentifierDocument(MockDidMetadata.VALID_DOMAIN_DID.value) } returns mockIdentifierDocument
        every { (mockIdentifierDocument as DidMetadata).id } returns MockDidMetadata.VALID_DOMAIN_DID.value

        runBlocking {
            // Act
            val actualResult = signedMetadataProcessor.process(signedMetadataString, credentialIssuer)

            // Assert
            assertThat(actualResult).isInstanceOf(RootOfTrust::class.java)
            assertThat(actualResult.source).isEqualTo("validDomain")
            assertThat(actualResult.verified).isTrue
            coVerify { mockRootOfTrustResolver.resolve(any<DidMetadata>()) }
        }
    }

    @Test
    fun process_LinkedDomainsNotVerifiedByResolverUsingWellKnownAndPasses_ReturnsVerifiedRootOfTrust() {
        // Arrange
        mockIdentifierDocument()
        mockkStatic(VerifiableCredentialSdk::class)
        mockkObject(LinkedDomainsResolver)
        val mockedResolver: Resolver = mockk()
        val mockedJwtValidator: JwtValidator = mockk()
        val mockRootOfTrustResolver = spyk(MockInjectedRootOfTrustResolver(), recordPrivateCalls = true)
        val mockedJwtDomainLinkageCredentialValidator =
            JwtDomainLinkageCredentialValidator(mockedJwtValidator, com.microsoft.walletlibrary.did.sdk.di.defaultTestSerializer)
        val linkedDomainsService = spyk(
            LinkedDomainsService(
                mockk(relaxed = true),
                mockedResolver,
                mockedJwtDomainLinkageCredentialValidator,
                mockRootOfTrustResolver
            ), recordPrivateCalls = true
        )
        every { VerifiableCredentialSdk.linkedDomainsService } answers { linkedDomainsService }
        every { LinkedDomainsResolver["getLinkedDomainsService"]() } answers { linkedDomainsService }
        val signedMetadataTokenClaimsString =
            """{"sub":"$credentialIssuer","iss": "${MockDidMetadata.VALID_DOMAIN_DID.value}","iat": 1707859806}""".trimIndent()
        mockJwsToken("${MockDidMetadata.VALID_DOMAIN_DID.value}#signingKey-1", signedMetadataTokenClaimsString)
        coEvery { IdentifierDocumentResolver.resolveIdentifierDocument(MockDidMetadata.VALID_DOMAIN_DID.value) } returns mockIdentifierDocument
        every { (mockIdentifierDocument as DidMetadata).id } returns MockDidMetadata.EMPTY_DOMAIN_DID.value
        coEvery { linkedDomainsService["verifyLinkedDomainsUsingWellKnownDocument"](mockIdentifierDocument) } returns LinkedDomainVerified(
            "validdomain"
        )

        runBlocking {
            // Act
            val actualResult = signedMetadataProcessor.process(signedMetadataString, credentialIssuer)

            // Assert
            assertThat(actualResult).isInstanceOf(RootOfTrust::class.java)
            assertThat(actualResult.source).isEqualTo("validdomain")
            assertThat(actualResult.verified).isTrue
            coVerify { mockRootOfTrustResolver.resolve(any<DidMetadata>()) }
            verify { linkedDomainsService["verifyLinkedDomainsUsingWellKnownDocument"](any<IdentifierDocument>()) }
        }
    }

    @Test
    fun process_LinkedDomainsNotVerifiedByResolverUsingWellKnownAndFails_ThrowsException() {
        // Arrange
        mockIdentifierDocument()
        mockkStatic(VerifiableCredentialSdk::class)
        mockkObject(LinkedDomainsResolver)
        val mockedResolver: Resolver = mockk()
        val mockedJwtValidator: JwtValidator = mockk()
        val mockRootOfTrustResolver = spyk(MockInjectedRootOfTrustResolver(), recordPrivateCalls = true)
        val mockedJwtDomainLinkageCredentialValidator =
            JwtDomainLinkageCredentialValidator(mockedJwtValidator, com.microsoft.walletlibrary.did.sdk.di.defaultTestSerializer)
        val linkedDomainsService = spyk(
            LinkedDomainsService(
                mockk(relaxed = true),
                mockedResolver,
                mockedJwtDomainLinkageCredentialValidator,
                mockRootOfTrustResolver
            ), recordPrivateCalls = true
        )
        every { VerifiableCredentialSdk.linkedDomainsService } answers { linkedDomainsService }
        every { LinkedDomainsResolver["getLinkedDomainsService"]() } answers { linkedDomainsService }
        val signedMetadataTokenClaimsString =
            """{"sub":"$credentialIssuer","iss": "${MockDidMetadata.VALID_DOMAIN_DID.value}","iat": 1707859806}""".trimIndent()
        mockJwsToken("${MockDidMetadata.VALID_DOMAIN_DID.value}#signingKey-1", signedMetadataTokenClaimsString)
        coEvery { IdentifierDocumentResolver.resolveIdentifierDocument(MockDidMetadata.VALID_DOMAIN_DID.value) } returns mockIdentifierDocument
        every { (mockIdentifierDocument as DidMetadata).id } returns MockDidMetadata.EMPTY_DOMAIN_DID.value

        runBlocking {
            // Act
            val actualResult = runCatching {
                signedMetadataProcessor.process(signedMetadataString, credentialIssuer)
            }

            // Assert
            assertThat(actualResult.isFailure).isTrue
            val actualException = actualResult.exceptionOrNull()
            assertThat(actualException).isInstanceOf(OpenId4VciValidationException::class.java)
            assertThat(actualException?.message).isEqualTo(
                "Signed metadata DID is not linked to the credential issuer origin"
            )
            coVerify { mockRootOfTrustResolver.resolve(any<DidMetadata>()) }
            verify { linkedDomainsService["verifyLinkedDomainsUsingWellKnownDocument"](any<IdentifierDocument>()) }
        }
    }

    @Test
    fun process_LinkedDomainDoesNotMatchCredentialIssuerOrigin_ThrowsException() {
        // Arrange
        mockIdentifierDocument()
        val issuerDid = "did:web:attacker.example"
        val signedMetadataTokenClaimsString =
            """{"sub":"https://issuer.example/credential-issuer","iss":"$issuerDid","iat":1707859806}"""
        mockJwsToken("$issuerDid#signingKey-1", signedMetadataTokenClaimsString)
        coEvery { IdentifierDocumentResolver.resolveIdentifierDocument(issuerDid) } returns mockIdentifierDocument
        coEvery {
            LinkedDomainsResolver.resolve(mockIdentifierDocument)
        } returns RootOfTrust("attacker.example", true)

        runBlocking {
            // Act
            val actualResult = runCatching {
                signedMetadataProcessor.process(
                    signedMetadataString,
                    "https://issuer.example/credential-issuer"
                )
            }

            // Assert
            assertThat(actualResult.isFailure).isTrue
            val actualException = actualResult.exceptionOrNull()
            assertThat(actualException).isInstanceOf(OpenId4VciValidationException::class.java)
            assertThat(actualException?.message).isEqualTo(
                "Signed metadata DID is not linked to the credential issuer origin"
            )
        }
    }

    @Test
    fun process_LinkedDomainMatchesCanonicalCredentialIssuerOrigin_ReturnsVerifiedRootOfTrust() {
        // Arrange
        mockIdentifierDocument()
        val issuerDid = "did:web:issuer.example"
        val credentialIssuer = "https://ISSUER.example:443/credential-issuer"
        val signedMetadataTokenClaimsString =
            """{"sub":"$credentialIssuer","iss":"$issuerDid","iat":1707859806}"""
        val expectedRootOfTrust = RootOfTrust("issuer.example", true)
        mockJwsToken("$issuerDid#signingKey-1", signedMetadataTokenClaimsString)
        coEvery { IdentifierDocumentResolver.resolveIdentifierDocument(issuerDid) } returns mockIdentifierDocument
        coEvery {
            LinkedDomainsResolver.resolve(mockIdentifierDocument)
        } returns expectedRootOfTrust

        runBlocking {
            // Act
            val actualResult = signedMetadataProcessor.process(signedMetadataString, credentialIssuer)

            // Assert
            assertThat(actualResult).isEqualTo(expectedRootOfTrust)
        }
    }

    private fun mockJwsToken(kid: String?, content: String? = null, passSignatureVerification: Boolean = true) {
        every { mockJwsToken.keyId } returns kid
        if (content != null) {
            every { mockJwsToken.content() } returns content
        }
        every { mockJwsToken.verify(listOf(mockJwk)) } returns passSignatureVerification
        kid?.let { mockJwk(it) }
    }

    private fun mockJwk(kid: String) {
        every { mockJwk.keyID } returns kid
    }

    private fun mockIdentifierDocument(jwk: JWK? = mockJwk) {
        val jwkToReturn = if (jwk == null) jwk else mockJwk
        every { mockIdentifierDocument.getJwk("signingKey-1") } returns jwkToReturn
    }
}