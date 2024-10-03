package com.microsoft.walletlibrary.requests.handlers

import com.microsoft.walletlibrary.did.sdk.LinkedDomainsService
import com.microsoft.walletlibrary.did.sdk.VerifiableCredentialSdk
import com.microsoft.walletlibrary.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.walletlibrary.did.sdk.identifier.models.identifierdocument.IdentifierDocument
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
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationException
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SignedMetadataProcessorTest {
    private val mockLibraryConfiguration: LibraryConfiguration = mockk()
    private val signedMetadataProcessor = spyk(SignedMetadataProcessor(mockLibraryConfiguration))
    private val signedMetadataString = "testSignedMetadata"
    private val credentialIssuer = "testCredentialIssuer"
    private val mockIdentifierDocument = mockk<IdentifierDocument>()
    private val mockJwsToken: JwsToken = mockk()
    private val mockJwk = mockk<JWK>()
    private val mockLinedDomainsService: LinkedDomainsService = mockk()

    init {
        mockkStatic(VerifiableCredentialSdk::class)
        mockkStatic(IdentifierDocumentResolver::class)
        mockkStatic(LinkedDomainsResolver::class)
        mockkStatic("com.microsoft.walletlibrary.mappings.IdentifierDocumentMappingKt")
        mockkStatic("com.microsoft.walletlibrary.mappings.LinkedDomainMappingKt")
        mockkStatic("com.microsoft.walletlibrary.mappings.LinkedDomainsServiceExtensionKt")
        every { VerifiableCredentialSdk.linkedDomainsService } returns mockLinedDomainsService
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
    fun process_LinkedDomainsNotVerifiedByResolver_ReturnsUnverifiedRootOfTrust() {
        // Arrange
        mockIdentifierDocument()
        val signedMetadataTokenClaimsString =
            """{"sub":"testCredentialIssuer","iss": "did:web:test","iat": 1707859806}""".trimIndent()
        mockJwsToken("did:web:test#signingKey-1", signedMetadataTokenClaimsString)
        coEvery { IdentifierDocumentResolver.resolveIdentifierDocument("did:web:test") } returns mockIdentifierDocument
        coEvery { LinkedDomainsResolver.resolveRootOfTrust(mockIdentifierDocument) } returns RootOfTrust(
            "unverifiedDomain",
            false
        )

        runBlocking {
            // Act
            val actualResult =
                signedMetadataProcessor.process(signedMetadataString, credentialIssuer)

            // Assert
            assertThat(actualResult).isInstanceOf(RootOfTrust::class.java)
            assertThat(actualResult.source).isEqualTo("unverifiedDomain")
            assertThat(actualResult.verified).isFalse
        }
    }

    @Test
    fun process_LinkedDomainsVerifiedByResolver_ReturnsVerifiedRootOfTrust() {
        // Arrange
        mockIdentifierDocument()
        val signedMetadataTokenClaimsString =
            """{"sub":"testCredentialIssuer","iss": "did:web:test","iat": 1707859806}""".trimIndent()
        mockJwsToken("did:web:test#signingKey-1", signedMetadataTokenClaimsString)
        coEvery { IdentifierDocumentResolver.resolveIdentifierDocument("did:web:test") } returns mockIdentifierDocument
        coEvery { LinkedDomainsResolver.resolveRootOfTrust(mockIdentifierDocument) } returns RootOfTrust(
            "verifiedDomain",
            true
        )

        runBlocking {
            // Act
            val actualResult =
                signedMetadataProcessor.process(signedMetadataString, credentialIssuer)

            // Assert
            assertThat(actualResult).isInstanceOf(RootOfTrust::class.java)
            assertThat(actualResult.source).isEqualTo("verifiedDomain")
            assertThat(actualResult.verified).isTrue
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