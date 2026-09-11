// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.did.sdk.datasource.network.apis

import com.microsoft.walletlibrary.did.sdk.util.HttpAgentUtils
import com.microsoft.walletlibrary.util.http.httpagent.IHttpAgent
import com.microsoft.walletlibrary.util.http.httpagent.IResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HttpAgentLinkedDomainsApiTest {

    @Test
    fun fetchWellKnownConfigDocument_disablesRedirectsForLinkedDomainRequest() {
        val url = "https://example.com/.well-known/did-configuration.json"
        val headers = mutableMapOf("header" to "value")
        val response = IResponse(200, emptyMap(), ByteArray(0))
        val agent = mockk<IHttpAgent> {
            coEvery { getWithoutRedirects(url, headers) } returns Result.success(response)
        }
        val httpAgentUtils = mockk<HttpAgentUtils> {
            every { defaultHeaders() } returns headers
        }
        val api = HttpAgentLinkedDomainsApi(agent, httpAgentUtils, Json)

        val result = runBlocking {
            api.fetchWellKnownConfigDocument(url)
        }

        assertThat(result.getOrNull()).isSameAs(response)
        coVerify(exactly = 1) { agent.getWithoutRedirects(url, headers) }
        coVerify(exactly = 0) { agent.get(any(), any()) }
    }
}
