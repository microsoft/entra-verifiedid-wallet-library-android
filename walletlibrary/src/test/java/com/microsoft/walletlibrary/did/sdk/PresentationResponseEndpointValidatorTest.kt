package com.microsoft.walletlibrary.did.sdk

import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainMissing
import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainUnVerified
import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainVerified
import com.microsoft.walletlibrary.did.sdk.util.controlflow.PresentationException
import com.microsoft.walletlibrary.mappings.toLinkedDomainResult
import com.microsoft.walletlibrary.requests.RootOfTrust
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

class PresentationResponseEndpointValidatorTest {
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
    fun validate_differentOrigin_throwsPresentationException() {
        assertThatThrownBy {
            PresentationResponseEndpointValidator.validate(
                "https://attacker.example/callback",
                LinkedDomainVerified("verifier.example", "https://verifier.example")
            )
        }.isInstanceOf(PresentationException::class.java)
    }

    @Test
    fun validate_subdomainsOfVerifiedOrigin_returnEndpoint() {
        mapOf(
            "https://api.verifier.example/callback" to
                "https://api.verifier.example/callback",
            "https://westus.api.verifier.example/callback" to
                "https://westus.api.verifier.example/callback",
            "https://API.VERIFIER.EXAMPLE./callback" to
                "https://api.verifier.example./callback"
        ).forEach { (endpoint, expectedEndpoint) ->
            assertThat(
                PresentationResponseEndpointValidator.validate(
                    endpoint,
                    LinkedDomainVerified("verifier.example", "https://VERIFIER.EXAMPLE.")
                )
            ).isEqualTo(expectedEndpoint)
        }
    }

    @Test
    fun validate_hostsOutsideVerifiedDomain_throwPresentationException() {
        listOf(
            "https://evil-verifier.example/callback",
            "https://verifier.example.attacker.example/callback",
            "https://attacker.example/callback"
        ).forEach { endpoint ->
            assertThatThrownBy {
                PresentationResponseEndpointValidator.validate(
                    endpoint,
                    LinkedDomainVerified("verifier.example", "https://verifier.example")
                )
            }.isInstanceOf(PresentationException::class.java)
        }
    }

    @Test
    fun validate_subdomainWithDifferentPort_throwsPresentationException() {
        assertThatThrownBy {
            PresentationResponseEndpointValidator.validate(
                "https://api.verifier.example:8443/callback",
                LinkedDomainVerified("verifier.example", "https://verifier.example")
            )
        }.isInstanceOf(PresentationException::class.java)
    }

    @Test
    fun validate_dnsNameEndingInVerifiedIp_throwsPresentationException() {
        assertThatThrownBy {
            PresentationResponseEndpointValidator.validate(
                "https://api.8.8.8.8/callback",
                LinkedDomainVerified("8.8.8.8", "https://8.8.8.8")
            )
        }.isInstanceOf(PresentationException::class.java)
    }

    @Test
    fun validate_unverifiedOrMissingLinkedDomain_throwsPresentationException() {
        assertThatThrownBy {
            PresentationResponseEndpointValidator.validate(
                "https://verifier.example/callback",
                LinkedDomainUnVerified("verifier.example")
            )
        }.isInstanceOf(PresentationException::class.java)

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
