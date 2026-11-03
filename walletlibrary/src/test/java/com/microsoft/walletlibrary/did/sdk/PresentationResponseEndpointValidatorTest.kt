package com.microsoft.walletlibrary.did.sdk

import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainMissing
import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainUnVerified
import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainVerified
import com.microsoft.walletlibrary.did.sdk.util.controlflow.PresentationException
import com.microsoft.walletlibrary.mappings.toLinkedDomainResult
import com.microsoft.walletlibrary.requests.RootOfTrust
import com.microsoft.walletlibrary.util.WalletLibraryEventRecorder
import com.microsoft.walletlibrary.util.WalletLibraryLogger
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.After
import org.junit.Before
import org.junit.Test

class PresentationResponseEndpointValidatorTest {
    private val eventRecorder = WalletLibraryEventRecorder()

    @Before
    fun setUp() {
        WalletLibraryLogger.addConsumer(eventRecorder)
    }

    @After
    fun tearDown() {
        WalletLibraryLogger.CONSUMERS.clear()
    }

    @Test
    fun validate_sameVerifiedOriginWithPathAndQuery_returnsEndpoint() {
        val endpoint = PresentationResponseEndpointValidator.validate(
            "https://VERIFIER.example:443/presentation/callback?request=123",
            LinkedDomainVerified("verifier.example", "https://verifier.example")
        )

        assertThat(endpoint).isEqualTo("https://verifier.example/presentation/callback?request=123")
    }

    @Test
    fun validate_matchingNonDefaultPort_returnsEndpoint() {
        val endpoint = PresentationResponseEndpointValidator.validate(
            "https://verifier.example:8443/callback",
            LinkedDomainVerified("verifier.example", "https://verifier.example:8443")
        )

        assertThat(endpoint).isEqualTo("https://verifier.example:8443/callback")
    }

    @Test
    fun validate_matchingInternationalizedDomain_returnsAsciiEndpoint() {
        val endpoint = PresentationResponseEndpointValidator.validate(
            "https://bücher.example/callback",
            LinkedDomainVerified("bücher.example", "https://bücher.example")
        )

        assertThat(endpoint).isEqualTo("https://xn--bcher-kva.example/callback")
    }

    @Test
    fun validate_matchingPublicIpv6_returnsEndpoint() {
        val endpoint = PresentationResponseEndpointValidator.validate(
            "https://[2606:4700:4700::1111]/callback",
            LinkedDomainVerified(
                "2606:4700:4700::1111",
                "https://[2606:4700:4700::1111]"
            )
        )

        assertThat(endpoint).isEqualTo("https://[2606:4700:4700::1111]/callback")
    }

    @Test
    fun validate_publicIpv4EmbeddedInNat64Address_returnsEndpoint() {
        val endpoint = PresentationResponseEndpointValidator.validate(
            "https://[64:ff9b::808:808]/callback",
            LinkedDomainVerified(
                "64:ff9b::808:808",
                "https://[64:ff9b::808:808]"
            )
        )

        assertThat(endpoint).isEqualTo("https://[64:ff9b::808:808]/callback")
    }

    @Test
    fun validate_matchingPrivateIpLiteral_returnsEndpoint() {
        val endpoint = PresentationResponseEndpointValidator.validate(
            "https://10.0.0.5/callback",
            LinkedDomainVerified("10.0.0.5", "https://10.0.0.5")
        )

        assertThat(endpoint).isEqualTo("https://10.0.0.5/callback")
    }

    @Test
    fun validate_customResolverEndpointWithPath_returnsEndpoint() {
        val linkedDomain = RootOfTrust(
            "https://verifier.example/.well-known/did-configuration.json",
            true
        ).toLinkedDomainResult()

        val endpoint = PresentationResponseEndpointValidator.validate(
            "https://verifier.example/presentation/callback",
            linkedDomain
        )

        assertThat(endpoint).isEqualTo("https://verifier.example/presentation/callback")
    }

    @Test
    fun validate_authorizedExternalResponseService_returnsEndpoint() {
        val endpoint = PresentationResponseEndpointValidator.validate(
            "https://verifiedid.did.msidentity.com/v1.0/verifyPresentation",
            LinkedDomainVerified(
                "eu-syntheticsdocumentprovider.azurewebsites.net",
                "https://eu-syntheticsdocumentprovider.azurewebsites.net"
            )
        )

        assertThat(endpoint).isEqualTo(
            "https://verifiedid.did.msidentity.com/v1.0/verifyPresentation"
        )
        assertThat(eventRecorder.events.single()).isEqualTo(
            WalletLibraryEventRecorder.Event(
                "PresentationResponseValidation",
                mapOf(
                    "stage" to "destination_validation",
                    "flight_enabled" to "true",
                    "outcome" to "accepted",
                    "reason" to "none",
                    "destination_relationship" to "external_host",
                    "transport" to "not_applicable",
                    "response_count" to "not_applicable",
                    "http_status_class" to "not_applicable"
                )
            )
        )
    }

    @Test
    fun validate_unverifiedOrMissingLinkedDomain_throwsPresentationException() {
        assertThatThrownBy {
            PresentationResponseEndpointValidator.validate(
                "https://verifier.example/callback",
                LinkedDomainUnVerified("verifier.example")
            )
        }.isInstanceOf(PresentationException::class.java)

        assertThat(eventRecorder.events).allMatch {
            val properties = it.properties
            properties?.get("outcome") == "rejected" &&
                properties["reason"] == "unverified_domain"
        }

        assertThatThrownBy {
            PresentationResponseEndpointValidator.validate(
                "https://verifier.example/callback",
                LinkedDomainMissing
            )
        }.isInstanceOf(PresentationException::class.java)
    }

    @Test
    fun validate_unsafeUriForms_throwPresentationException() {
        val linkedDomain = LinkedDomainVerified("verifier.example", "https://verifier.example")
        listOf(
            "http://verifier.example/callback",
            "https://verifier.example@attacker.example/callback",
            "https://verifier.example/callback#fragment",
            "/relative/callback",
            "openid://verifier.example/callback"
        ).forEach { endpoint ->
            assertThatThrownBy {
                PresentationResponseEndpointValidator.validate(endpoint, linkedDomain)
            }.isInstanceOf(PresentationException::class.java)
        }
    }

}
