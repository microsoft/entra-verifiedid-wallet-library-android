package com.microsoft.walletlibrary.requests.handlers

import com.microsoft.walletlibrary.util.LibraryConfiguration
import com.microsoft.walletlibrary.util.defaultTestSerializer
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class OpenId4VCIRequestHandlerTest {
    private val mockLibraryConfiguration = mockk<LibraryConfiguration>()
    private val openId4VCIRequestHandler = OpenId4VCIRequestHandler(mockLibraryConfiguration)
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
        val actualCanHandleResult = openId4VCIRequestHandler.canHandle(expectedCredentialOfferString)

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
        val actualCanHandleResult = openId4VCIRequestHandler.canHandle(expectedCredentialOfferString)

        // Assert
        assertThat(actualCanHandleResult).isEqualTo(false)
    }
}