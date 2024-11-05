package com.microsoft.walletlibrary.networking.formatters

import com.microsoft.walletlibrary.did.sdk.IdentifierService
import com.microsoft.walletlibrary.did.sdk.VerifiableCredentialSdk
import com.microsoft.walletlibrary.did.sdk.credential.service.protectors.JwsHeaderFormatter
import com.microsoft.walletlibrary.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.walletlibrary.did.sdk.datasource.network.apis.HttpAgentApiProvider
import com.microsoft.walletlibrary.did.sdk.identifier.models.Identifier
import com.microsoft.walletlibrary.did.sdk.util.controlflow.Result
import com.microsoft.walletlibrary.identifier.EncryptedSharedPreferencesIdentifier
import com.microsoft.walletlibrary.identifier.IdentifierFactory
import com.microsoft.walletlibrary.identifier.IdentifierManager
import com.microsoft.walletlibrary.networking.entities.openid4vci.credentialoffer.CredentialOffer
import com.microsoft.walletlibrary.networking.entities.openid4vci.request.OpenID4VCIJWTProofClaims
import com.microsoft.walletlibrary.networking.entities.openid4vci.request.RawOpenID4VCIRequest
import com.microsoft.walletlibrary.util.LibraryConfiguration
import com.microsoft.walletlibrary.util.OpenId4VciValidationException
import com.microsoft.walletlibrary.util.PreviewFeatureFlags
import com.microsoft.walletlibrary.util.VerifiedIdExceptions
import com.microsoft.walletlibrary.util.WalletLibraryLogger
import com.microsoft.walletlibrary.util.defaultTestSerializer
import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.jwk.JWK
import io.mockk.coEvery
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class OpenId4VciIssuanceRequestFormatterTest {
    private val slot = slot<String>()
    private val mockedKeyStore: EncryptedKeyStore = mockk()
    private val mockedHolderIdentifier: EncryptedSharedPreferencesIdentifier = mockk()
    private val mockedIdentifier: Identifier = mockk()
    private val mockedIdentifierManager: IdentifierManager = mockk()
    private val mockIdentifierService: IdentifierService = mockk()
    private val mockWalletLibraryLogger: WalletLibraryLogger = mockk()
    private val mockIdentifierFactory: IdentifierFactory = mockk()
    private val signingKeyRef: String = "sigKeyRef1243523"
    private val expectedDid: String = "did:test:2354543"
    private val expectedJsonWebKey: JWK = JWK.parse(
        "{\"kty\":\"EC\"," +
                "\"crv\":\"secp256k1\",\"x\":\"WfY7Px6AgH6x-_dgAoRbg8weYRJA36ON-gQiFnETrqw\"," +
                "\"y\":\"IzFx3BUGztK0cyDStiunXbrZYYTtKbOUzx16SUK0sAY\"}"
    )
    private val mockHttpAgentApiProvider: HttpAgentApiProvider = mockk()
    private val mockPreviewFeatureFlags: PreviewFeatureFlags = mockk()
    private var openId4VciIssuanceRequestFormatter: OpenId4VciIssuanceRequestFormatter

    init {
        val libraryConfiguration = LibraryConfiguration(
            mockPreviewFeatureFlags,
            mockHttpAgentApiProvider,
            defaultTestSerializer,
            rootOfTrustResolver = null,
            mockedIdentifierManager,
            mockWalletLibraryLogger,
            mockIdentifierFactory
        )
        openId4VciIssuanceRequestFormatter = spyk(
            OpenId4VciIssuanceRequestFormatter(libraryConfiguration),
            recordPrivateCalls = true
        )
        every { mockIdentifierFactory.getIdentifier() } returns mockedHolderIdentifier
        setUpGetPublicKey()
        every { mockedKeyStore.getKey(signingKeyRef) } returns expectedJsonWebKey
        every {
            mockedHolderIdentifier.sign(
                capture(slot)
            )
        } answers { slot.captured }
        mockkStatic(VerifiableCredentialSdk::class)
        every { VerifiableCredentialSdk.identifierService } returns mockIdentifierService
        coEvery { mockIdentifierService.getMasterIdentifier() } returns Result.Success(
            mockedIdentifier
        )
    }

    private fun setUpGetPublicKey() {
        every { mockIdentifierService.getKeyStore() } returns mockedKeyStore
        every { mockedIdentifier.id } returns expectedDid
        every { mockedIdentifier.signatureKeyReference } returns signingKeyRef
        every { mockedHolderIdentifier.id } returns expectedDid
        every { mockedHolderIdentifier.keyReference } returns signingKeyRef
        every { mockedHolderIdentifier.algorithm } returns "ES256K"
        mockkStatic(JwsHeaderFormatter::class)
        val jwsHeader = JWSHeader.Builder(JWSAlgorithm("ES256K"))
            .type(JOSEObjectType("openid4vci-proof+jwt"))
            .keyID("$expectedDid#$signingKeyRef")
            .build()
        justRun {
            openId4VciIssuanceRequestFormatter["formatJwsHeaderForIdentifier"](
                mockedHolderIdentifier
            )
        }
        every { mockedHolderIdentifier.jwsHeader } returns jwsHeader
    }

    @Test
    fun formatOpenId4VciIssuanceResponse_WithNoCredentials_ThrowsException() {
        // Arrange
        val mockCredentialOffer: CredentialOffer = mockk()
        every { mockCredentialOffer.credential_configuration_ids } returns emptyList()

        runBlocking {
            // Act
            val actual = runCatching {
                openId4VciIssuanceRequestFormatter.format(
                    mockCredentialOffer,
                    "",
                    ""
                )
            }

            // Assert
            assertThat(actual.isFailure).isTrue
            val actualException = actual.exceptionOrNull()
            assertThat(actualException).isInstanceOf(OpenId4VciValidationException::class.java)
            assertThat(actualException?.message).isEqualTo("Credential id is not present in the credential offer.")
            assertThat((actualException as OpenId4VciValidationException).code).isEqualTo(
                VerifiedIdExceptions.REQUEST_CREATION_EXCEPTION.value
            )
        }
    }

    @Test
    fun formatOpenId4VciIssuanceResponse_ValidInput_FormatsAndSignsContentSuccessfully() {
        // Arrange
        val expectedConfigurationId = "mockCredentialId1"
        val expectedIssuerSession = "mockIssuerSession"
        val expectedCredentialEndpoint = "mockCredentialEndpoint"
        val mockCredentialOffer: CredentialOffer = mockk()
        every { mockCredentialOffer.credential_configuration_ids } returns listOf(
            expectedConfigurationId
        )
        every { mockCredentialOffer.issuer_session } returns expectedIssuerSession

        runBlocking {
            // Act
            val actual = runCatching {
                openId4VciIssuanceRequestFormatter.format(
                    mockCredentialOffer,
                    expectedCredentialEndpoint,
                    "mockAccessToken"
                )
            }

            // Assert
            assertThat(actual.isSuccess).isTrue
            val actualRequest = actual.getOrThrow()
            assertThat(actualRequest).isInstanceOf(RawOpenID4VCIRequest::class.java)
            assertThat(actualRequest.credential_configuration_id).isEqualTo(expectedConfigurationId)
            assertThat(actualRequest.issuer_session).isEqualTo(expectedIssuerSession)
            val openID4VCIJWTProof = actualRequest.proof
            assertThat(openID4VCIJWTProof.proof_type).isEqualTo("jwt")
            val claims = defaultTestSerializer.decodeFromString(
                OpenID4VCIJWTProofClaims.serializer(),
                openID4VCIJWTProof.jwt
            )
            assertThat(claims.aud).isEqualTo(expectedCredentialEndpoint)
            assertThat(claims.sub).isEqualTo(expectedDid)
            assertThat(claims.at_hash).isEqualTo("3zhNfw-dNvvFu-TcG4v9wA")
        }
    }
}