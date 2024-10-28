package com.microsoft.walletlibrary.networking.operations

import com.microsoft.walletlibrary.did.sdk.datasource.network.apis.HttpAgentApiProvider
import com.microsoft.walletlibrary.networking.entities.openid4vci.OpenID4VCIPreAuthTokenResponse
import com.microsoft.walletlibrary.networking.entities.openid4vci.request.OpenID4VCIPreAuthTokenRequest
import com.microsoft.walletlibrary.util.NetworkingException
import com.microsoft.walletlibrary.util.defaultTestSerializer
import com.microsoft.walletlibrary.util.http.httpagent.IResponse
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.junit.Test

class PostOpenID4VCIPreAuthNetworkOperationTest {
    private val expectedAccessToken = "valid access token"
    private val mockOpenID4VCIPreAuthTokenResponse: OpenID4VCIPreAuthTokenResponse = mockk {
        every { access_token } returns expectedAccessToken
    }

    @Test
    fun postOpenID4VCIPreAuthTokenTest_ValidRequest_ReturnsAccessToken() {
        // Arrange
        val mockGrantType = "grant_type"
        val mockPreAuthorizedCode = "pre_authorized_code"
        val mockTxCode = "tx_code"
        val mockOpenID4VCIPreAuthTokenRequest = mockk<OpenID4VCIPreAuthTokenRequest> {
            every { grant_type } returns mockGrantType
            every { pre_authorized_code } returns mockPreAuthorizedCode
            every { tx_code } returns mockTxCode
        }
        val apiProvider: HttpAgentApiProvider = mockk {
            every { openId4VciApi } returns mockk {
                coEvery {
                    postOpenID4VCIPreAuthToken(
                        any(),
                        mockGrantType,
                        mockPreAuthorizedCode,
                        mockTxCode
                    )
                } returns Result.success(
                    IResponse(
                        status = 200,
                        headers = emptyMap(),
                        body = defaultTestSerializer.encodeToString(
                            OpenID4VCIPreAuthTokenResponse.serializer(),
                            mockOpenID4VCIPreAuthTokenResponse
                        ).toByteArray(Charsets.UTF_8)
                    )
                )
            }
        }
        val operation = PostOpenID4VCIPreAuthNetworkOperation(
            "",
            mockOpenID4VCIPreAuthTokenRequest,
            apiProvider,
            defaultTestSerializer
        )

        runBlocking {
            // Act
            val actual = operation.fire()

            // Assert
            Assertions.assertThat(actual.isSuccess).isTrue
            val unwrapped = actual.getOrNull()?.access_token
            Assertions.assertThat(unwrapped).isEqualTo(expectedAccessToken)
        }
    }

    @Test
    fun postOpenID4VCIPreAuthTokenTest_BadRequest_ReturnsFailureWithException() {
        // Arrange
        val mockGrantType = "grant_type"
        val mockPreAuthorizedCode = "pre_authorized_code"
        val mockTxCode = "tx_code"
        val mockOpenID4VCIPreAuthTokenRequest = mockk<OpenID4VCIPreAuthTokenRequest> {
            every { grant_type } returns mockGrantType
            every { pre_authorized_code } returns mockPreAuthorizedCode
            every { tx_code } returns mockTxCode
        }
        val apiProvider: HttpAgentApiProvider = mockk {
            every { openId4VciApi } returns mockk {
                coEvery {
                    postOpenID4VCIPreAuthToken(
                        any(),
                        mockGrantType,
                        mockPreAuthorizedCode,
                        mockTxCode
                    )
                } returns Result.failure(
                    IResponse(
                        status = 400,
                        headers = emptyMap(),
                        body = "Bad request".toByteArray(Charsets.UTF_8)
                    ).toNetworkingException()
                )
            }
        }
        val operation = PostOpenID4VCIPreAuthNetworkOperation(
            "",
            mockOpenID4VCIPreAuthTokenRequest,
            apiProvider,
            defaultTestSerializer
        )

        runBlocking {
            // Act
            val actual = operation.fire()

            // Assert
            Assertions.assertThat(actual.isFailure).isTrue
            val unwrapped = actual.exceptionOrNull()
            Assertions.assertThat(unwrapped).isInstanceOf(NetworkingException::class.java)
        }
    }
}