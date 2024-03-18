package com.microsoft.walletlibrary.requests.handlers

import com.microsoft.walletlibrary.did.sdk.util.controlflow.NetworkException
import com.microsoft.walletlibrary.did.sdk.util.controlflow.SdkException
import com.microsoft.walletlibrary.util.LibraryConfiguration
import com.microsoft.walletlibrary.util.OpenId4VciRequestException
import com.microsoft.walletlibrary.util.OpenId4VciValidationException
import com.microsoft.walletlibrary.util.defaultTestSerializer
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import kotlin.Result as KotlinResult

class OpenId4VCIRequestHandlerTest {
    private val mockLibraryConfiguration = mockk<LibraryConfiguration>()
    private val openId4VCIRequestHandler = spyk(OpenId4VCIRequestHandler(mockLibraryConfiguration))
    private val expectedCredentialOfferString = """
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
    fun canHandleTest_ValidCredentialOfferAsString_ReturnsTrue() {
        // Arrange
        every { mockLibraryConfiguration.serializer } returns defaultTestSerializer

        //Act
        val actualCanHandleResult =
            openId4VCIRequestHandler.canHandle(expectedCredentialOfferString)

        // Assert
        assertThat(actualCanHandleResult).isEqualTo(true)
    }

    @Test
    fun canHandleTest_InvalidCredentialOfferAsString_ReturnsFalse() {
        // Arrange
        every { mockLibraryConfiguration.serializer } returns defaultTestSerializer
        val invalidCredentialOfferString = "invalid_credential_offer"

        //Act
        val actualCanHandleResult = openId4VCIRequestHandler.canHandle(invalidCredentialOfferString)

        // Assert
        assertThat(actualCanHandleResult).isEqualTo(false)
    }

    @Test
    fun canHandleTest_EmptyStringAsRequest_ReturnsFalse() {
        // Arrange
        every { mockLibraryConfiguration.serializer } returns defaultTestSerializer
        val invalidCredentialOfferString = ""

        //Act
        val actualCanHandleResult = openId4VCIRequestHandler.canHandle(invalidCredentialOfferString)

        // Assert
        assertThat(actualCanHandleResult).isEqualTo(false)
    }

    @Test
    fun canHandleTest_AnyFailureWithSerializer_ReturnsFalse() {
        //Act
        val actualCanHandleResult =
            openId4VCIRequestHandler.canHandle(expectedCredentialOfferString)

        // Assert
        assertThat(actualCanHandleResult).isEqualTo(false)
    }

    @Test
    fun handleRequestTest_AnyFailureWithSerializer_ThrowsException() {
        runBlocking {
            //Act
            val actualHandleRequestResult = runCatching {
                openId4VCIRequestHandler.handleRequest(expectedCredentialOfferString)
            }
            // Assert
            assertThat(actualHandleRequestResult.isFailure).isTrue
            val actualException = actualHandleRequestResult.exceptionOrNull()
            assertThat(actualException).isInstanceOf(OpenId4VciValidationException::class.java)
            assertThat(actualException?.message).contains("Failed to decode CredentialOffer")
            assertThat((actualException as OpenId4VciValidationException).innerError).isNotNull
        }
    }

    @Test
    fun handleRequestTest_AnyFailureWithFetchingMetadata_ThrowsException() {
        // Arrange
        every { mockLibraryConfiguration.serializer } returns defaultTestSerializer
        coEvery { openId4VCIRequestHandler["fetchCredentialMetadata"]("metadata_url") } returns KotlinResult.failure<SdkException>(
            NetworkException("Failed to fetch metadata", false)
        )

        runBlocking {
            //Act
            val actualHandleRequestResult = runCatching {
                openId4VCIRequestHandler.handleRequest(expectedCredentialOfferString)
            }
            // Assert
            assertThat(actualHandleRequestResult.isFailure).isTrue
            val actualException = actualHandleRequestResult.exceptionOrNull()
            assertThat(actualException).isInstanceOf(OpenId4VciRequestException::class.java)
            assertThat(actualException?.message).contains("Failed to fetch credential metadata")
            assertThat((actualException as OpenId4VciRequestException).innerError).isInstanceOf(NetworkException::class.java)
        }
    }
}