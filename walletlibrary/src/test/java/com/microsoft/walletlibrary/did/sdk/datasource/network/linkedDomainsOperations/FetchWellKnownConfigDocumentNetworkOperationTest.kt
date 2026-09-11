// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.did.sdk.datasource.network.linkedDomainsOperations

import com.microsoft.walletlibrary.did.sdk.credential.service.models.serviceResponses.LinkedDomainsResponse
import com.microsoft.walletlibrary.did.sdk.datasource.network.apis.HttpAgentApiProvider
import com.microsoft.walletlibrary.did.sdk.datasource.network.apis.HttpAgentLinkedDomainsApi
import com.microsoft.walletlibrary.util.NetworkingException
import com.microsoft.walletlibrary.util.http.httpagent.IResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class FetchWellKnownConfigDocumentNetworkOperationTest {

    @Test
    fun exactOrigin_fetchesOnlyRootWellKnownConfiguration() {
        val response = IResponse(200, emptyMap(), ByteArray(0))
        val expected = LinkedDomainsResponse("", emptyList())
        val api = mockk<HttpAgentLinkedDomainsApi> {
            coEvery {
                fetchWellKnownConfigDocument("https://example.com/.well-known/did-configuration.json")
            } returns Result.success(response)
            every { toLinkedDomainsResponse(response) } returns expected
        }
        val apiProvider = mockk<HttpAgentApiProvider> {
            every { linkedDomainsApis } returns api
        }

        val result = runBlocking {
            FetchWellKnownConfigDocumentNetworkOperation("https://example.com", apiProvider).fire()
        }

        assertThat(result.getOrNull()).isEqualTo(expected)
        coVerify(exactly = 1) {
            api.fetchWellKnownConfigDocument("https://example.com/.well-known/did-configuration.json")
        }
    }

    @Test
    fun redirectResponse_isRejectedWithoutFollowingLocation() {
        val redirect = IResponse(
            302,
            mapOf("Location" to "http://attacker.example/.well-known/did-configuration.json"),
            ByteArray(0)
        ).toNetworkingException()
        val api = mockk<HttpAgentLinkedDomainsApi> {
            coEvery { fetchWellKnownConfigDocument(any()) } returns Result.failure(redirect)
        }
        val apiProvider = mockk<HttpAgentApiProvider> {
            every { linkedDomainsApis } returns api
        }

        val result = runBlocking {
            FetchWellKnownConfigDocumentNetworkOperation("https://example.com", apiProvider).fire()
        }

        assertThat(result.exceptionOrNull()).isInstanceOf(NetworkingException::class.java)
        assertThat((result.exceptionOrNull() as NetworkingException).statusCode).isEqualTo("302")
        coVerify(exactly = 1) { api.fetchWellKnownConfigDocument(any()) }
    }
}
