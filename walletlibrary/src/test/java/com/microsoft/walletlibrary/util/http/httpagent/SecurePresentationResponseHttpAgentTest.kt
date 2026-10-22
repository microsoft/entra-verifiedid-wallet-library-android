package com.microsoft.walletlibrary.util.http.httpagent

import okhttp3.Dns
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import java.io.IOException
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
    fun post_truncatedResponseBody_resumesWithException() {
        val truncatedBody = object : ResponseBody() {
            override fun contentType(): MediaType? = null

            override fun contentLength(): Long = 2

            override fun source(): BufferedSource = Buffer().writeByte(1)
        }
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(truncatedBody)
                    .build()
            }
            .build()
        val agent = SecurePresentationResponseHttpAgent(client)

        assertThatThrownBy {
            runBlocking {
                agent.post("https://verifier.example/callback", emptyMap(), ByteArray(0))
            }
        }.isInstanceOf(IOException::class.java)
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
