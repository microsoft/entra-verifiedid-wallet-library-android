// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.requests.handlers

import com.microsoft.walletlibrary.WalletLibraryFlight
import com.microsoft.walletlibrary.WalletLibraryFlightProvider
import com.microsoft.walletlibrary.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.walletlibrary.did.sdk.identifier.models.identifierdocument.IdentifierDocument
import com.microsoft.walletlibrary.did.sdk.identifier.models.identifierdocument.IdentifierDocumentPublicKey
import com.microsoft.walletlibrary.networking.entities.openid4vci.credentialmetadata.CredentialConfiguration
import com.microsoft.walletlibrary.networking.entities.openid4vci.credentialmetadata.CredentialMetadata
import com.microsoft.walletlibrary.networking.entities.openid4vci.credentialoffer.CredentialOffer
import com.microsoft.walletlibrary.requests.RootOfTrust
import com.microsoft.walletlibrary.requests.VerifiedIdRequest
import com.microsoft.walletlibrary.util.LibraryConfiguration
import com.microsoft.walletlibrary.util.PreviewFeatureFlags
import com.microsoft.walletlibrary.util.WalletLibraryLogger
import com.microsoft.walletlibrary.util.getResult
import com.microsoft.walletlibrary.wrapper.IdentifierDocumentResolver
import com.microsoft.walletlibrary.wrapper.LinkedDomainsResolver
import com.microsoft.walletlibrary.wrapper.VerifiedIdRequester
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.gen.ECKeyGenerator
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.spyk
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Test

class SignedMetadataHardeningTest {
    private val issuer = "https://issuer.example:8443/tenant"
    private val origin = "https://issuer.example:8443"
    private val did = "did:web:issuer.example"
    private val key = ECKeyGenerator(Curve.P_256).keyID("$did#key-1").generate()
    private var flightEnabled = true
    private val configuration = LibraryConfiguration(
        PreviewFeatureFlags(emptyList()),
        mockk(),
        Json { ignoreUnknownKeys = true; isLenient = true },
        logger = WalletLibraryLogger,
        identifierFactory = mockk(),
        flightProvider = WalletLibraryFlightProvider {
            it == WalletLibraryFlight.SignedMetadataValidation && flightEnabled
        }
    )
    private val processor = SignedMetadataProcessor(configuration)
    private val document = IdentifierDocument(
        listOf(IdentifierDocumentPublicKey("$did#key-1", "JsonWebKey2020", did, key.toPublicJWK())),
        did
    )
    private val handler = spyk(OpenId4VCIRequestHandler(configuration), recordPrivateCalls = true)
    private val consumedMetadata = slot<CredentialMetadata>()
    private val request = mockk<VerifiedIdRequest<*>>()
    private val offer = """
        {"credential_issuer":"$issuer","issuer_session":"session",
         "credential_configuration_ids":["id"],
         "grants":{"authorization_code":{"authorization_server":"https://auth.example/tenant"}}}
    """.trimIndent()

    init {
        mockkObject(IdentifierDocumentResolver, LinkedDomainsResolver, WalletLibraryLogger, VerifiedIdRequester)
        coEvery { IdentifierDocumentResolver.resolveIdentifierDocument(did) } returns document
        coEvery { LinkedDomainsResolver.resolve(document) } returns RootOfTrust("legacy", false)
        coEvery { LinkedDomainsResolver.resolveForOrigin(document, origin) } returns RootOfTrust(origin, true)
        justRun { WalletLibraryLogger.event(any(), any()) }
        coEvery {
            handler["transformToVerifiedIdRequest"](
                capture(consumedMetadata), any<CredentialConfiguration>(), any<CredentialOffer>(), any<RootOfTrust>()
            )
        } returns request
    }

    @After
    fun tearDown() {
        unmockkObject(IdentifierDocumentResolver, LinkedDomainsResolver, WalletLibraryLogger, VerifiedIdRequester)
    }

    @Test
    fun `real signature accepts authenticated delegated endpoints and preserves issuer port`() = runBlocking {
        val signed = token()
        assertThat(JwsToken.deserialize(signed).verify(listOf(key.toPublicJWK()))).isTrue
        val result = processor.processAuthenticatedMetadata(signed, issuer)
        assertThat(result.rootOfTrust).isEqualTo(RootOfTrust(origin, true))
        assertThat(result.metadata.credentialEndpoint).isEqualTo("https://credentials.example/issue")
        coVerify(exactly = 1) { LinkedDomainsResolver.resolveForOrigin(document, origin) }
        coVerify(exactly = 0) { LinkedDomainsResolver.resolve(any()) }
    }

    @Test
    fun `wrong signing key fails before domain lookup`() = runBlocking {
        val otherKey = ECKeyGenerator(Curve.P_256).generate()
        val forged = JwsToken(payload(), JWSHeader.Builder(JWSAlgorithm.ES256).keyID("$did#key-1").build())
        forged.sign(otherKey)
        assertThat(runCatching { processor.processAuthenticatedMetadata(forged.serialize(), issuer) }.isFailure).isTrue
        coVerify(exactly = 0) { LinkedDomainsResolver.resolveForOrigin(any(), any()) }
    }

    @Test
    fun `verified but different origin is rejected`() = runBlocking {
        coEvery { LinkedDomainsResolver.resolveForOrigin(document, origin) } returns
            RootOfTrust("https://issuer.example", true)
        assertReason(SignedMetadataValidation.Reason.UNVERIFIED_ISSUER_ORIGIN) {
            processor.processAuthenticatedMetadata(token(), issuer)
        }
    }

    @Test
    fun `unverified domain is rejected`() = runBlocking {
        coEvery { LinkedDomainsResolver.resolveForOrigin(document, origin) } returns RootOfTrust(origin, false)
        assertReason(SignedMetadataValidation.Reason.UNVERIFIED_ISSUER_ORIGIN) {
            processor.processAuthenticatedMetadata(token(), issuer)
        }
    }

    @Test
    fun `identity only token cannot authenticate unsigned endpoints`() = runBlocking {
        assertReason(SignedMetadataValidation.Reason.ISSUER_MISMATCH) {
            processor.processAuthenticatedMetadata(token("""{"iss":"$did","sub":"$issuer","iat":1}"""), issuer)
        }
    }

    @Test
    fun `missing signed endpoint is rejected`() = runBlocking {
        assertReason(SignedMetadataValidation.Reason.MISSING_SIGNED_FIELDS) {
            processor.processAuthenticatedMetadata(
                token(payload().replace("\"credential_endpoint\":\"https://credentials.example/issue\",", "")),
                issuer
            )
        }
    }

    @Test
    fun `invalid signed endpoint is rejected`() = runBlocking {
        listOf("http://credentials.example/issue", "https://user@credentials.example/issue", "https://credentials.example/#fragment")
            .forEach { endpoint ->
                assertReason(SignedMetadataValidation.Reason.INVALID_ENDPOINT) {
                    processor.processAuthenticatedMetadata(
                        token(payload().replace("https://credentials.example/issue", endpoint)), issuer
                    )
                }
            }
    }

    @Test
    fun `outer endpoint display and callback substitutions are never consumed`() = runBlocking {
        serve(token())
        assertThat(handler.handleRequest(offer)).isSameAs(request)
        assertThat(consumedMetadata.captured.credentialEndpoint).isEqualTo("https://credentials.example/issue")
        assertThat(consumedMetadata.captured.notificationEndpoint).isNull()
        assertThat(consumedMetadata.captured.display).isNull()
        coVerify(exactly = 0) { VerifiedIdRequester.sendIssuanceCallback(any(), any()) }
        verify {
            WalletLibraryLogger.event(
                "SignedMetadataValidation",
                mapOf("flight_enabled" to "true", "outcome" to "accepted", "reason" to "none")
            )
        }
    }

    @Test
    fun `flight off and runtime changes preserve legacy behavior`() = runBlocking<Unit> {
        serve(token())
        flightEnabled = false
        handler.handleRequest(offer)
        assertThat(consumedMetadata.captured.credentialEndpoint).isEqualTo("https://attacker.example/issue")
        flightEnabled = true
        handler.handleRequest(offer)
        assertThat(consumedMetadata.captured.credentialEndpoint).isEqualTo("https://credentials.example/issue")
        flightEnabled = false
        handler.handleRequest(offer)
        assertThat(consumedMetadata.captured.credentialEndpoint).isEqualTo("https://attacker.example/issue")
    }

    @Test
    fun `missing token fails without callback and preserves categorized public result`() = runBlocking {
        serve(null)
        val result = getResult { handler.handleRequest(offer) }
        assertThat(result.exceptionOrNull()).isInstanceOf(SignedMetadataValidationException::class.java)
        assertThat((result.exceptionOrNull() as SignedMetadataValidationException).reason)
            .isEqualTo(SignedMetadataValidation.Reason.MISSING_SIGNED_METADATA)
        assertThat(consumedMetadata.isCaptured).isFalse
        coVerify(exactly = 0) { VerifiedIdRequester.sendIssuanceCallback(any(), any()) }
        verify {
            WalletLibraryLogger.event(
                "SignedMetadataValidation",
                mapOf("flight_enabled" to "true", "outcome" to "rejected", "reason" to "missing_signed_metadata")
            )
        }
    }

    @Test
    fun `flight is sampled once even when rollout changes during metadata fetch`() = runBlocking<Unit> {
        val metadata = CredentialMetadata(credentialIssuer = issuer, signedMetadata = token())
        coEvery { handler["fetchCredentialMetadata"](issuer) } answers {
            flightEnabled = false
            Result.success(metadata)
        }
        handler.handleRequest(offer)
        assertThat(consumedMetadata.captured.credentialEndpoint).isEqualTo("https://credentials.example/issue")
    }

    @Test
    fun `no provider defaults new flight to off`() {
        val defaultConfiguration = LibraryConfiguration(
            PreviewFeatureFlags(emptyList()), mockk(), configuration.serializer,
            logger = WalletLibraryLogger, identifierFactory = mockk()
        )
        assertThat(defaultConfiguration.isFlightEnabled(WalletLibraryFlight.SignedMetadataValidation)).isFalse
    }

    @Test
    fun `outer issuer mismatch is rejected before signature processing or discovery`() = runBlocking<Unit> {
        serve(token(), "https://attacker.example")
        assertReason(SignedMetadataValidation.Reason.ISSUER_MISMATCH) { handler.handleRequest(offer) }
        coVerify(exactly = 0) { IdentifierDocumentResolver.resolveIdentifierDocument(any()) }
        assertThat(consumedMetadata.isCaptured).isFalse
    }

    @Test
    fun `malformed issuer rejected before metadata retrieval`() = runBlocking {
        listOf(
            "http://issuer.example", "https:issuer.example", "https:///issuer.example",
            "https://user@issuer.example", "https://issuer.example?query", "https://issuer.example#fragment"
        )
            .forEach { invalid ->
                assertReason(SignedMetadataValidation.Reason.INVALID_ISSUER) {
                    handler.handleRequest(offer.replace(issuer, invalid))
                }
            }
        coVerify(exactly = 0) { handler["fetchCredentialMetadata"](any<String>()) }
    }

    @Test
    fun `cancellation propagates and telemetry contains bounded values only`() = runBlocking {
        coEvery { handler["fetchCredentialMetadata"](issuer) } throws CancellationException("private details")
        assertThat(runCatching { handler.handleRequest(offer) }.exceptionOrNull())
            .isInstanceOf(CancellationException::class.java)
        verify {
            WalletLibraryLogger.event(
                "SignedMetadataValidation",
                mapOf("flight_enabled" to "true", "outcome" to "cancelled", "reason" to "cancelled")
            )
        }
    }

    private fun serve(signed: String?, outerIssuer: String = issuer) {
        val outer = CredentialMetadata(
            credentialIssuer = outerIssuer,
            credentialEndpoint = "https://attacker.example/issue",
            notificationEndpoint = "https://attacker.example/callback",
            authorizationServers = listOf("https://auth.example/tenant"),
            credentialConfigurationsSupported = mapOf("id" to CredentialConfiguration(scope = "scope")),
            signedMetadata = signed
        )
        coEvery { handler["fetchCredentialMetadata"](issuer) } returns Result.success(outer)
    }

    private fun payload(): String = """
        {"iss":"$did","sub":"$issuer","iat":1,
         "credential_issuer":"$issuer","credential_endpoint":"https://credentials.example/issue",
         "authorization_servers":["https://auth.example/tenant"],
         "credential_configurations_supported":{"id":{"scope":"scope"}}}
    """.trimIndent()

    private fun token(payload: String = payload()): String {
        val token = JwsToken(payload, JWSHeader.Builder(JWSAlgorithm.ES256).keyID("$did#key-1").build())
        token.sign(key)
        return token.serialize()
    }

    private suspend fun assertReason(reason: SignedMetadataValidation.Reason, block: suspend () -> Any) {
        val failure = runCatching { block() }.exceptionOrNull()
        assertThat(failure).isInstanceOf(SignedMetadataValidationException::class.java)
        assertThat((failure as SignedMetadataValidationException).reason).isEqualTo(reason)
    }
}
