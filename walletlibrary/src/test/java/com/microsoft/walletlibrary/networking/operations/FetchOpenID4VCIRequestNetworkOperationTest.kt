package com.microsoft.walletlibrary.networking.operations

import com.microsoft.walletlibrary.did.sdk.datasource.network.apis.HttpAgentApiProvider
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
    fun fetchOpenID4VCIRequestNetworkOperationTest_withTwoVPTokens_SucceedsToResolvePresentationRequestContent() {
        // Arrange
        val apiProvider: HttpAgentApiProvider = mockk {
            every { presentationApis } returns mockk {
                coEvery { getOpenID4VCIRequest(any()) } returns Result.success(
                    IResponse(
                    status = 200,
                    headers = emptyMap(),
                    body = expectedCredentialOffer.toByteArray(Charsets.UTF_8)
                )
                )
            }
        }
        val operation = FetchOpenID4VCIRequestNetworkOperation("", apiProvider)

        runBlocking {
            // Act
            val actual = operation.fire()

            // Assert
            assertThat(actual.isSuccess).isTrue
            val unwrapped = actual.getOrThrow().decodeToString()
            assertThat(unwrapped).isEqualTo(expectedCredentialOffer)
        }
    }
}