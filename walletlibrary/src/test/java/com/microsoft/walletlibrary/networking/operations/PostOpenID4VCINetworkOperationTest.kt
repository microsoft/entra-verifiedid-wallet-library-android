package com.microsoft.walletlibrary.networking.operations

import com.microsoft.walletlibrary.did.sdk.datasource.network.apis.HttpAgentApiProvider
import com.microsoft.walletlibrary.networking.entities.openid4vci.RawOpenID4VCIResponse
import com.microsoft.walletlibrary.util.NetworkingException
import com.microsoft.walletlibrary.util.defaultTestSerializer
import com.microsoft.walletlibrary.util.http.httpagent.IResponse
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PostOpenID4VCINetworkOperationTest {
    private val expectedCredential = "valid raw VC"
    private val mockRawOpenID4VCIResponse: RawOpenID4VCIResponse = mockk {
        every { credential } returns expectedCredential
    }

    @Test
    fun postOpenID4VCIRequestTest_ValidIssuanceRequest_ReturnsVerifiableCredential() {
        // Arrange
        val apiProvider: HttpAgentApiProvider = mockk {
            every { openId4VciApi } returns mockk {
                coEvery { postOpenID4VCIRequest(any(), any(), any()) } returns Result.success(
                    IResponse(
                        status = 200,
                        headers = emptyMap(),
                        body = defaultTestSerializer.encodeToString(
                            RawOpenID4VCIResponse.serializer(),
                            mockRawOpenID4VCIResponse
                        ).toByteArray(Charsets.UTF_8)
                    )
                )
            }
        }
        val operation =
            PostOpenID4VCINetworkOperation("", "", "", apiProvider, defaultTestSerializer)

        runBlocking {
            // Act
            val actual = operation.fire()

            // Assert
            assertThat(actual.isSuccess).isTrue
            val unwrapped = actual.getOrNull()?.credential
            assertThat(unwrapped).isEqualTo(expectedCredential)
        }
    }

    @Test
    fun postOpenID4VCIRequestTest_BadRequest_ReturnsFailureWithException() {
        // Arrange
        val apiProvider: HttpAgentApiProvider = mockk {
            every { openId4VciApi } returns mockk {
                coEvery { postOpenID4VCIRequest(any(), any(), any()) } returns Result.failure(
                    IResponse(
                        status = 400,
                        headers = emptyMap(),
                        body = "Bad request".toByteArray(Charsets.UTF_8)
                    ).toNetworkingException()
                )
            }
        }
        val operation =
            PostOpenID4VCINetworkOperation("", "", "", apiProvider, defaultTestSerializer)

        runBlocking {
            // Act
            val actual = operation.fire()

            // Assert
            assertThat(actual.isFailure).isTrue
            val unwrapped = actual.exceptionOrNull()
            assertThat(unwrapped).isInstanceOf(NetworkingException::class.java)
        }
    }
}