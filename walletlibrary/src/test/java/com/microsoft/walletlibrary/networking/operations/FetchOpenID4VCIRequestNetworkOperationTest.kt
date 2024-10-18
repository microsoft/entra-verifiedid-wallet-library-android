package com.microsoft.walletlibrary.networking.operations

import com.microsoft.walletlibrary.did.sdk.datasource.network.apis.HttpAgentApiProvider
import com.microsoft.walletlibrary.did.sdk.util.controlflow.ClientException
import com.microsoft.walletlibrary.did.sdk.util.controlflow.ForbiddenException
import com.microsoft.walletlibrary.did.sdk.util.controlflow.ServiceUnreachableException
import com.microsoft.walletlibrary.did.sdk.util.controlflow.UnauthorizedException
import com.microsoft.walletlibrary.util.http.httpagent.IHttpAgent
import com.microsoft.walletlibrary.util.http.httpagent.IResponse
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class FetchOpenID4VCIRequestNetworkOperationTest {
    private val expectedCredentialOffer = """
        {
            "credential_issuer": "metadata_url",
            "issuer_session": "request_state",
            "credential_configuration_ids": [
                "credential_id"
            ],
            "grants": {
                "authorization_code": {
                    "authorization_server": "authorization_server"
                }
            }
        }
    """.trimIndent()

    @Test
    fun fetchOpenID4VCIRequestNetworkOperationTest_ResolveCredentialOffer_ReturnsCredentialOffer() {
        // Arrange
        val apiProvider: HttpAgentApiProvider = mockk {
            every { openId4VciApi } returns mockk {
                coEvery { getOpenID4VCIRequest(any(), any()) } returns Result.success(
                    IResponse(
                        status = 200,
                        headers = emptyMap(),
                        body = expectedCredentialOffer.toByteArray(Charsets.UTF_8)
                    )
                )
            }
        }
        val operation = FetchOpenID4VCIRequestNetworkOperation("", emptyList(), apiProvider)

        runBlocking {
            // Act
            val actual = operation.fire()

            // Assert
            assertThat(actual.isSuccess).isTrue
            val unwrapped = actual.getOrThrow().decodeToString()
            assertThat(unwrapped).isEqualTo(expectedCredentialOffer)
        }
    }

    @Test
    fun fetchOpenID4VCIRequestNetworkOperationTest_ResolveCredentialOfferFailsWith400_ReturnsFailureWithClientException() {
        // Arrange
        val apiProvider: HttpAgentApiProvider = mockk {
            every { openId4VciApi } returns mockk {
                coEvery { getOpenID4VCIRequest(any(), any()) } returns Result.failure(
                    IHttpAgent.ClientException(
                        IResponse(
                            status = 400,
                            headers = emptyMap(),
                            body = "Bad request".toByteArray(Charsets.UTF_8)
                        )
                    )
                )
            }
        }
        val operation = FetchOpenID4VCIRequestNetworkOperation("", emptyList(), apiProvider)

        runBlocking {
            // Act
            val actual = operation.fire()

            // Assert
            assertThat(actual.isFailure).isTrue
            val unwrapped = actual.exceptionOrNull()
            assertThat(unwrapped).isInstanceOf(ClientException::class.java)
            assertThat((unwrapped as ClientException).errorCode?.toInt()).isEqualTo(400)
        }
    }

    @Test
    fun fetchOpenID4VCIRequestNetworkOperationTest_ResolveCredentialOfferFailsWith401_ReturnsFailureWithUnauthorizedException() {
        // Arrange
        val apiProvider: HttpAgentApiProvider = mockk {
            every { openId4VciApi } returns mockk {
                coEvery { getOpenID4VCIRequest(any(), any()) } returns Result.failure(
                    IHttpAgent.ClientException(
                        IResponse(
                            status = 401,
                            headers = emptyMap(),
                            body = "Bad request".toByteArray(Charsets.UTF_8)
                        )
                    )
                )
            }
        }
        val operation = FetchOpenID4VCIRequestNetworkOperation("", emptyList(), apiProvider)

        runBlocking {
            // Act
            val actual = operation.fire()

            // Assert
            assertThat(actual.isFailure).isTrue
            val unwrapped = actual.exceptionOrNull()
            assertThat(unwrapped).isInstanceOf(UnauthorizedException::class.java)
            assertThat((unwrapped as UnauthorizedException).errorCode?.toInt()).isEqualTo(401)
        }
    }

    @Test
    fun fetchOpenID4VCIRequestNetworkOperationTest_ResolveCredentialOfferFailsWith403_ReturnsFailureWithForbiddenException() {
        // Arrange
        val apiProvider: HttpAgentApiProvider = mockk {
            every { openId4VciApi } returns mockk {
                coEvery { getOpenID4VCIRequest(any(), any()) } returns Result.failure(
                    IHttpAgent.ClientException(
                        IResponse(
                            status = 403,
                            headers = emptyMap(),
                            body = "Bad request".toByteArray(Charsets.UTF_8)
                        )
                    )
                )
            }
        }
        val operation = FetchOpenID4VCIRequestNetworkOperation("", emptyList(), apiProvider)

        runBlocking {
            // Act
            val actual = operation.fire()

            // Assert
            assertThat(actual.isFailure).isTrue
            val unwrapped = actual.exceptionOrNull()
            assertThat(unwrapped).isInstanceOf(ForbiddenException::class.java)
            assertThat((unwrapped as ForbiddenException).errorCode?.toInt()).isEqualTo(403)
        }
    }
    @Test
    fun fetchOpenID4VCIRequestNetworkOperationTest_ResolveCredentialOfferFailsWith500_ReturnsFailureWithServiceUnreachableException() {
        // Arrange
        val apiProvider: HttpAgentApiProvider = mockk {
            every { openId4VciApi } returns mockk {
                coEvery { getOpenID4VCIRequest(any(), any()) } returns Result.failure(
                    IHttpAgent.ServerException(
                        IResponse(
                            status = 500,
                            headers = emptyMap(),
                            body = "Bad request".toByteArray(Charsets.UTF_8)
                        )
                    )
                )
            }
        }
        val operation = FetchOpenID4VCIRequestNetworkOperation("", emptyList(), apiProvider)

        runBlocking {
            // Act
            val actual = operation.fire()

            // Assert
            assertThat(actual.isFailure).isTrue
            val unwrapped = actual.exceptionOrNull()
            assertThat(unwrapped).isInstanceOf(ServiceUnreachableException::class.java)
            assertThat((unwrapped as ServiceUnreachableException).errorCode?.toInt()).isEqualTo(500)
        }
    }
}