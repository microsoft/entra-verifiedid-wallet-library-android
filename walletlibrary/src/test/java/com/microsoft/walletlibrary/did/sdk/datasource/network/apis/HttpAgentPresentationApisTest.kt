package com.microsoft.walletlibrary.did.sdk.datasource.network.apis

import com.microsoft.walletlibrary.WalletLibraryFlight
import com.microsoft.walletlibrary.WalletLibraryFlightProvider
import com.microsoft.walletlibrary.did.sdk.util.HttpAgentUtils
import com.microsoft.walletlibrary.util.NetworkingException
import com.microsoft.walletlibrary.util.WalletLibraryEventRecorder
import com.microsoft.walletlibrary.util.WalletLibraryLogger
import com.microsoft.walletlibrary.util.http.httpagent.IHttpAgent
import com.microsoft.walletlibrary.util.http.httpagent.IResponse
import com.microsoft.walletlibrary.util.http.httpagent.PresentationResponseHttpAgent
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test

class HttpAgentPresentationApisTest {
    private val eventRecorder = WalletLibraryEventRecorder()

    @Before
    fun setUp() {
        WalletLibraryLogger.addConsumer(eventRecorder)
    }

    @After
    fun tearDown() {
        WalletLibraryLogger.CONSUMERS.clear()
    }

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
        val api = HttpAgentPresentationApis(
            agent,
            httpAgentUtils,
            WalletLibraryFlightProvider {
                it == WalletLibraryFlight.PresentationResponseDestinationValidation
            },
            responseAgent
        )

        val result = runBlocking {
            api.sendResponse(url, "id-token", "vp-token", "state")
        }

        assertThat(result.getOrNull()).isSameAs(response)
        assertThat(eventRecorder.events.single().properties).containsAllEntriesOf(
            mapOf(
                "stage" to "response_post",
                "flight_enabled" to "true",
                "outcome" to "succeeded",
                "transport" to "redirect_resistant",
                "response_count" to "single"
            )
        )
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
        val api = HttpAgentPresentationApis(
            agent,
            httpAgentUtils,
            WalletLibraryFlightProvider {
                it == WalletLibraryFlight.PresentationResponseDestinationValidation
            },
            responseAgent
        )

        val result = runBlocking {
            api.sendResponses(url, "id-token", listOf("vp-token-1", "vp-token-2"), "state")
        }

        assertThat(result.getOrNull()).isSameAs(response)
        coVerify(exactly = 1) { responseAgent.post(url, any(), any()) }
        coVerify(exactly = 0) { agent.post(any(), any(), any()) }
    }

    @Test
    fun sendResponse_usesProvidedAgentWhenValidationIsDisabled() {
        val url = "http://10.0.0.5/callback"
        val response = IResponse(200, emptyMap(), ByteArray(0))
        val agent = mockk<IHttpAgent> {
            coEvery { post(url, any(), any()) } returns Result.success(response)
        }
        val responseAgent = mockk<PresentationResponseHttpAgent>(relaxed = true)
        val httpAgentUtils = mockk<HttpAgentUtils>(relaxed = true) {
            every { combineMaps(any(), any()) } returns emptyMap()
        }
        val api = HttpAgentPresentationApis(
            agent,
            httpAgentUtils,
            WalletLibraryFlightProvider { false },
            responseAgent
        )

        val result = runBlocking {
            api.sendResponse(url, "id-token", "vp-token", "state")
        }

        assertThat(result.getOrNull()).isSameAs(response)
        coVerify(exactly = 1) { agent.post(url, any(), any()) }
        coVerify(exactly = 0) { responseAgent.post(any(), any(), any()) }
    }

    @Test
    fun sendResponses_usesProvidedAgentWhenValidationIsDisabled() {
        val url = "http://10.0.0.5/callback"
        val response = IResponse(200, emptyMap(), ByteArray(0))
        val agent = mockk<IHttpAgent> {
            coEvery { post(url, any(), any()) } returns Result.success(response)
        }
        val responseAgent = mockk<PresentationResponseHttpAgent>(relaxed = true)
        val httpAgentUtils = mockk<HttpAgentUtils>(relaxed = true) {
            every { combineMaps(any(), any()) } returns emptyMap()
        }
        val api = HttpAgentPresentationApis(
            agent,
            httpAgentUtils,
            WalletLibraryFlightProvider { false },
            responseAgent
        )

        val result = runBlocking {
            api.sendResponses(url, "id-token", listOf("vp-token-1", "vp-token-2"), "state")
        }

        assertThat(result.getOrNull()).isSameAs(response)
        coVerify(exactly = 1) { agent.post(url, any(), any()) }
        coVerify(exactly = 0) { responseAgent.post(any(), any(), any()) }
    }

    @Test
    fun sendResponse_usesCurrentFlightState() {
        val url = "https://verifier.example/callback"
        val response = IResponse(200, emptyMap(), ByteArray(0))
        var validationEnabled = false
        val agent = mockk<IHttpAgent> {
            coEvery { post(url, any(), any()) } returns Result.success(response)
        }
        val responseAgent = mockk<PresentationResponseHttpAgent> {
            coEvery { post(url, any(), any()) } returns Result.success(response)
        }
        val httpAgentUtils = mockk<HttpAgentUtils>(relaxed = true) {
            every { combineMaps(any(), any()) } returns emptyMap()
        }
        val api = HttpAgentPresentationApis(
            agent,
            httpAgentUtils,
            WalletLibraryFlightProvider { validationEnabled },
            responseAgent
        )

        runBlocking {
            api.sendResponse(url, "id-token", "vp-token", "state")
            validationEnabled = true
            api.sendResponse(url, "id-token", "vp-token", "state")
        }

        coVerify(exactly = 1) { agent.post(url, any(), any()) }
        coVerify(exactly = 1) { responseAgent.post(url, any(), any()) }
    }

    @Test
    fun sendResponse_recordsBoundedNetworkFailure() {
        val url = "https://verifier.example/callback"
        val failure = NetworkingException(
            message = "Service unavailable",
            code = "network_error",
            statusCode = "503"
        )
        val responseAgent = mockk<PresentationResponseHttpAgent> {
            coEvery { post(url, any(), any()) } returns Result.failure(failure)
        }
        val api = HttpAgentPresentationApis(
            mockk(relaxed = true),
            mockk(relaxed = true) {
                every { combineMaps(any(), any()) } returns emptyMap()
            },
            WalletLibraryFlightProvider { true },
            responseAgent
        )

        val result = runBlocking {
            api.sendResponse(url, "id-token", "vp-token", "state")
        }

        assertThat(result.exceptionOrNull()).isSameAs(failure)
        assertThat(eventRecorder.events.single().properties).containsAllEntriesOf(
            mapOf(
                "stage" to "response_post",
                "flight_enabled" to "true",
                "outcome" to "failed",
                "reason" to "network_error",
                "transport" to "redirect_resistant",
                "response_count" to "single",
                "http_status_class" to "5xx"
            )
        )
    }
}
