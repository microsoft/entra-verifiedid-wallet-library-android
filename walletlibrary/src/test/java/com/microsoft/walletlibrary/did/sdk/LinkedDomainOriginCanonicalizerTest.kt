// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.did.sdk

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

class LinkedDomainOriginCanonicalizerTest {

    @Test
    fun `equivalent HTTPS origins have the same canonical form`() {
        val equivalentOrigins = listOf(
            "https://example.com",
            "https://example.com/",
            "HTTPS://EXAMPLE.COM",
            "https://example.com:443",
            "https://example.com."
        )

        equivalentOrigins.forEach { origin ->
            assertThat(canonicalizeLinkedDomainOrigin(origin)).isEqualTo("https://example.com")
        }
    }

    @Test
    fun `non-default HTTPS port is preserved`() {
        assertThat(canonicalizeLinkedDomainOrigin("https://Example.COM.:8443/"))
            .isEqualTo("https://example.com:8443")
    }

    @Test
    fun `unsafe or malformed origins are rejected`() {
        val invalidOrigins = listOf(
            "http://example.com",
            "https://user@example.com",
            "https://example.com/path",
            "https://example.com/%2F",
            "https://example.com/./",
            "https://example.com/../",
            "https://example.com/%2e%2e/",
            "https://example.com?query=value",
            "https://example.com#fragment",
            "https://example.com:0",
            "https://example.com:65536",
            "https://example.com:not-a-port",
            "https:example.com",
            "https://example.com..",
            "https://"
        )

        invalidOrigins.forEach { origin ->
            assertThatThrownBy { canonicalizeLinkedDomainOrigin(origin) }
                .describedAs("origin %s", origin)
                .isInstanceOfAny(IllegalArgumentException::class.java)
        }
    }
}
