package com.microsoft.walletlibrary.wrapper

import com.microsoft.walletlibrary.did.sdk.LinkedDomainsService
import com.microsoft.walletlibrary.did.sdk.VerifiableCredentialSdk
import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainVerified
import com.microsoft.walletlibrary.did.sdk.identifier.models.identifierdocument.IdentifierDocument
import com.microsoft.walletlibrary.did.sdk.util.controlflow.SdkException
import com.microsoft.walletlibrary.mappings.fetchAndVerifyLinkedDomains
import com.microsoft.walletlibrary.requests.RootOfTrust
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.junit.Test
import kotlin.Result as KotlinResult

class LinkedDomainsResolverTest {
    private val mockLinedDomainsService: LinkedDomainsService = mockk()
    private val mockIdentifierDocument: IdentifierDocument = mockk()
    private val expectedDomain = "testdomain"

    init {
        mockkStatic(VerifiableCredentialSdk::class)
        mockkStatic("com.microsoft.walletlibrary.mappings.LinkedDomainsServiceExtensionKt")
        every { VerifiableCredentialSdk.linkedDomainsService } returns mockLinedDomainsService
    }

    @Test
    fun resolveRootOfTrust_VerifiedLinkedDomainExists_ReturnsRootOfTrustWithVerifiedDomain() {
        // Arrange
        coEvery { mockLinedDomainsService.fetchAndVerifyLinkedDomains(mockIdentifierDocument) } returns KotlinResult.success(LinkedDomainVerified(expectedDomain))

        runBlocking {
            // Act
            val actualResult = LinkedDomainsResolver.resolveRootOfTrust(mockIdentifierDocument)

            // Assert
            Assertions.assertThat(actualResult).isInstanceOf(RootOfTrust::class.java)
            Assertions.assertThat(actualResult.verified).isTrue
            Assertions.assertThat(actualResult.source).isEqualTo(expectedDomain)
        }
    }

    @Test
    fun resolveRootOfTrust_FailedWhileFetchingOrVerifying_ReturnsRootOfTrustWithUnverifiedEmptyDomain() {
        // Arrange
        coEvery { mockLinedDomainsService.fetchAndVerifyLinkedDomains(mockIdentifierDocument) } returns KotlinResult.failure(SdkException())

        runBlocking {
            // Act
            val actualResult = LinkedDomainsResolver.resolveRootOfTrust(mockIdentifierDocument)

            // Assert
            Assertions.assertThat(actualResult).isInstanceOf(RootOfTrust::class.java)
            Assertions.assertThat(actualResult.verified).isFalse
            Assertions.assertThat(actualResult.source).isEqualTo("")
        }
    }
}