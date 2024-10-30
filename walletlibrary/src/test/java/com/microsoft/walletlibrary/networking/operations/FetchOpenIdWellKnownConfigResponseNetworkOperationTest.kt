package com.microsoft.walletlibrary.networking.operations

import com.microsoft.walletlibrary.did.sdk.datasource.network.apis.HttpAgentApiProvider
import com.microsoft.walletlibrary.did.sdk.di.defaultTestSerializer
import com.microsoft.walletlibrary.did.sdk.util.controlflow.ClientException
import com.microsoft.walletlibrary.util.NetworkingException
import com.microsoft.walletlibrary.util.http.httpagent.IHttpAgent
import com.microsoft.walletlibrary.util.http.httpagent.IResponse
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class FetchOpenIdWellKnownConfigResponseNetworkOperationTest {
    private val expectedOpenIdWellKnownConfig = """
        {
            "issuer": "issuer_endpoint",
            "token_endpoint": "token_endpoint",
            "grant_types_supported": [
                "urn:ietf:params:oauth:grant-type:pre-authorized_code"
            ]
        }
    """.trimIndent()

    @Test
    fun getOpenIdWellKnownConfigTest_ValidRequest_ReturnsOpenIdWellKnownConfig() {
        // Arrange
        val apiProvider: HttpAgentApiProvider = mockk {
            every { openId4VciApi } returns mockk {
                coEvery { getOpenIdWellKnownConfig(any()) } returns Result.success(
                    IResponse(
                        status = 200,
                        headers = emptyMap(),
                        body = expectedOpenIdWellKnownConfig.toByteArray(Charsets.UTF_8)
                    )
                )
            }
        }
        val operation = FetchOpenIdWellKnownConfigNetworkOperation("", apiProvider, defaultTestSerializer)

        runBlocking {
            // Act
            val actual = operation.fire()

            // Assert
            assertThat(actual.isSuccess).isTrue
            val unwrapped = actual.getOrNull()
            assertThat(unwrapped).isNotNull
            assertThat(unwrapped?.token_endpoint).isEqualTo("token_endpoint")
        }
    }

    @Test
    fun fetchOpenID4VCIRequestNetworkOperationTest_ResolveCredentialOfferFailsWith400_ReturnsFailureWithClientException() {
        // Arrange

        // Arrange
        val apiProvider: HttpAgentApiProvider = mockk {
            every { openId4VciApi } returns mockk {
                coEvery { getOpenIdWellKnownConfig(any()) } returns Result.failure(
                        IResponse(
                            status = 400,
                            headers = emptyMap(),
                            body = "Bad request".toByteArray(Charsets.UTF_8)
                        ).toNetworkingException()
                    )
            }
        }
        val operation = FetchOpenIdWellKnownConfigNetworkOperation("", apiProvider, defaultTestSerializer)

        runBlocking {
            // Act
            val actual = operation.fire()

            // Assert
            assertThat(actual.isFailure).isTrue
            val unwrapped = actual.exceptionOrNull()
            assertThat(unwrapped).isInstanceOf(NetworkingException::class.java)
            assertThat((unwrapped as NetworkingException).statusCode).isEqualTo("400")
        }
    }
}