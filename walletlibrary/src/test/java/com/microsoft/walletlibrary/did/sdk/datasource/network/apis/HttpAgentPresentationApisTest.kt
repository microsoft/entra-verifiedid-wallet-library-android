package com.microsoft.walletlibrary.did.sdk.datasource.network.apis

import com.microsoft.walletlibrary.did.sdk.util.HttpAgentUtils
import com.microsoft.walletlibrary.util.http.httpagent.IHttpAgent
import com.microsoft.walletlibrary.util.http.httpagent.IResponse
import com.microsoft.walletlibrary.util.http.httpagent.PresentationResponseHttpAgent
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HttpAgentPresentationApisTest {
    @Test
    fun sendResponse_usesNoRedirectTransport() {
        val url = "https://verifier.example/callback"
        val response = IResponse(200, emptyMap(), ByteArray(0))
        val agent = mockk<IHttpAgent>(relaxed = true)
        val responseAgent = mockk<PresentationResponseHttpAgent> {
            coEvery { post(url, any(), any()) } returns Result.success(response)
        }
        val httpAgentUtils = mockk<HttpAgentUtils>(relaxed = true) {
            every { combineMaps(any(), any()) } returns emptyMap()
        }
        val api = HttpAgentPresentationApis(agent, httpAgentUtils, responseAgent)

        val result = runBlocking {
            api.sendResponse(url, "id-token", "vp-token", "state")
        }

        assertThat(result.getOrNull()).isSameAs(response)
        coVerify(exactly = 1) { responseAgent.post(url, any(), any()) }
        coVerify(exactly = 0) { agent.post(any(), any(), any()) }
    }

    @Test
    fun sendResponses_usesNoRedirectTransport() {
        val url = "https://verifier.example/callback"
        val response = IResponse(200, emptyMap(), ByteArray(0))
        val agent = mockk<IHttpAgent>(relaxed = true)
        val responseAgent = mockk<PresentationResponseHttpAgent> {
            coEvery { post(url, any(), any()) } returns Result.success(response)
        }
        val httpAgentUtils = mockk<HttpAgentUtils>(relaxed = true) {
            every { combineMaps(any(), any()) } returns emptyMap()
        }
        val api = HttpAgentPresentationApis(agent, httpAgentUtils, responseAgent)

        val result = runBlocking {
            api.sendResponses(url, "id-token", listOf("vp-token-1", "vp-token-2"), "state")
        }

        assertThat(result.getOrNull()).isSameAs(response)
        coVerify(exactly = 1) { responseAgent.post(url, any(), any()) }
        coVerify(exactly = 0) { agent.post(any(), any(), any()) }
    }
}
