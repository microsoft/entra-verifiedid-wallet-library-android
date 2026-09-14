package com.microsoft.walletlibrary.util.http.httpagent

import okhttp3.Dns
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import java.net.InetAddress
import java.net.Proxy
import java.net.UnknownHostException

class SecurePresentationResponseHttpAgentTest {
    @Test
    fun secureClient_disablesRedirectsAndProxies() {
        val client = SecurePresentationResponseHttpAgent().client

        assertThat(client.followRedirects).isFalse
        assertThat(client.followSslRedirects).isFalse
        assertThat(client.proxy).isEqualTo(Proxy.NO_PROXY)
    }

    @Test
    fun publicNetworkDns_rejectsPrivateAddress() {
        val dns = PublicNetworkDns(
            object : Dns {
                override fun lookup(hostname: String) = listOf(InetAddress.getByName("10.0.0.5"))
            }
        )

        assertThatThrownBy { dns.lookup("verifier.example") }
            .isInstanceOf(UnknownHostException::class.java)
    }

    @Test
    fun publicNetworkDns_returnsPublicAddress() {
        val expectedAddress = InetAddress.getByName("8.8.8.8")
        val dns = PublicNetworkDns(
            object : Dns {
                override fun lookup(hostname: String) = listOf(expectedAddress)
            }
        )

        assertThat(dns.lookup("verifier.example")).containsExactly(expectedAddress)
    }
}
