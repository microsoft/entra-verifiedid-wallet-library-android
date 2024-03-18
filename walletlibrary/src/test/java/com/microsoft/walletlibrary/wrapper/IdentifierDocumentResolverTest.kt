package com.microsoft.walletlibrary.wrapper

import com.microsoft.walletlibrary.did.sdk.LinkedDomainsService
import com.microsoft.walletlibrary.did.sdk.VerifiableCredentialSdk
import com.microsoft.walletlibrary.did.sdk.identifier.models.identifierdocument.IdentifierDocument
import com.microsoft.walletlibrary.did.sdk.util.controlflow.SdkException
import com.microsoft.walletlibrary.util.IdentifierDocumentResolutionException
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class IdentifierDocumentResolverTest {
    private val mockLinedDomainsService: LinkedDomainsService = mockk()
    private val mockIdentifierDocument: IdentifierDocument = mockk()

    init {
        mockkStatic(VerifiableCredentialSdk::class)
        every { VerifiableCredentialSdk.linkedDomainsService } returns mockLinedDomainsService
    }

    @Test
    fun resolveIdentifierDocument_SuccessfulFetch_ReturnsIdentifierDocument() {
        // Arrange
        coEvery { mockLinedDomainsService.resolveIdentifierDocument("") } returns Result.success(
            mockIdentifierDocument
        )

        runBlocking {
            // Act
            val actualResult = IdentifierDocumentResolver.resolveIdentifierDocument("")

            // Assert
            assertThat(actualResult).isEqualTo(mockIdentifierDocument)
        }
    }

    @Test
    fun resolveIdentifierDocument_FailedFetch_ThrowsException() {
        // Arrange
        coEvery { mockLinedDomainsService.resolveIdentifierDocument("") } returns Result.failure(
            SdkException()
        )

        runBlocking {
            // Act
            val actualResult = runCatching {
                IdentifierDocumentResolver.resolveIdentifierDocument("")
            }

            // Assert
            assertThat(actualResult.isFailure).isTrue
            assertThat(actualResult.exceptionOrNull()).isInstanceOf(
                IdentifierDocumentResolutionException::class.java
            )
        }
    }
}