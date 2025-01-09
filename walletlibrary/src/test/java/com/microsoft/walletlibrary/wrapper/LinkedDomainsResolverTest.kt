package com.microsoft.walletlibrary.wrapper

import com.microsoft.walletlibrary.did.sdk.LinkedDomainsService
import com.microsoft.walletlibrary.did.sdk.MockDidMetadata
import com.microsoft.walletlibrary.did.sdk.MockInjectedRootOfTrustResolver
import com.microsoft.walletlibrary.did.sdk.VerifiableCredentialSdk
import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainVerified
import com.microsoft.walletlibrary.did.sdk.credential.service.validators.JwtDomainLinkageCredentialValidator
import com.microsoft.walletlibrary.did.sdk.credential.service.validators.JwtValidator
import com.microsoft.walletlibrary.did.sdk.di.defaultTestSerializer
import com.microsoft.walletlibrary.did.sdk.identifier.models.identifierdocument.IdentifierDocument
import com.microsoft.walletlibrary.did.sdk.identifier.resolvers.Resolver
import com.microsoft.walletlibrary.did.sdk.util.controlflow.SdkException
import com.microsoft.walletlibrary.requests.RootOfTrust
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.junit.Test
import kotlin.Result as KotlinResult

class LinkedDomainsResolverTest {
    private val mockLinkedDomainsService: LinkedDomainsService = mockk()
    private val mockIdentifierDocument: IdentifierDocument = mockk()
    private val expectedDomain = "testdomain"

    init {
        mockkStatic(VerifiableCredentialSdk::class)
        every { VerifiableCredentialSdk.linkedDomainsService } returns mockLinkedDomainsService
    }

    @Test
    fun resolveRootOfTrust_VerifiedLinkedDomainExists_ReturnsRootOfTrustWithVerifiedDomain() {
        // Arrange
        coEvery { mockLinkedDomainsService.validateLinkedDomains(mockIdentifierDocument) } returns KotlinResult.success(
            LinkedDomainVerified(
                expectedDomain
            )
        )

        runBlocking {
            // Act
            val actualResult = LinkedDomainsResolver.resolve(mockIdentifierDocument)

            // Assert
            Assertions.assertThat(actualResult).isInstanceOf(RootOfTrust::class.java)
            Assertions.assertThat(actualResult.verified).isTrue
            Assertions.assertThat(actualResult.source).isEqualTo(expectedDomain)
        }
    }

    @Test
    fun resolveRootOfTrust_FailedWhileFetchingOrVerifying_ReturnsRootOfTrustWithUnverifiedEmptyDomain() {
        // Arrange
        coEvery { mockLinkedDomainsService.validateLinkedDomains(mockIdentifierDocument) } returns KotlinResult.failure(SdkException())

        runBlocking {
            // Act
            val actualResult = LinkedDomainsResolver.resolve(mockIdentifierDocument)

            // Assert
            Assertions.assertThat(actualResult).isInstanceOf(RootOfTrust::class.java)
            Assertions.assertThat(actualResult.verified).isFalse
            Assertions.assertThat(actualResult.source).isEqualTo("")
        }
    }

    @Test
    fun resolveRootOfTrustWithInjectedResolver_VerifiedLinkedDomainExists_ReturnsRootOfTrustWithVerifiedDomain() {
        // Arrange
        val mockedResolver: Resolver = mockk()
        val mockedJwtValidator: JwtValidator = mockk()
        val mockedJwtDomainLinkageCredentialValidator = JwtDomainLinkageCredentialValidator(mockedJwtValidator, defaultTestSerializer)
        val mockLinkedDomainsService: LinkedDomainsService =
            spyk(
                LinkedDomainsService(
                    mockk(relaxed = true),
                    mockedResolver,
                    mockedJwtDomainLinkageCredentialValidator,
                    MockInjectedRootOfTrustResolver()
                ), recordPrivateCalls = true
            )
        every { VerifiableCredentialSdk.linkedDomainsService } returns mockLinkedDomainsService
        val mockIdentifierDocument = IdentifierDocument(id = MockDidMetadata.VALID_DOMAIN_DID.value)

        runBlocking {
            // Act
            val actualResult = LinkedDomainsResolver.resolve(mockIdentifierDocument)

            // Assert
            Assertions.assertThat(actualResult).isInstanceOf(RootOfTrust::class.java)
            Assertions.assertThat(actualResult.verified).isTrue
            Assertions.assertThat(actualResult.source).isEqualTo("validDomain")
        }
    }

    @Test
    fun resolveRootOfTrustWithInjectedResolver_LinkedDomainDoesNotExist_ReturnsRootOfTrustWithMissingDomainFromWellKnown() {
        // Arrange
        val mockedResolver: Resolver = mockk()
        val mockedJwtValidator: JwtValidator = mockk()
        val mockedJwtDomainLinkageCredentialValidator = JwtDomainLinkageCredentialValidator(mockedJwtValidator, defaultTestSerializer)
        val mockLinkedDomainsService: LinkedDomainsService =
            spyk(
                LinkedDomainsService(
                    mockk(relaxed = true),
                    mockedResolver,
                    mockedJwtDomainLinkageCredentialValidator,
                    MockInjectedRootOfTrustResolver()
                ), recordPrivateCalls = true
            )
        every { VerifiableCredentialSdk.linkedDomainsService } returns mockLinkedDomainsService
        val mockIdentifierDocument = IdentifierDocument(id = MockDidMetadata.EMPTY_DOMAIN_DID.value)

        runBlocking {
            // Act
            val actualResult = LinkedDomainsResolver.resolve(mockIdentifierDocument)

            // Assert
            Assertions.assertThat(actualResult).isInstanceOf(RootOfTrust::class.java)
            Assertions.assertThat(actualResult.verified).isFalse
            Assertions.assertThat(actualResult.source).isEqualTo("")
        }
        coVerify { mockLinkedDomainsService["verifyLinkedDomainsUsingWellKnownDocument"](mockIdentifierDocument) }
    }

    @Test
    fun resolveRootOfTrustWithInjectedResolver_FailsUsesWellKnown_ReturnsRootOfTrustWithValidDomain() {
        // Arrange
        val mockedResolver: Resolver = mockk()
        val mockedJwtValidator: JwtValidator = mockk()
        val mockedJwtDomainLinkageCredentialValidator = JwtDomainLinkageCredentialValidator(mockedJwtValidator, defaultTestSerializer)
        val mockLinkedDomainsService: LinkedDomainsService =
            spyk(
                LinkedDomainsService(
                    mockk(relaxed = true),
                    mockedResolver,
                    mockedJwtDomainLinkageCredentialValidator,
                    MockInjectedRootOfTrustResolver()
                ), recordPrivateCalls = true
            )
        every { VerifiableCredentialSdk.linkedDomainsService } returns mockLinkedDomainsService
        val mockIdentifierDocument = IdentifierDocument(id = "failure")
        coEvery { mockLinkedDomainsService["verifyLinkedDomainsUsingWellKnownDocument"](mockIdentifierDocument) } returns LinkedDomainVerified(
            "testdomain"
        )

        runBlocking {
            // Act
            val actualResult = LinkedDomainsResolver.resolve(mockIdentifierDocument)

            // Assert
            Assertions.assertThat(actualResult).isInstanceOf(RootOfTrust::class.java)
            Assertions.assertThat(actualResult.verified).isTrue
            Assertions.assertThat(actualResult.source).isEqualTo("testdomain")
        }
        coVerify { mockLinkedDomainsService["verifyLinkedDomainsUsingWellKnownDocument"](mockIdentifierDocument) }
    }

    @Test
    fun resolveRootOfTrustWithInjectedResolver_FailsUsesWellKnownAndAlsoFails_ReturnsRootOfTrustWithMissingDomain() {
        // Arrange
        val mockedResolver: Resolver = mockk()
        val mockedJwtValidator: JwtValidator = mockk()
        val mockedJwtDomainLinkageCredentialValidator = JwtDomainLinkageCredentialValidator(mockedJwtValidator, defaultTestSerializer)
        val mockLinkedDomainsService: LinkedDomainsService =
            spyk(
                LinkedDomainsService(
                    mockk(relaxed = true),
                    mockedResolver,
                    mockedJwtDomainLinkageCredentialValidator,
                    MockInjectedRootOfTrustResolver()
                ), recordPrivateCalls = true
            )
        every { VerifiableCredentialSdk.linkedDomainsService } returns mockLinkedDomainsService
        val mockIdentifierDocument = IdentifierDocument(id = "failure")
        coEvery { mockLinkedDomainsService["verifyLinkedDomainsUsingWellKnownDocument"](mockIdentifierDocument) } throws Exception()

        runBlocking {
            // Act
            val actualResult = LinkedDomainsResolver.resolve(mockIdentifierDocument)

            // Assert
            Assertions.assertThat(actualResult).isInstanceOf(RootOfTrust::class.java)
            Assertions.assertThat(actualResult.verified).isFalse
            Assertions.assertThat(actualResult.source).isEqualTo("")
        }
        coVerify { mockLinkedDomainsService["verifyLinkedDomainsUsingWellKnownDocument"](mockIdentifierDocument) }
    }
}