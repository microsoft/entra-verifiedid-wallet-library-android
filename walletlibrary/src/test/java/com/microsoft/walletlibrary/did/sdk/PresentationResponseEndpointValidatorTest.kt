package com.microsoft.walletlibrary.did.sdk

import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainMissing
import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainUnVerified
import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainVerified
import com.microsoft.walletlibrary.did.sdk.util.controlflow.PresentationException
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

        assertThat(endpoint).isEqualTo("https://VERIFIER.example:443/presentation/callback?request=123")
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
    fun validate_differentOrigin_throwsPresentationException() {
        assertThatThrownBy {
            PresentationResponseEndpointValidator.validate(
                "https://attacker.example/callback",
                LinkedDomainVerified("verifier.example", "https://verifier.example")
            )
        }.isInstanceOf(PresentationException::class.java)
    }

    @Test
    fun validate_subdomainOfVerifiedOrigin_throwsPresentationException() {
        assertThatThrownBy {
            PresentationResponseEndpointValidator.validate(
                "https://evil.verifier.example/callback",
                LinkedDomainVerified("verifier.example", "https://verifier.example")
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

    @Test
    fun validate_privateAndLoopbackIpLiterals_throwPresentationException() {
        listOf(
            "10.0.0.5",
            "127.0.0.1",
            "169.254.169.254",
            "192.168.1.1",
            "::1",
            "fc00::1",
            "fe80::1"
        ).forEach { host ->
            val formattedHost = if (host.contains(":")) "[$host]" else host
            assertThatThrownBy {
                PresentationResponseEndpointValidator.validate(
                    "https://$formattedHost/callback",
                    LinkedDomainVerified(host, "https://$formattedHost")
                )
            }.isInstanceOf(PresentationException::class.java)
        }
    }
}
