package com.microsoft.walletlibrary.util.http.httpagent

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class OkHttpAgentTest {
    private val clientMock = mockk<OkHttpClient>()
    private val client = OkHttpAgent()

    @Before
    fun beforeAllTests() {
        client.client = clientMock;
    }

    @Test
    fun testGet_withUrlAndHeaders_shouldCallClientWithRequest() {
        // Arrange
        val request = slot<Request>()
        val callback = slot<Callback>()

        val expectedURL = "https://test.local/"
        val expectedStatusCode = 200
        val expectedHeaders = mapOf("Test" to "passed")
        val expectedPayload = "This is a test".toByteArray(Charsets.UTF_8)

        every { clientMock.newCall(capture(request)) } answers {
            mockk {
                every { enqueue(capture(callback)) } answers {
                    assertThat(request.captured.headers["header"]).isEqualTo("it sure is")
                    val responseBuilder = Response.Builder()
                        .protocol(Protocol.HTTP_1_1)
                        .addHeader("Test", "passed")
                        .code(expectedStatusCode)
                        .message("OK")
                        .request(request.captured)
                        .body(mockk() {
                            every {bytes()} returns expectedPayload
                        })
                        .build()
                    callback.captured.onResponse(this@mockk, responseBuilder)
                }
            }
        }
        runBlocking {
            // Act
            val response = client.get(expectedURL, mapOf("header" to "it sure is"))

            // Assert
            assertThat(request.captured.url.toString()).isEqualTo(expectedURL)
            assertThat(response.isSuccess).isTrue
            val unwrapped = response.getOrThrow()
            assertThat(unwrapped.status).isEqualTo(expectedStatusCode)
            assertThat(unwrapped.body).isEqualTo(expectedPayload)
            assertThat(unwrapped.headers).isEqualTo(expectedHeaders)
        }
    }

    @Test
    fun testPost_withUrlAndHeaders_shouldCallClientWithRequest() {
        // Arrange
        val request = slot<Request>()
        val callback = slot<Callback>()

        val expectedURL = "https://test.local/"
        val expectedStatusCode = 201
        val expectedHeaders = mapOf("Test2" to "passed")
        val expectedPostPayload = "Some data".toByteArray(Charsets.UTF_8)
        val expectedPayload = "A post response should be here".toByteArray(Charsets.UTF_8)

        every { clientMock.newCall(capture(request)) } answers {
            mockk {
                every { enqueue(capture(callback)) } answers {
                    assertThat(request.captured.headers["header"]).isEqualTo("it sure is")
                    assertThat(request.captured.body?.contentLength()).isEqualTo(expectedPostPayload.size.toLong())
                    val responseBuilder = Response.Builder()
                        .protocol(Protocol.HTTP_1_1)
                        .addHeader("Test2", "passed")
                        .code(expectedStatusCode)
                        .message("CREATED")
                        .request(request.captured)
                        .body(mockk() {
                            every {bytes()} returns expectedPayload
                        })
                        .build()
                    callback.captured.onResponse(this@mockk, responseBuilder)
                }
            }
        }
        runBlocking {
            // Act
            val response = client.post(expectedURL, mapOf("header" to "it sure is"), expectedPostPayload)

            // Assert
            assertThat(request.captured.url.toString()).isEqualTo(expectedURL)
            assertThat(response.isSuccess).isTrue
            val unwrapped = response.getOrThrow()
            assertThat(unwrapped.status).isEqualTo(expectedStatusCode)
            assertThat(unwrapped.body).isEqualTo(expectedPayload)
            assertThat(unwrapped.headers).isEqualTo(expectedHeaders)
        }
    }

    @Test
    fun cancelPendingRequest_cancelsOkHttpCallAndPropagatesCancellation() = runBlocking {
        val callback = slot<Callback>()
        val call = mockk<Call>(relaxed = true)
        val responseBody = mockk<ResponseBody>(relaxed = true)
        val response = mockk<Response>(relaxed = true) {
            every { body } returns responseBody
        }
        every { clientMock.newCall(any()) } returns call
        every { call.enqueue(capture(callback)) } returns Unit

        val requestJob = launch {
            client.post("https://test.local/", emptyMap(), ByteArray(0))
        }
        yield()

        requestJob.cancel()
        requestJob.join()

        verify(exactly = 1) { call.cancel() }
        assertThat(requestJob.isCancelled).isTrue

        callback.captured.onResponse(call, response)

        verify(exactly = 0) { responseBody.bytes() }
        verify(exactly = 1) { response.close() }
    }
}