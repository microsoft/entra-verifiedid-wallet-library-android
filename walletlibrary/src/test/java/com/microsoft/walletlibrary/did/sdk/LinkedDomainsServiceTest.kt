// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.did.sdk

import com.microsoft.walletlibrary.WalletLibraryFlight
import com.microsoft.walletlibrary.WalletLibraryFlightProvider
import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainMissing
import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainUnVerified
import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainVerified
import com.microsoft.walletlibrary.did.sdk.credential.service.models.serviceResponses.LinkedDomainsResponse
import com.microsoft.walletlibrary.did.sdk.credential.service.validators.DomainLinkageCredentialValidator
import com.microsoft.walletlibrary.did.sdk.credential.service.validators.DomainLinkageCredentialValidationResult
import com.microsoft.walletlibrary.did.sdk.credential.service.validators.JwtDomainLinkageCredentialValidator
import com.microsoft.walletlibrary.did.sdk.credential.service.validators.JwtValidator
import com.microsoft.walletlibrary.did.sdk.di.defaultTestSerializer
import com.microsoft.walletlibrary.did.sdk.identifier.models.identifierdocument.DidMetadata
import com.microsoft.walletlibrary.did.sdk.identifier.models.identifierdocument.IdentifierDocument
import com.microsoft.walletlibrary.did.sdk.identifier.models.identifierdocument.IdentifierResponse
import com.microsoft.walletlibrary.did.sdk.identifier.models.payload.document.IdentifierDocumentService
import com.microsoft.walletlibrary.did.sdk.identifier.resolvers.MockRootOfTrustResolver
import com.microsoft.walletlibrary.did.sdk.identifier.resolvers.Resolver
import com.microsoft.walletlibrary.did.sdk.util.Constants
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import kotlin.Result as KotlinResult

class LinkedDomainsServiceTest {
    private val linkedDomainHardeningFlightProvider = WalletLibraryFlightProvider { flight ->
        flight == WalletLibraryFlight.LinkedDomainValidationHardening
    }
    private val mockedResolver: Resolver = mockk()
    private val mockedJwtValidator: JwtValidator = mockk()
    private val mockedJwtDomainLinkageCredentialValidator: JwtDomainLinkageCredentialValidator =
        JwtDomainLinkageCredentialValidator(mockedJwtValidator, defaultTestSerializer)
    private val linkedDomainsService: LinkedDomainsService =
        spyk(LinkedDomainsService(mockk(relaxed = true), mockedResolver, mockedJwtDomainLinkageCredentialValidator))

    @Test
    fun `injected root of trust resolver returns valid linked domain result`() {
        val linkedDomainsService: LinkedDomainsService =
            spyk(
                LinkedDomainsService(
                    mockk(relaxed = true),
                    mockedResolver,
                    mockedJwtDomainLinkageCredentialValidator,
                    MockInjectedRootOfTrustResolver()
                )
            )
        val mockIdentifierDocument = IdentifierDocument(id = MockDidMetadata.VALID_DOMAIN_DID.value)

        runBlocking {
            val linkedDomainsResult = linkedDomainsService.validateLinkedDomains(mockIdentifierDocument)
            assertThat(linkedDomainsResult.isSuccess).isTrue
            assertThat(linkedDomainsResult.getOrNull()).isInstanceOf(LinkedDomainVerified::class.java)
            assertThat((linkedDomainsResult.getOrNull() as LinkedDomainVerified).domainUrl).isEqualTo("validDomain")
        }
    }

    @Test
    fun `injected root of trust resolver returns empty domain and well-known config is not setup returns missing linked domain result`() {
        val linkedDomainsService: LinkedDomainsService =
            spyk(
                LinkedDomainsService(
                    mockk(relaxed = true),
                    mockedResolver,
                    mockedJwtDomainLinkageCredentialValidator,
                    MockInjectedRootOfTrustResolver()
                ), recordPrivateCalls = true
            )
        val mockIdentifierDocument = IdentifierDocument(id = MockDidMetadata.EMPTY_DOMAIN_DID.value)

        runBlocking {
            val linkedDomainsResult = linkedDomainsService.validateLinkedDomains(mockIdentifierDocument)
            assertThat(linkedDomainsResult.isSuccess).isTrue
            assertThat(linkedDomainsResult.getOrNull()).isInstanceOf(LinkedDomainMissing::class.java)
        }
        coVerify { linkedDomainsService["verifyLinkedDomainsUsingWellKnownDocument"](mockIdentifierDocument) }
    }

    @Test
    fun `injected root of trust resolver fails and well-known config is not setup returns missing linked domain result`() {
        val linkedDomainsService: LinkedDomainsService =
            spyk(
                LinkedDomainsService(
                    mockk(relaxed = true),
                    mockedResolver,
                    mockedJwtDomainLinkageCredentialValidator,
                    MockInjectedRootOfTrustResolver()
                ), recordPrivateCalls = true
            )
        val mockIdentifierDocument = IdentifierDocument(id = "failure")

        runBlocking {
            val linkedDomainsResult = linkedDomainsService.validateLinkedDomains(mockIdentifierDocument)
            assertThat(linkedDomainsResult.isSuccess).isTrue
            assertThat(linkedDomainsResult.getOrNull()).isInstanceOf(LinkedDomainMissing::class.java)
        }
        coVerify { linkedDomainsService["verifyLinkedDomainsUsingWellKnownDocument"](mockIdentifierDocument) }
    }

    @Test
    fun `injected root of trust resolver fails or returns empty and well-known config returns valid linked domain`() {
        val linkedDomainsService: LinkedDomainsService =
            spyk(
                LinkedDomainsService(
                    mockk(relaxed = true),
                    mockedResolver,
                    mockedJwtDomainLinkageCredentialValidator,
                    MockInjectedRootOfTrustResolver()
                ), recordPrivateCalls = true
            )
        val mockIdentifierDocument = IdentifierDocument(id = "failure")
        coEvery {
            linkedDomainsService["verifyLinkedDomainsUsingWellKnownDocument"](mockIdentifierDocument)
        } returns LinkedDomainValidationAttempt(
            result = LinkedDomainVerified("testdomain"),
            source = LinkedDomainValidationSource.WELL_KNOWN,
            failureStage = LinkedDomainValidationFailureStage.NONE
        )

        runBlocking {
            val linkedDomainsResult = linkedDomainsService.validateLinkedDomains(mockIdentifierDocument)
            assertThat(linkedDomainsResult.isSuccess).isTrue
            assertThat(linkedDomainsResult.getOrNull()).isInstanceOf(LinkedDomainVerified::class.java)
            assertThat((linkedDomainsResult.getOrNull() as LinkedDomainVerified).domainUrl).isEqualTo("testdomain")
        }
        coVerify { linkedDomainsService["verifyLinkedDomainsUsingWellKnownDocument"](mockIdentifierDocument) }
    }

    @Test
    fun `injected root of trust resolver fails or returns empty and well-known config returns invalid linked domain`() {
        val linkedDomainsService: LinkedDomainsService =
            spyk(
                LinkedDomainsService(
                    mockk(relaxed = true),
                    mockedResolver,
                    mockedJwtDomainLinkageCredentialValidator,
                    MockInjectedRootOfTrustResolver()
                ), recordPrivateCalls = true
            )
        val mockIdentifierDocument = IdentifierDocument(id = "failure")
        coEvery {
            linkedDomainsService["verifyLinkedDomainsUsingWellKnownDocument"](mockIdentifierDocument)
        } returns LinkedDomainValidationAttempt(
            result = LinkedDomainUnVerified("testdomain"),
            source = LinkedDomainValidationSource.WELL_KNOWN,
            failureStage = LinkedDomainValidationFailureStage.CREDENTIAL_INVALID
        )

        runBlocking {
            val linkedDomainsResult = linkedDomainsService.validateLinkedDomains(mockIdentifierDocument)
            assertThat(linkedDomainsResult.isSuccess).isTrue
            assertThat(linkedDomainsResult.getOrNull()).isInstanceOf(LinkedDomainUnVerified::class.java)
            assertThat((linkedDomainsResult.getOrNull() as LinkedDomainUnVerified).domainUrl).isEqualTo("testdomain")
        }
        coVerify { linkedDomainsService["verifyLinkedDomainsUsingWellKnownDocument"](mockIdentifierDocument) }
    }

    @Test
    fun `injected root of trust resolver fails or returns empty and well-known config fails returns missing Linked Domain Result`() {
        val linkedDomainsService: LinkedDomainsService =
            spyk(
                LinkedDomainsService(
                    mockk(relaxed = true),
                    mockedResolver,
                    mockedJwtDomainLinkageCredentialValidator,
                    MockInjectedRootOfTrustResolver()
                ), recordPrivateCalls = true
            )
        val mockIdentifierDocument = IdentifierDocument(id = "failure")
        coEvery { linkedDomainsService["verifyLinkedDomainsUsingWellKnownDocument"](mockIdentifierDocument) } throws Exception()

        runBlocking {
            val linkedDomainsResult = linkedDomainsService.validateLinkedDomains(mockIdentifierDocument)
            assertThat(linkedDomainsResult.isSuccess).isTrue
            assertThat(linkedDomainsResult.getOrNull()).isInstanceOf(LinkedDomainMissing::class.java)
        }
        coVerify { linkedDomainsService["verifyLinkedDomainsUsingWellKnownDocument"](mockIdentifierDocument) }
    }

    @Test
    fun `trusted resolver emits one bounded terminal validation event`() {
        mockkObject(com.microsoft.walletlibrary.did.sdk.util.log.SdkLog)
        every {
            com.microsoft.walletlibrary.did.sdk.util.log.SdkLog.event(any(), any())
        } returns Unit
        val service = LinkedDomainsService(
            mockk(relaxed = true),
            mockedResolver,
            mockedJwtDomainLinkageCredentialValidator,
            MockInjectedRootOfTrustResolver()
        )

        try {
            runBlocking {
                service.validateLinkedDomains(
                    IdentifierDocument(id = MockDidMetadata.VALID_DOMAIN_DID.value)
                )
            }

            verify(exactly = 1) {
                com.microsoft.walletlibrary.did.sdk.util.log.SdkLog.event(
                    "LinkedDomainValidation",
                    mapOf(
                        "validation_mode" to "legacy",
                        "flow" to "unknown",
                        "source" to "trusted_resolver",
                        "outcome" to "verified",
                        "failure_stage" to "none",
                        "endpoint_count_bucket" to "unknown",
                        "credential_count_bucket" to "unknown",
                        "http_status_class" to "not_applicable"
                    )
                )
            }
        } finally {
            unmockkObject(com.microsoft.walletlibrary.did.sdk.util.log.SdkLog)
        }
    }

    @Test
    fun `invalid endpoint emits one bounded terminal validation event`() {
        mockkObject(com.microsoft.walletlibrary.did.sdk.util.log.SdkLog)
        every {
            com.microsoft.walletlibrary.did.sdk.util.log.SdkLog.event(any(), any())
        } returns Unit
        every {
            com.microsoft.walletlibrary.did.sdk.util.log.SdkLog.w(any(), any(), any())
        } returns Unit
        val service = LinkedDomainsService(
            mockk(relaxed = true),
            mockedResolver,
            mockedJwtDomainLinkageCredentialValidator,
            flightProvider = linkedDomainHardeningFlightProvider
        )
        val identifierDocument = IdentifierDocument(id = "did:example:123").apply {
            this.service = listOf(
                IdentifierDocumentService(
                    id = "#linked-domain",
                    type = Constants.LINKED_DOMAINS_SERVICE_ENDPOINT_TYPE,
                    serviceEndpoint = listOf("https://example.com/not-an-origin")
                )
            )
        }

        try {
            runBlocking {
                service.validateLinkedDomains(identifierDocument)
            }

            verify(exactly = 1) {
                com.microsoft.walletlibrary.did.sdk.util.log.SdkLog.event(
                    "LinkedDomainValidation",
                    mapOf(
                        "validation_mode" to "hardened",
                        "flow" to "unknown",
                        "source" to "well_known",
                        "outcome" to "unverified",
                        "failure_stage" to "endpoint_invalid",
                        "endpoint_count_bucket" to "one",
                        "credential_count_bucket" to "unknown",
                        "http_status_class" to "not_applicable"
                    )
                )
            }
        } finally {
            unmockkObject(com.microsoft.walletlibrary.did.sdk.util.log.SdkLog)
        }
    }

    @Test
    fun `presentation origin mismatch emits specific failure stage and flow`() {
        mockkObject(com.microsoft.walletlibrary.did.sdk.util.log.SdkLog)
        every {
            com.microsoft.walletlibrary.did.sdk.util.log.SdkLog.event(any(), any())
        } returns Unit
        every {
            com.microsoft.walletlibrary.did.sdk.util.log.SdkLog.w(any(), any(), any())
        } returns Unit
        val validator: DomainLinkageCredentialValidator = mockk()
        val service = spyk(
            LinkedDomainsService(
                mockk(relaxed = true),
                mockedResolver,
                validator,
                flightProvider = linkedDomainHardeningFlightProvider
            ),
            recordPrivateCalls = true
        )
        val identifierDocument = IdentifierDocument(id = "did:example:verifier").apply {
            this.service = listOf(
                IdentifierDocumentService(
                    id = "#linked-domain",
                    type = Constants.LINKED_DOMAINS_SERVICE_ENDPOINT_TYPE,
                    serviceEndpoint = listOf("https://example.com")
                )
            )
        }
        coEvery {
            service["getWellKnownConfigDocument"]("https://example.com")
        } returns KotlinResult.success(LinkedDomainsResponse("", listOf("linked-domain-jwt")))
        coEvery {
            validator.validate(
                "linked-domain-jwt",
                "did:example:verifier",
                "https://example.com"
            )
        } returns DomainLinkageCredentialValidationResult.ORIGIN_MISMATCH

        try {
            runBlocking {
                service.validateLinkedDomains(
                    identifierDocument,
                    LinkedDomainValidationFlow.PRESENTATION
                )
            }

            verify(exactly = 1) {
                com.microsoft.walletlibrary.did.sdk.util.log.SdkLog.event(
                    "LinkedDomainValidation",
                    mapOf(
                        "validation_mode" to "hardened",
                        "flow" to "presentation",
                        "source" to "well_known",
                        "outcome" to "unverified",
                        "failure_stage" to "origin_mismatch",
                        "endpoint_count_bucket" to "one",
                        "credential_count_bucket" to "one",
                        "http_status_class" to "2xx"
                    )
                )
            }
        } finally {
            unmockkObject(com.microsoft.walletlibrary.did.sdk.util.log.SdkLog)
        }
    }

    @Test
    fun `test linked domains with single domain as string successfully`() {
        // Arrange
        val suppliedDidWithSingleServiceEndpoint =
            "did:ion:EiA8HR28m5KUig9elPRXkmKvvBGXcOoxpUrCscTdGJcIXQ?-ion-initial-state=eyJkZWx0YV9oYXNoIjoiRWlDVD" +
            "V5MG5nNTJCNzVjYWFqWU9qVjBRMmpxSng0NDZSajhRTjFpaHdteUpJZyIsInJlY292ZXJ5X2NvbW1pdG1lbnQiOiJFaURsOER4eX" +
            "ZLa3lvRmJsUWp0OXllU2J3TXkwR083MFM1R2FUU1F0UlF0aFJRIn0.eyJ1cGRhdGVfY29tbWl0bWVudCI6IkVpRGFXS2sycjJiSW" +
            "JsRWFpRUhtRU5Kc2h6czhtY1hJd2hTV0Z1YmtWQlJ3WWciLCJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbn" +
            "QiOnsicHVibGljX2tleXMiOlt7ImlkIjoic2lnX2VkMmM1ZWRmIiwidHlwZSI6IkVjZHNhU2VjcDI1NmsxVmVyaWZpY2F0aW9uS2" +
            "V5MjAxOSIsImp3ayI6eyJrdHkiOiJFQyIsImNydiI6InNlY3AyNTZrMSIsIngiOiJoTzhYaXhtWUxNOVVVMmFWOW9kc2VsSDNobD" +
            "JtbVFPLS1GTzNKa2JrekVrIiwieSI6InBDWEpxbXpUbzVQQkdRTERibnRtdUFaSElZQnFZOG1DZVdkaWhpb0tGUmMifSwicHVycG" +
            "9zZSI6WyJhdXRoIiwiZ2VuZXJhbCJdfV19fV19"
        val expectedResponseString =
            "{\"@context\":\"https://www.w3.org/ns/did-resolution/v1\",\"didDocument\":{\"id\":\"did:ion:EiA8HR28m5KUig9el" +
            "PRXkmKvvBGXcOoxpUrCscTdGJcIXQ?-ion-initial-state=eyJkZWx0YV9oYXNoIjoiRWlDVDV5MG5nNTJCNzVjYWFqWU9qVjB" +
            "RMmpxSng0NDZSajhRTjFpaHdteUpJZyIsInJlY292ZXJ5X2NvbW1pdG1lbnQiOiJFaURsOER4eXZLa3lvRmJsUWp0OXllU2J3TXk" +
            "wR083MFM1R2FUU1F0UlF0aFJRIn0.eyJ1cGRhdGVfY29tbWl0bWVudCI6IkVpRGFXS2sycjJiSWJsRWFpRUhtRU5Kc2h6czhtY1h" +
            "Jd2hTV0Z1YmtWQlJ3WWciLCJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljX2tleXMiOlt" +
            "7ImlkIjoic2lnX2VkMmM1ZWRmIiwidHlwZSI6IkVjZHNhU2VjcDI1NmsxVmVyaWZpY2F0aW9uS2V5MjAxOSIsImp3ayI6eyJrdHk" +
            "iOiJFQyIsImNydiI6InNlY3AyNTZrMSIsIngiOiJoTzhYaXhtWUxNOVVVMmFWOW9kc2VsSDNobDJtbVFPLS1GTzNKa2JrekVrIiw" +
            "ieSI6InBDWEpxbXpUbzVQQkdRTERibnRtdUFaSElZQnFZOG1DZVdkaWhpb0tGUmMifSwicHVycG9zZSI6WyJhdXRoIiwiZ2VuZXJ" +
            "hbCJdfV19fV19\",\"@context\":[\"https://www.w3.org/ns/did/v1\",{\"@base\":\"did:ion:EiA8HR28m5KUig9elPRXkmKv" +
            "vBGXcOoxpUrCscTdGJcIXQ?-ion-initial-state=eyJkZWx0YV9oYXNoIjoiRWlDVDV5MG5nNTJCNzVjYWFqWU9qVjBRMmpxSn" +
            "g0NDZSajhRTjFpaHdteUpJZyIsInJlY292ZXJ5X2NvbW1pdG1lbnQiOiJFaURsOER4eXZLa3lvRmJsUWp0OXllU2J3TXkwR083MF" +
            "M1R2FUU1F0UlF0aFJRIn0.eyJ1cGRhdGVfY29tbWl0bWVudCI6IkVpRGFXS2sycjJiSWJsRWFpRUhtRU5Kc2h6czhtY1hJd2hTV0" +
            "Z1YmtWQlJ3WWciLCJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljX2tleXMiOlt7ImlkIj" +
            "oic2lnX2VkMmM1ZWRmIiwidHlwZSI6IkVjZHNhU2VjcDI1NmsxVmVyaWZpY2F0aW9uS2V5MjAxOSIsImp3ayI6eyJrdHkiOiJFQy" +
            "IsImNydiI6InNlY3AyNTZrMSIsIngiOiJoTzhYaXhtWUxNOVVVMmFWOW9kc2VsSDNobDJtbVFPLS1GTzNKa2JrekVrIiwieSI6In" +
            "BDWEpxbXpUbzVQQkdRTERibnRtdUFaSElZQnFZOG1DZVdkaWhpb0tGUmMifSwicHVycG9zZSI6WyJhdXRoIiwiZ2VuZXJhbCJdfV" +
            "19fV19\"}],\"service\":[{\"id\":\"#linkeddomains\",\"type\":\"LinkedDomains\",\"serviceEndpoint\":\"https://issuer" +
            "testng.com\"}],\"verificationMethod\":[{\"id\":\"#sig_ed2c5edf\",\"controller\":\"\",\"type\":\"EcdsaSecp256k1Veri" +
            "ficationKey2019\",\"publicKeyJwk\":{\"kty\":\"EC\",\"crv\":\"secp256k1\",\"x\":\"hO8XixmYLM9UU2aV9odselH3hl2mmQO--" +
            "FO3JkbkzEk\",\"y\":\"pCXJqmzTo5PBGQLDbntmuAZHIYBqY8mCeWdihioKFRc\"}}],\"authentication\":[\"#sig_ed2c5edf\"]}" +
            ",\"methodMetadata\":{\"published\":false,\"recoveryCommitment\":\"EiDl8DxyvKkyoFblQjt9yeSbwMy0GO70S5GaTSQtR" +
            "QthRQ\",\"updateCommitment\":\"EiDaWKk2r2bIblEaiEHmENJshzs8mcXIwhSWFubkVBRwYg\"},\"resolverMetadata\":{\"dri" +
            "verId\":\"did:ion\",\"driver\":\"HttpDriver\",\"retrieved\":\"2020-10-25T06:39:09.486Z\",\"duration\":\"117.3589ms" +
            "\"}}"
        val expectedResponse = defaultTestSerializer.decodeFromString(IdentifierResponse.serializer(), expectedResponseString)
        val expectedWellKnownConfigDocumentResponse =
            "{\"@context\":\"https://identity.foundation/.well-known/contexts/did-configuration-v0.0.jsonld\",\"linked" +
            "_dids\":[\"eyJhbGciOiJFUzI1NksiLCJraWQiOiJkaWQ6aW9uOkVpQThIUjI4bTVLVWlnOWVsUFJYa21LdnZCR1hjT294cFVyQ3N" +
            "jVGRHSmNJWFE_LWlvbi1pbml0aWFsLXN0YXRlPWV5SmtaV3gwWVY5b1lYTm9Jam9pUldsRFZEVjVNRzVuTlRKQ056VmpZV0ZxV1U" +
            "5cVZqQlJNbXB4U25nME5EWlNhamhSVGpGcGFIZHRlVXBKWnlJc0luSmxZMjkyWlhKNVgyTnZiVzFwZEcxbGJuUWlPaUpGYVVSc09" +
            "FUjRlWFpMYTNsdlJtSnNVV3AwT1hsbFUySjNUWGt3UjA4M01GTTFSMkZVVTFGMFVsRjBhRkpSSW4wLmV5SjFjR1JoZEdWZlkyOXR" +
            "iV2wwYldWdWRDSTZJa1ZwUkdGWFMyc3ljakppU1dKc1JXRnBSVWh0UlU1S2MyaDZjemh0WTFoSmQyaFRWMFoxWW10V1FsSjNXV2N" +
            "pTENKd1lYUmphR1Z6SWpwYmV5SmhZM1JwYjI0aU9pSnlaWEJzWVdObElpd2laRzlqZFcxbGJuUWlPbnNpY0hWaWJHbGpYMnRsZVh" +
            "NaU9sdDdJbWxrSWpvaWMybG5YMlZrTW1NMVpXUm1JaXdpZEhsd1pTSTZJa1ZqWkhOaFUyVmpjREkxTm1zeFZtVnlhV1pwWTJGMGF" +
            "XOXVTMlY1TWpBeE9TSXNJbXAzYXlJNmV5SnJkSGtpT2lKRlF5SXNJbU55ZGlJNkluTmxZM0F5TlRack1TSXNJbmdpT2lKb1R6aFl" +
            "hWGh0V1V4Tk9WVlZNbUZXT1c5a2MyVnNTRE5vYkRKdGJWRlBMUzFHVHpOS2EySnJla1ZySWl3aWVTSTZJbkJEV0VweGJYcFVielZ" +
            "RUWtkUlRFUmliblJ0ZFVGYVNFbFpRbkZaT0cxRFpWZGthV2hwYjB0R1VtTWlmU3dpY0hWeWNHOXpaU0k2V3lKaGRYUm9JaXdpWjJ" +
            "WdVpYSmhiQ0pkZlYxOWZWMTkjc2lnX2VkMmM1ZWRmIn0.eyJzdWIiOiJkaWQ6aW9uOkVpQThIUjI4bTVLVWlnOWVsUFJYa21LdnZ" +
            "CR1hjT294cFVyQ3NjVGRHSmNJWFE_LWlvbi1pbml0aWFsLXN0YXRlPWV5SmtaV3gwWVY5b1lYTm9Jam9pUldsRFZEVjVNRzVuTlR" +
            "KQ056VmpZV0ZxV1U5cVZqQlJNbXB4U25nME5EWlNhamhSVGpGcGFIZHRlVXBKWnlJc0luSmxZMjkyWlhKNVgyTnZiVzFwZEcxbGJ" +
            "uUWlPaUpGYVVSc09FUjRlWFpMYTNsdlJtSnNVV3AwT1hsbFUySjNUWGt3UjA4M01GTTFSMkZVVTFGMFVsRjBhRkpSSW4wLmV5SjF" +
            "jR1JoZEdWZlkyOXRiV2wwYldWdWRDSTZJa1ZwUkdGWFMyc3ljakppU1dKc1JXRnBSVWh0UlU1S2MyaDZjemh0WTFoSmQyaFRWMFo" +
            "xWW10V1FsSjNXV2NpTENKd1lYUmphR1Z6SWpwYmV5SmhZM1JwYjI0aU9pSnlaWEJzWVdObElpd2laRzlqZFcxbGJuUWlPbnNpY0h" +
            "WaWJHbGpYMnRsZVhNaU9sdDdJbWxrSWpvaWMybG5YMlZrTW1NMVpXUm1JaXdpZEhsd1pTSTZJa1ZqWkhOaFUyVmpjREkxTm1zeFZ" +
            "tVnlhV1pwWTJGMGFXOXVTMlY1TWpBeE9TSXNJbXAzYXlJNmV5SnJkSGtpT2lKRlF5SXNJbU55ZGlJNkluTmxZM0F5TlRack1TSXN" +
            "JbmdpT2lKb1R6aFlhWGh0V1V4Tk9WVlZNbUZXT1c5a2MyVnNTRE5vYkRKdGJWRlBMUzFHVHpOS2EySnJla1ZySWl3aWVTSTZJbkJ" +
            "EV0VweGJYcFVielZRUWtkUlRFUmliblJ0ZFVGYVNFbFpRbkZaT0cxRFpWZGthV2hwYjB0R1VtTWlmU3dpY0hWeWNHOXpaU0k2V3l" +
            "KaGRYUm9JaXdpWjJWdVpYSmhiQ0pkZlYxOWZWMTkiLCJpc3MiOiJkaWQ6aW9uOkVpQThIUjI4bTVLVWlnOWVsUFJYa21LdnZCR1h" +
            "jT294cFVyQ3NjVGRHSmNJWFE_LWlvbi1pbml0aWFsLXN0YXRlPWV5SmtaV3gwWVY5b1lYTm9Jam9pUldsRFZEVjVNRzVuTlRKQ05" +
            "6VmpZV0ZxV1U5cVZqQlJNbXB4U25nME5EWlNhamhSVGpGcGFIZHRlVXBKWnlJc0luSmxZMjkyWlhKNVgyTnZiVzFwZEcxbGJuUWl" +
            "PaUpGYVVSc09FUjRlWFpMYTNsdlJtSnNVV3AwT1hsbFUySjNUWGt3UjA4M01GTTFSMkZVVTFGMFVsRjBhRkpSSW4wLmV5SjFjR1J" +
            "oZEdWZlkyOXRiV2wwYldWdWRDSTZJa1ZwUkdGWFMyc3ljakppU1dKc1JXRnBSVWh0UlU1S2MyaDZjemh0WTFoSmQyaFRWMFoxWW1" +
            "0V1FsSjNXV2NpTENKd1lYUmphR1Z6SWpwYmV5SmhZM1JwYjI0aU9pSnlaWEJzWVdObElpd2laRzlqZFcxbGJuUWlPbnNpY0hWaWJ" +
            "HbGpYMnRsZVhNaU9sdDdJbWxrSWpvaWMybG5YMlZrTW1NMVpXUm1JaXdpZEhsd1pTSTZJa1ZqWkhOaFUyVmpjREkxTm1zeFZtVnl" +
            "hV1pwWTJGMGFXOXVTMlY1TWpBeE9TSXNJbXAzYXlJNmV5SnJkSGtpT2lKRlF5SXNJbU55ZGlJNkluTmxZM0F5TlRack1TSXNJbmd" +
            "pT2lKb1R6aFlhWGh0V1V4Tk9WVlZNbUZXT1c5a2MyVnNTRE5vYkRKdGJWRlBMUzFHVHpOS2EySnJla1ZySWl3aWVTSTZJbkJEV0V" +
            "weGJYcFVielZRUWtkUlRFUmliblJ0ZFVGYVNFbFpRbkZaT0cxRFpWZGthV2hwYjB0R1VtTWlmU3dpY0hWeWNHOXpaU0k2V3lKaGR" +
            "YUm9JaXdpWjJWdVpYSmhiQ0pkZlYxOWZWMTkiLCJuYmYiOjE2MDM0MTU2NjQsImV4cCI6MjM5MjMzNDA2NCwidmMiOnsiQGNvbnR" +
            "leHQiOlsiaHR0cHM6Ly93d3cudzMub3JnLzIwMTgvY3JlZGVudGlhbHMvdjEiLCJodHRwczovL2lkZW50aXR5LmZvdW5kYXRpb24" +
            "vLndlbGwta25vd24vY29udGV4dHMvZGlkLWNvbmZpZ3VyYXRpb24tdjAuMC5qc29ubGQiXSwiaXNzdWVyIjoiZGlkOmlvbjpFaUE" +
            "4SFIyOG01S1VpZzllbFBSWGttS3Z2QkdYY09veHBVckNzY1RkR0pjSVhRPy1pb24taW5pdGlhbC1zdGF0ZT1leUprWld4MFlWOW9" +
            "ZWE5vSWpvaVJXbERWRFY1TUc1bk5USkNOelZqWVdGcVdVOXFWakJSTW1weFNuZzBORFpTYWpoUlRqRnBhSGR0ZVVwSlp5SXNJbkp" +
            "sWTI5MlpYSjVYMk52YlcxcGRHMWxiblFpT2lKRmFVUnNPRVI0ZVhaTGEzbHZSbUpzVVdwME9YbGxVMkozVFhrd1IwODNNRk0xUjJ" +
            "GVVUxRjBVbEYwYUZKUkluMC5leUoxY0dSaGRHVmZZMjl0YldsMGJXVnVkQ0k2SWtWcFJHRlhTMnN5Y2pKaVNXSnNSV0ZwUlVodFJ" +
            "VNUtjMmg2Y3podFkxaEpkMmhUVjBaMVltdFdRbEozV1djaUxDSndZWFJqYUdWeklqcGJleUpoWTNScGIyNGlPaUp5WlhCc1lXTmx" +
            "JaXdpWkc5amRXMWxiblFpT25zaWNIVmliR2xqWDJ0bGVYTWlPbHQ3SW1sa0lqb2ljMmxuWDJWa01tTTFaV1JtSWl3aWRIbHdaU0k" +
            "2SWtWalpITmhVMlZqY0RJMU5tc3hWbVZ5YVdacFkyRjBhVzl1UzJWNU1qQXhPU0lzSW1wM2F5STZleUpyZEhraU9pSkZReUlzSW1" +
            "OeWRpSTZJbk5sWTNBeU5UWnJNU0lzSW5naU9pSm9UemhZYVhodFdVeE5PVlZWTW1GV09XOWtjMlZzU0ROb2JESnRiVkZQTFMxR1R" +
            "6TkthMkpyZWtWcklpd2llU0k2SW5CRFdFcHhiWHBVYnpWUVFrZFJURVJpYm5SdGRVRmFTRWxaUW5GWk9HMURaVmRrYVdocGIwdEd" +
            "VbU1pZlN3aWNIVnljRzl6WlNJNld5SmhkWFJvSWl3aVoyVnVaWEpoYkNKZGZWMTlmVjE5IiwiaXNzdWFuY2VEYXRlIjoiMjAyMC0" +
            "xMC0yM1QwMToxNDoyNC43NzRaIiwiZXhwaXJhdGlvbkRhdGUiOiIyMDQ1LTEwLTIzVDAxOjE0OjI0Ljc3NFoiLCJ0eXBlIjpbIlZ" +
            "lcmlmaWFibGVDcmVkZW50aWFsIiwiRG9tYWluTGlua2FnZUNyZWRlbnRpYWwiXSwiY3JlZGVudGlhbFN1YmplY3QiOnsiaWQiOiJ" +
            "kaWQ6aW9uOkVpQThIUjI4bTVLVWlnOWVsUFJYa21LdnZCR1hjT294cFVyQ3NjVGRHSmNJWFE_LWlvbi1pbml0aWFsLXN0YXRlPWV" +
            "5SmtaV3gwWVY5b1lYTm9Jam9pUldsRFZEVjVNRzVuTlRKQ056VmpZV0ZxV1U5cVZqQlJNbXB4U25nME5EWlNhamhSVGpGcGFIZHR" +
            "lVXBKWnlJc0luSmxZMjkyWlhKNVgyTnZiVzFwZEcxbGJuUWlPaUpGYVVSc09FUjRlWFpMYTNsdlJtSnNVV3AwT1hsbFUySjNUWGt" +
            "3UjA4M01GTTFSMkZVVTFGMFVsRjBhRkpSSW4wLmV5SjFjR1JoZEdWZlkyOXRiV2wwYldWdWRDSTZJa1ZwUkdGWFMyc3ljakppU1d" +
            "Kc1JXRnBSVWh0UlU1S2MyaDZjemh0WTFoSmQyaFRWMFoxWW10V1FsSjNXV2NpTENKd1lYUmphR1Z6SWpwYmV5SmhZM1JwYjI0aU9" +
            "pSnlaWEJzWVdObElpd2laRzlqZFcxbGJuUWlPbnNpY0hWaWJHbGpYMnRsZVhNaU9sdDdJbWxrSWpvaWMybG5YMlZrTW1NMVpXUm1" +
            "JaXdpZEhsd1pTSTZJa1ZqWkhOaFUyVmpjREkxTm1zeFZtVnlhV1pwWTJGMGFXOXVTMlY1TWpBeE9TSXNJbXAzYXlJNmV5SnJkSGt" +
            "pT2lKRlF5SXNJbU55ZGlJNkluTmxZM0F5TlRack1TSXNJbmdpT2lKb1R6aFlhWGh0V1V4Tk9WVlZNbUZXT1c5a2MyVnNTRE5vYkR" +
            "KdGJWRlBMUzFHVHpOS2EySnJla1ZySWl3aWVTSTZJbkJEV0VweGJYcFVielZRUWtkUlRFUmliblJ0ZFVGYVNFbFpRbkZaT0cxRFp" +
            "WZGthV2hwYjB0R1VtTWlmU3dpY0hWeWNHOXpaU0k2V3lKaGRYUm9JaXdpWjJWdVpYSmhiQ0pkZlYxOWZWMTkiLCJvcmlnaW4iOiJ" +
            "odHRwczovL2lzc3VlcnRlc3RuZy5jb20ifX19.luP4vIE30u_kj5CINDhbjZcsHDbEEXS5hMFnzd7qVKs2OtseDJlYcgLOgbCqI1" +
            "7v3edXx9OVkErQ-sSuIGvt8g\"]}"
        val expectedWellKnownConfigDocument =
            defaultTestSerializer.decodeFromString(LinkedDomainsResponse.serializer(), expectedWellKnownConfigDocumentResponse)
        val expectedDomainUrl = "https://issuertestng.com"
        coEvery { linkedDomainsService.resolveIdentifierDocument(suppliedDidWithSingleServiceEndpoint) } returns KotlinResult.success(
            expectedResponse.didDocument
        )
        coEvery { linkedDomainsService["getWellKnownConfigDocument"](expectedDomainUrl) } returns KotlinResult.success(
            expectedWellKnownConfigDocument
        )
        coEvery { mockedJwtValidator.verifySignature(any()) } returns true
        coEvery { mockedJwtValidator.validateDidInHeaderAndPayload(any(), any()) } returns true

        // Act and Assert
        runBlocking {
            val actualLinkedDomainsResultResponse =
                linkedDomainsService.fetchDocumentAndVerifyLinkedDomains(suppliedDidWithSingleServiceEndpoint)
            assertThat(actualLinkedDomainsResultResponse).isInstanceOf(KotlinResult.success(LinkedDomainVerified)::class.java)
            assertThat((actualLinkedDomainsResultResponse.getOrNull() as? LinkedDomainVerified)?.domainUrl)
                .isEqualTo("issuertestng.com")
        }
    }

    @Test
    fun `test linked domains with an array of domains successfully`() {
        // Arrange
        val suppliedDidWithMultipleServiceEndpoints =
            "did:ion:EiA8HR28m5KUig9elPRXkmKvvBGXcOoxpUrCscTdGJcIXQ?-ion-initial-state=eyJkZWx0YV9oYXNoIjoiRWlDVD" +
            "V5MG5nNTJCNzVjYWFqWU9qVjBRMmpxSng0NDZSajhRTjFpaHdteUpJZyIsInJlY292ZXJ5X2NvbW1pdG1lbnQiOiJFaURsOER4eX" +
            "ZLa3lvRmJsUWp0OXllU2J3TXkwR083MFM1R2FUU1F0UlF0aFJRIn0.eyJ1cGRhdGVfY29tbWl0bWVudCI6IkVpRGFXS2sycjJiSW" +
            "JsRWFpRUhtRU5Kc2h6czhtY1hJd2hTV0Z1YmtWQlJ3WWciLCJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbn" +
            "QiOnsicHVibGljX2tleXMiOlt7ImlkIjoic2lnX2VkMmM1ZWRmIiwidHlwZSI6IkVjZHNhU2VjcDI1NmsxVmVyaWZpY2F0aW9uS2" +
            "V5MjAxOSIsImp3ayI6eyJrdHkiOiJFQyIsImNydiI6InNlY3AyNTZrMSIsIngiOiJoTzhYaXhtWUxNOVVVMmFWOW9kc2VsSDNobD" +
            "JtbVFPLS1GTzNKa2JrekVrIiwieSI6InBDWEpxbXpUbzVQQkdRTERibnRtdUFaSElZQnFZOG1DZVdkaWhpb0tGUmMifSwicHVycG" +
            "9zZSI6WyJhdXRoIiwiZ2VuZXJhbCJdfV19fV19"
        val expectedResponseString =
            "{\"@context\":\"https://www.w3.org/ns/did-resolution/v1\",\"didDocument\":{\"id\":\"did:ion:EiA8HR28m5KUig9el" +
            "PRXkmKvvBGXcOoxpUrCscTdGJcIXQ?-ion-initial-state=eyJkZWx0YV9oYXNoIjoiRWlDVDV5MG5nNTJCNzVjYWFqWU9qVjB" +
            "RMmpxSng0NDZSajhRTjFpaHdteUpJZyIsInJlY292ZXJ5X2NvbW1pdG1lbnQiOiJFaURsOER4eXZLa3lvRmJsUWp0OXllU2J3TXk" +
            "wR083MFM1R2FUU1F0UlF0aFJRIn0.eyJ1cGRhdGVfY29tbWl0bWVudCI6IkVpRGFXS2sycjJiSWJsRWFpRUhtRU5Kc2h6czhtY1h" +
            "Jd2hTV0Z1YmtWQlJ3WWciLCJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljX2tleXMiOlt" +
            "7ImlkIjoic2lnX2VkMmM1ZWRmIiwidHlwZSI6IkVjZHNhU2VjcDI1NmsxVmVyaWZpY2F0aW9uS2V5MjAxOSIsImp3ayI6eyJrdHk" +
            "iOiJFQyIsImNydiI6InNlY3AyNTZrMSIsIngiOiJoTzhYaXhtWUxNOVVVMmFWOW9kc2VsSDNobDJtbVFPLS1GTzNKa2JrekVrIiw" +
            "ieSI6InBDWEpxbXpUbzVQQkdRTERibnRtdUFaSElZQnFZOG1DZVdkaWhpb0tGUmMifSwicHVycG9zZSI6WyJhdXRoIiwiZ2VuZXJ" +
            "hbCJdfV19fV19\",\"@context\":[\"https://www.w3.org/ns/did/v1\",{\"@base\":\"did:ion:EiA8HR28m5KUig9elPRXkmKv" +
            "vBGXcOoxpUrCscTdGJcIXQ?-ion-initial-state=eyJkZWx0YV9oYXNoIjoiRWlDVDV5MG5nNTJCNzVjYWFqWU9qVjBRMmpxSn" +
            "g0NDZSajhRTjFpaHdteUpJZyIsInJlY292ZXJ5X2NvbW1pdG1lbnQiOiJFaURsOER4eXZLa3lvRmJsUWp0OXllU2J3TXkwR083MF" +
            "M1R2FUU1F0UlF0aFJRIn0.eyJ1cGRhdGVfY29tbWl0bWVudCI6IkVpRGFXS2sycjJiSWJsRWFpRUhtRU5Kc2h6czhtY1hJd2hTV0" +
            "Z1YmtWQlJ3WWciLCJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljX2tleXMiOlt7ImlkIj" +
            "oic2lnX2VkMmM1ZWRmIiwidHlwZSI6IkVjZHNhU2VjcDI1NmsxVmVyaWZpY2F0aW9uS2V5MjAxOSIsImp3ayI6eyJrdHkiOiJFQy" +
            "IsImNydiI6InNlY3AyNTZrMSIsIngiOiJoTzhYaXhtWUxNOVVVMmFWOW9kc2VsSDNobDJtbVFPLS1GTzNKa2JrekVrIiwieSI6In" +
            "BDWEpxbXpUbzVQQkdRTERibnRtdUFaSElZQnFZOG1DZVdkaWhpb0tGUmMifSwicHVycG9zZSI6WyJhdXRoIiwiZ2VuZXJhbCJdfV" +
            "19fV19\"}],\"service\":[{\"id\":\"#linkeddomains\",\"type\":\"LinkedDomains\",\"serviceEndpoint\": {\"origins\":[\"h" +
            "ttps://issuertestng.com\"]}}],\"verificationMethod\":[{\"id\":\"#sig_ed2c5edf\",\"controller\":\"\",\"type\":\"Ecd" +
            "saSecp256k1VerificationKey2019\",\"publicKeyJwk\":{\"kty\":\"EC\",\"crv\":\"secp256k1\",\"x\":\"hO8XixmYLM9UU2aV9o" +
            "dselH3hl2mmQO--FO3JkbkzEk\",\"y\":\"pCXJqmzTo5PBGQLDbntmuAZHIYBqY8mCeWdihioKFRc\"}}],\"authentication\":[\"#" +
            "sig_ed2c5edf\"]},\"methodMetadata\":{\"published\":false,\"recoveryCommitment\":\"EiDl8DxyvKkyoFblQjt9yeSbwM" +
            "y0GO70S5GaTSQtRQthRQ\",\"updateCommitment\":\"EiDaWKk2r2bIblEaiEHmENJshzs8mcXIwhSWFubkVBRwYg\"},\"resolver" +
            "Metadata\":{\"driverId\":\"did:ion\",\"driver\":\"HttpDriver\",\"retrieved\":\"2020-10-25T06:39:09.486Z\",\"durati" +
            "on\":\"117.3589ms\"}}"
        val expectedResponse = defaultTestSerializer.decodeFromString(IdentifierResponse.serializer(), expectedResponseString)
        val expectedWellKnownConfigDocumentResponse =
            "{\"@context\":\"https://identity.foundation/.well-known/contexts/did-configuration-v0.0.jsonld\",\"linked" +
            "_dids\":[\"eyJhbGciOiJFUzI1NksiLCJraWQiOiJkaWQ6aW9uOkVpQThIUjI4bTVLVWlnOWVsUFJYa21LdnZCR1hjT294cFVyQ3N" +
            "jVGRHSmNJWFE_LWlvbi1pbml0aWFsLXN0YXRlPWV5SmtaV3gwWVY5b1lYTm9Jam9pUldsRFZEVjVNRzVuTlRKQ056VmpZV0ZxV1U" +
            "5cVZqQlJNbXB4U25nME5EWlNhamhSVGpGcGFIZHRlVXBKWnlJc0luSmxZMjkyWlhKNVgyTnZiVzFwZEcxbGJuUWlPaUpGYVVSc09" +
            "FUjRlWFpMYTNsdlJtSnNVV3AwT1hsbFUySjNUWGt3UjA4M01GTTFSMkZVVTFGMFVsRjBhRkpSSW4wLmV5SjFjR1JoZEdWZlkyOXR" +
            "iV2wwYldWdWRDSTZJa1ZwUkdGWFMyc3ljakppU1dKc1JXRnBSVWh0UlU1S2MyaDZjemh0WTFoSmQyaFRWMFoxWW10V1FsSjNXV2N" +
            "pTENKd1lYUmphR1Z6SWpwYmV5SmhZM1JwYjI0aU9pSnlaWEJzWVdObElpd2laRzlqZFcxbGJuUWlPbnNpY0hWaWJHbGpYMnRsZVh" +
            "NaU9sdDdJbWxrSWpvaWMybG5YMlZrTW1NMVpXUm1JaXdpZEhsd1pTSTZJa1ZqWkhOaFUyVmpjREkxTm1zeFZtVnlhV1pwWTJGMGF" +
            "XOXVTMlY1TWpBeE9TSXNJbXAzYXlJNmV5SnJkSGtpT2lKRlF5SXNJbU55ZGlJNkluTmxZM0F5TlRack1TSXNJbmdpT2lKb1R6aFl" +
            "hWGh0V1V4Tk9WVlZNbUZXT1c5a2MyVnNTRE5vYkRKdGJWRlBMUzFHVHpOS2EySnJla1ZySWl3aWVTSTZJbkJEV0VweGJYcFVielZ" +
            "RUWtkUlRFUmliblJ0ZFVGYVNFbFpRbkZaT0cxRFpWZGthV2hwYjB0R1VtTWlmU3dpY0hWeWNHOXpaU0k2V3lKaGRYUm9JaXdpWjJ" +
            "WdVpYSmhiQ0pkZlYxOWZWMTkjc2lnX2VkMmM1ZWRmIn0.eyJzdWIiOiJkaWQ6aW9uOkVpQThIUjI4bTVLVWlnOWVsUFJYa21LdnZ" +
            "CR1hjT294cFVyQ3NjVGRHSmNJWFE_LWlvbi1pbml0aWFsLXN0YXRlPWV5SmtaV3gwWVY5b1lYTm9Jam9pUldsRFZEVjVNRzVuTlR" +
            "KQ056VmpZV0ZxV1U5cVZqQlJNbXB4U25nME5EWlNhamhSVGpGcGFIZHRlVXBKWnlJc0luSmxZMjkyWlhKNVgyTnZiVzFwZEcxbGJ" +
            "uUWlPaUpGYVVSc09FUjRlWFpMYTNsdlJtSnNVV3AwT1hsbFUySjNUWGt3UjA4M01GTTFSMkZVVTFGMFVsRjBhRkpSSW4wLmV5SjF" +
            "jR1JoZEdWZlkyOXRiV2wwYldWdWRDSTZJa1ZwUkdGWFMyc3ljakppU1dKc1JXRnBSVWh0UlU1S2MyaDZjemh0WTFoSmQyaFRWMFo" +
            "xWW10V1FsSjNXV2NpTENKd1lYUmphR1Z6SWpwYmV5SmhZM1JwYjI0aU9pSnlaWEJzWVdObElpd2laRzlqZFcxbGJuUWlPbnNpY0h" +
            "WaWJHbGpYMnRsZVhNaU9sdDdJbWxrSWpvaWMybG5YMlZrTW1NMVpXUm1JaXdpZEhsd1pTSTZJa1ZqWkhOaFUyVmpjREkxTm1zeFZ" +
            "tVnlhV1pwWTJGMGFXOXVTMlY1TWpBeE9TSXNJbXAzYXlJNmV5SnJkSGtpT2lKRlF5SXNJbU55ZGlJNkluTmxZM0F5TlRack1TSXN" +
            "JbmdpT2lKb1R6aFlhWGh0V1V4Tk9WVlZNbUZXT1c5a2MyVnNTRE5vYkRKdGJWRlBMUzFHVHpOS2EySnJla1ZySWl3aWVTSTZJbkJ" +
            "EV0VweGJYcFVielZRUWtkUlRFUmliblJ0ZFVGYVNFbFpRbkZaT0cxRFpWZGthV2hwYjB0R1VtTWlmU3dpY0hWeWNHOXpaU0k2V3l" +
            "KaGRYUm9JaXdpWjJWdVpYSmhiQ0pkZlYxOWZWMTkiLCJpc3MiOiJkaWQ6aW9uOkVpQThIUjI4bTVLVWlnOWVsUFJYa21LdnZCR1h" +
            "jT294cFVyQ3NjVGRHSmNJWFE_LWlvbi1pbml0aWFsLXN0YXRlPWV5SmtaV3gwWVY5b1lYTm9Jam9pUldsRFZEVjVNRzVuTlRKQ05" +
            "6VmpZV0ZxV1U5cVZqQlJNbXB4U25nME5EWlNhamhSVGpGcGFIZHRlVXBKWnlJc0luSmxZMjkyWlhKNVgyTnZiVzFwZEcxbGJuUWl" +
            "PaUpGYVVSc09FUjRlWFpMYTNsdlJtSnNVV3AwT1hsbFUySjNUWGt3UjA4M01GTTFSMkZVVTFGMFVsRjBhRkpSSW4wLmV5SjFjR1J" +
            "oZEdWZlkyOXRiV2wwYldWdWRDSTZJa1ZwUkdGWFMyc3ljakppU1dKc1JXRnBSVWh0UlU1S2MyaDZjemh0WTFoSmQyaFRWMFoxWW1" +
            "0V1FsSjNXV2NpTENKd1lYUmphR1Z6SWpwYmV5SmhZM1JwYjI0aU9pSnlaWEJzWVdObElpd2laRzlqZFcxbGJuUWlPbnNpY0hWaWJ" +
            "HbGpYMnRsZVhNaU9sdDdJbWxrSWpvaWMybG5YMlZrTW1NMVpXUm1JaXdpZEhsd1pTSTZJa1ZqWkhOaFUyVmpjREkxTm1zeFZtVnl" +
            "hV1pwWTJGMGFXOXVTMlY1TWpBeE9TSXNJbXAzYXlJNmV5SnJkSGtpT2lKRlF5SXNJbU55ZGlJNkluTmxZM0F5TlRack1TSXNJbmd" +
            "pT2lKb1R6aFlhWGh0V1V4Tk9WVlZNbUZXT1c5a2MyVnNTRE5vYkRKdGJWRlBMUzFHVHpOS2EySnJla1ZySWl3aWVTSTZJbkJEV0V" +
            "weGJYcFVielZRUWtkUlRFUmliblJ0ZFVGYVNFbFpRbkZaT0cxRFpWZGthV2hwYjB0R1VtTWlmU3dpY0hWeWNHOXpaU0k2V3lKaGR" +
            "YUm9JaXdpWjJWdVpYSmhiQ0pkZlYxOWZWMTkiLCJuYmYiOjE2MDM0MTU2NjQsImV4cCI6MjM5MjMzNDA2NCwidmMiOnsiQGNvbnR" +
            "leHQiOlsiaHR0cHM6Ly93d3cudzMub3JnLzIwMTgvY3JlZGVudGlhbHMvdjEiLCJodHRwczovL2lkZW50aXR5LmZvdW5kYXRpb24" +
            "vLndlbGwta25vd24vY29udGV4dHMvZGlkLWNvbmZpZ3VyYXRpb24tdjAuMC5qc29ubGQiXSwiaXNzdWVyIjoiZGlkOmlvbjpFaUE" +
            "4SFIyOG01S1VpZzllbFBSWGttS3Z2QkdYY09veHBVckNzY1RkR0pjSVhRPy1pb24taW5pdGlhbC1zdGF0ZT1leUprWld4MFlWOW9" +
            "ZWE5vSWpvaVJXbERWRFY1TUc1bk5USkNOelZqWVdGcVdVOXFWakJSTW1weFNuZzBORFpTYWpoUlRqRnBhSGR0ZVVwSlp5SXNJbkp" +
            "sWTI5MlpYSjVYMk52YlcxcGRHMWxiblFpT2lKRmFVUnNPRVI0ZVhaTGEzbHZSbUpzVVdwME9YbGxVMkozVFhrd1IwODNNRk0xUjJ" +
            "GVVUxRjBVbEYwYUZKUkluMC5leUoxY0dSaGRHVmZZMjl0YldsMGJXVnVkQ0k2SWtWcFJHRlhTMnN5Y2pKaVNXSnNSV0ZwUlVodFJ" +
            "VNUtjMmg2Y3podFkxaEpkMmhUVjBaMVltdFdRbEozV1djaUxDSndZWFJqYUdWeklqcGJleUpoWTNScGIyNGlPaUp5WlhCc1lXTmx" +
            "JaXdpWkc5amRXMWxiblFpT25zaWNIVmliR2xqWDJ0bGVYTWlPbHQ3SW1sa0lqb2ljMmxuWDJWa01tTTFaV1JtSWl3aWRIbHdaU0k" +
            "2SWtWalpITmhVMlZqY0RJMU5tc3hWbVZ5YVdacFkyRjBhVzl1UzJWNU1qQXhPU0lzSW1wM2F5STZleUpyZEhraU9pSkZReUlzSW1" +
            "OeWRpSTZJbk5sWTNBeU5UWnJNU0lzSW5naU9pSm9UemhZYVhodFdVeE5PVlZWTW1GV09XOWtjMlZzU0ROb2JESnRiVkZQTFMxR1R" +
            "6TkthMkpyZWtWcklpd2llU0k2SW5CRFdFcHhiWHBVYnpWUVFrZFJURVJpYm5SdGRVRmFTRWxaUW5GWk9HMURaVmRrYVdocGIwdEd" +
            "VbU1pZlN3aWNIVnljRzl6WlNJNld5SmhkWFJvSWl3aVoyVnVaWEpoYkNKZGZWMTlmVjE5IiwiaXNzdWFuY2VEYXRlIjoiMjAyMC0" +
            "xMC0yM1QwMToxNDoyNC43NzRaIiwiZXhwaXJhdGlvbkRhdGUiOiIyMDQ1LTEwLTIzVDAxOjE0OjI0Ljc3NFoiLCJ0eXBlIjpbIlZ" +
            "lcmlmaWFibGVDcmVkZW50aWFsIiwiRG9tYWluTGlua2FnZUNyZWRlbnRpYWwiXSwiY3JlZGVudGlhbFN1YmplY3QiOnsiaWQiOiJ" +
            "kaWQ6aW9uOkVpQThIUjI4bTVLVWlnOWVsUFJYa21LdnZCR1hjT294cFVyQ3NjVGRHSmNJWFE_LWlvbi1pbml0aWFsLXN0YXRlPWV" +
            "5SmtaV3gwWVY5b1lYTm9Jam9pUldsRFZEVjVNRzVuTlRKQ056VmpZV0ZxV1U5cVZqQlJNbXB4U25nME5EWlNhamhSVGpGcGFIZHR" +
            "lVXBKWnlJc0luSmxZMjkyWlhKNVgyTnZiVzFwZEcxbGJuUWlPaUpGYVVSc09FUjRlWFpMYTNsdlJtSnNVV3AwT1hsbFUySjNUWGt" +
            "3UjA4M01GTTFSMkZVVTFGMFVsRjBhRkpSSW4wLmV5SjFjR1JoZEdWZlkyOXRiV2wwYldWdWRDSTZJa1ZwUkdGWFMyc3ljakppU1d" +
            "Kc1JXRnBSVWh0UlU1S2MyaDZjemh0WTFoSmQyaFRWMFoxWW10V1FsSjNXV2NpTENKd1lYUmphR1Z6SWpwYmV5SmhZM1JwYjI0aU9" +
            "pSnlaWEJzWVdObElpd2laRzlqZFcxbGJuUWlPbnNpY0hWaWJHbGpYMnRsZVhNaU9sdDdJbWxrSWpvaWMybG5YMlZrTW1NMVpXUm1" +
            "JaXdpZEhsd1pTSTZJa1ZqWkhOaFUyVmpjREkxTm1zeFZtVnlhV1pwWTJGMGFXOXVTMlY1TWpBeE9TSXNJbXAzYXlJNmV5SnJkSGt" +
            "pT2lKRlF5SXNJbU55ZGlJNkluTmxZM0F5TlRack1TSXNJbmdpT2lKb1R6aFlhWGh0V1V4Tk9WVlZNbUZXT1c5a2MyVnNTRE5vYkR" +
            "KdGJWRlBMUzFHVHpOS2EySnJla1ZySWl3aWVTSTZJbkJEV0VweGJYcFVielZRUWtkUlRFUmliblJ0ZFVGYVNFbFpRbkZaT0cxRFp" +
            "WZGthV2hwYjB0R1VtTWlmU3dpY0hWeWNHOXpaU0k2V3lKaGRYUm9JaXdpWjJWdVpYSmhiQ0pkZlYxOWZWMTkiLCJvcmlnaW4iOiJ" +
            "odHRwczovL2lzc3VlcnRlc3RuZy5jb20ifX19.luP4vIE30u_kj5CINDhbjZcsHDbEEXS5hMFnzd7qVKs2OtseDJlYcgLOgbCqI1" +
            "7v3edXx9OVkErQ-sSuIGvt8g\"]}"
        val expectedWellKnownConfigDocument =
            defaultTestSerializer.decodeFromString(LinkedDomainsResponse.serializer(), expectedWellKnownConfigDocumentResponse)
        val expectedDomainUrl = "https://issuertestng.com"
        coEvery { linkedDomainsService.resolveIdentifierDocument(suppliedDidWithMultipleServiceEndpoints) } returns KotlinResult.success(
            expectedResponse.didDocument
        )
        coEvery { linkedDomainsService["getWellKnownConfigDocument"](expectedDomainUrl) } returns KotlinResult.success(
            expectedWellKnownConfigDocument
        )
        coEvery { mockedJwtValidator.verifySignature(any()) } returns true
        coEvery { mockedJwtValidator.validateDidInHeaderAndPayload(any(), any()) } returns true

        // Act and Assert
        runBlocking {
            val actualLinkedDomainsResultResponse =
                linkedDomainsService.fetchDocumentAndVerifyLinkedDomains(suppliedDidWithMultipleServiceEndpoints)
            assertThat(actualLinkedDomainsResultResponse).isInstanceOf(KotlinResult.success(LinkedDomainVerified)::class.java)
            assertThat((actualLinkedDomainsResultResponse.getOrNull() as? LinkedDomainVerified)?.domainUrl)
                .isEqualTo("issuertestng.com")
        }
    }

    @Test
    fun `linked domains exact HTTPS origin is canonicalized before verification`() {
        val validator: DomainLinkageCredentialValidator = mockk()
        val service = spyk(
            LinkedDomainsService(
                mockk(relaxed = true),
                mockedResolver,
                validator,
                flightProvider = linkedDomainHardeningFlightProvider
            ),
            recordPrivateCalls = true
        )
        val identifierDocument = IdentifierDocument(id = "did:example:issuer").also {
            it.service = listOf(
                IdentifierDocumentService(
                    id = "#linked-domains",
                    type = "LinkedDomains",
                    serviceEndpoint = listOf("HTTPS://Example.COM:443/")
                )
            )
        }
        val response = LinkedDomainsResponse("", listOf("linked-domain-jwt"))

        coEvery { service["getWellKnownConfigDocument"]("https://example.com") } returns KotlinResult.success(response)
        coEvery {
            validator.validate("linked-domain-jwt", "did:example:issuer", "https://example.com")
        } returns DomainLinkageCredentialValidationResult.VALID

        runBlocking {
            val result = service.validateLinkedDomains(identifierDocument)

            val linkedDomainResult = result.getOrNull()
            assertThat(linkedDomainResult).isInstanceOf(LinkedDomainVerified::class.java)
            assertThat((linkedDomainResult as LinkedDomainVerified).domainUrl).isEqualTo("https://example.com")
        }
        coVerify(exactly = 1) { service["getWellKnownConfigDocument"]("https://example.com") }
        coVerify(exactly = 1) {
            validator.validate("linked-domain-jwt", "did:example:issuer", "https://example.com")
        }
    }

    @Test
    fun `linked domains defaults to legacy raw endpoint handling`() {
        val validator: DomainLinkageCredentialValidator = mockk()
        val service = spyk(
            LinkedDomainsService(mockk(relaxed = true), mockedResolver, validator),
            recordPrivateCalls = true
        )
        val rawEndpoint = "https://Example.COM/tenant"
        val identifierDocument = IdentifierDocument(id = "did:example:issuer").also {
            it.service = listOf(
                IdentifierDocumentService(
                    id = "#linked-domains",
                    type = "LinkedDomains",
                    serviceEndpoint = listOf(rawEndpoint)
                )
            )
        }
        val response = LinkedDomainsResponse("", listOf("linked-domain-jwt"))

        coEvery {
            service["getWellKnownConfigDocument"](rawEndpoint)
        } returns KotlinResult.success(response)
        coEvery {
            validator.validate("linked-domain-jwt", "did:example:issuer", rawEndpoint)
        } returns DomainLinkageCredentialValidationResult.VALID

        runBlocking {
            val result = service.validateLinkedDomains(identifierDocument)

            val linkedDomainResult = result.getOrNull()
            assertThat(linkedDomainResult).isInstanceOf(LinkedDomainVerified::class.java)
            assertThat((linkedDomainResult as LinkedDomainVerified).domainUrl)
                .isEqualTo("Example.COM")
        }
        coVerify(exactly = 1) { service["getWellKnownConfigDocument"](rawEndpoint) }
        coVerify(exactly = 1) {
            validator.validate("linked-domain-jwt", "did:example:issuer", rawEndpoint)
        }
    }

    @Test
    fun `legacy validation preserves verified non-default port`() {
        val validator: DomainLinkageCredentialValidator = mockk()
        val service = spyk(
            LinkedDomainsService(mockk(relaxed = true), mockedResolver, validator),
            recordPrivateCalls = true
        )
        val verifiedOrigin = "https://verifier.example:8443"
        val identifierDocument = IdentifierDocument(id = "did:example:verifier").also {
            it.service = listOf(
                IdentifierDocumentService(
                    id = "#linked-domains",
                    type = "LinkedDomains",
                    serviceEndpoint = listOf(verifiedOrigin)
                )
            )
        }
        val response = LinkedDomainsResponse("", listOf("linked-domain-jwt"))

        coEvery {
            service["getWellKnownConfigDocument"](verifiedOrigin)
        } returns KotlinResult.success(response)
        coEvery {
            validator.validate("linked-domain-jwt", "did:example:verifier", verifiedOrigin)
        } returns DomainLinkageCredentialValidationResult.VALID

        runBlocking {
            val result = service.validateLinkedDomains(identifierDocument)
            val linkedDomainResult = result.getOrNull() as LinkedDomainVerified

            assertThat(linkedDomainResult.domainUrl).isEqualTo("verifier.example")
            assertThat(linkedDomainResult.origin).isNull()
            assertThat(linkedDomainResult.validatedOrigin).isEqualTo(verifiedOrigin)
        }
    }

    @Test
    fun `linked domains non-default HTTPS port is retained in verified origin`() {
        val validator: DomainLinkageCredentialValidator = mockk()
        val service = spyk(
            LinkedDomainsService(
                mockk(relaxed = true),
                mockedResolver,
                validator,
                flightProvider = linkedDomainHardeningFlightProvider
            ),
            recordPrivateCalls = true
        )
        val identifierDocument = IdentifierDocument(id = "did:example:issuer").also {
            it.service = listOf(
                IdentifierDocumentService(
                    id = "#linked-domains",
                    type = "LinkedDomains",
                    serviceEndpoint = listOf("https://example.com:8443")
                )
            )
        }
        val response = LinkedDomainsResponse("", listOf("linked-domain-jwt"))

        coEvery {
            service["getWellKnownConfigDocument"]("https://example.com:8443")
        } returns KotlinResult.success(response)
        coEvery {
            validator.validate(
                "linked-domain-jwt",
                "did:example:issuer",
                "https://example.com:8443"
            )
        } returns DomainLinkageCredentialValidationResult.VALID

        runBlocking {
            val result = service.validateLinkedDomains(identifierDocument)

            val linkedDomainResult = result.getOrNull()
            assertThat(linkedDomainResult).isInstanceOf(LinkedDomainVerified::class.java)
            assertThat((linkedDomainResult as LinkedDomainVerified).domainUrl)
                .isEqualTo("https://example.com:8443")
        }
        coVerify(exactly = 1) {
            service["getWellKnownConfigDocument"]("https://example.com:8443")
        }
        coVerify(exactly = 1) {
            validator.validate(
                "linked-domain-jwt",
                "did:example:issuer",
                "https://example.com:8443"
            )
        }
    }

    @Test
    fun `linked domains trailing dot is removed from canonical origin`() {
        val validator: DomainLinkageCredentialValidator = mockk()
        val service = spyk(
            LinkedDomainsService(
                mockk(relaxed = true),
                mockedResolver,
                validator,
                flightProvider = linkedDomainHardeningFlightProvider
            ),
            recordPrivateCalls = true
        )
        val identifierDocument = IdentifierDocument(id = "did:example:issuer").also {
            it.service = listOf(
                IdentifierDocumentService(
                    id = "#linked-domains",
                    type = "LinkedDomains",
                    serviceEndpoint = listOf("https://Example.COM.:8443")
                )
            )
        }
        val response = LinkedDomainsResponse("", listOf("linked-domain-jwt"))
        val expectedOrigin = "https://example.com:8443"

        coEvery {
            service["getWellKnownConfigDocument"](expectedOrigin)
        } returns KotlinResult.success(response)
        coEvery {
            validator.validate("linked-domain-jwt", "did:example:issuer", expectedOrigin)
        } returns DomainLinkageCredentialValidationResult.VALID

        runBlocking {
            val result = service.validateLinkedDomains(identifierDocument)

            val linkedDomainResult = result.getOrNull()
            assertThat(linkedDomainResult).isInstanceOf(LinkedDomainVerified::class.java)
            assertThat((linkedDomainResult as LinkedDomainVerified).domainUrl)
                .isEqualTo(expectedOrigin)
        }
        coVerify(exactly = 1) {
            service["getWellKnownConfigDocument"](expectedOrigin)
        }
        coVerify(exactly = 1) {
            validator.validate("linked-domain-jwt", "did:example:issuer", expectedOrigin)
        }
    }

    @Test
    fun `linked domains HTTPS endpoint with subpath is rejected without network request`() {
        val validator: DomainLinkageCredentialValidator = mockk(relaxed = true)
        val service = spyk(
            LinkedDomainsService(
                mockk(relaxed = true),
                mockedResolver,
                validator,
                flightProvider = linkedDomainHardeningFlightProvider
            ),
            recordPrivateCalls = true
        )
        val identifierDocument = IdentifierDocument(id = "did:example:issuer").also {
            it.service = listOf(
                IdentifierDocumentService(
                    id = "#linked-domains",
                    type = "LinkedDomains",
                    serviceEndpoint = listOf("https://example.com/attacker-controlled")
                )
            )
        }

        runBlocking {
            val result = service.validateLinkedDomains(identifierDocument)

            val linkedDomainResult = result.getOrNull()
            assertThat(linkedDomainResult).isInstanceOf(LinkedDomainUnVerified::class.java)
            assertThat((linkedDomainResult as LinkedDomainUnVerified).domainUrl).isEqualTo("example.com")
        }
        coVerify(exactly = 0) { service["getWellKnownConfigDocument"](any<String>()) }
        coVerify(exactly = 0) { validator.validate(any(), any(), any()) }
    }

    @Test
    fun `test did without service endpoints`() {
        val suppliedDidWithoutServiceEndpoint =
            "did:ion:EiBFVG2BhADKN48GNNhD-y0EgrcjluWJg7tQbpe1rv6kMQ:eyJkZWx0YSI6eyJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZ" +
            "XBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJSVTRfc2lnbl9JT05fMSIsInB1YmxpY0tleUp3ayI6eyJjc" +
            "nYiOiJzZWNwMjU2azEiLCJrdHkiOiJFQyIsIngiOiIzbE5VXzIwbnREeHBfZnBnbDRMUEdtYUc4c0M4Ty12N1Aydk5waG00R3BVI" +
            "iwieSI6IkxzSUl4X1lOd0lsZ1BDamNsbklBUnMtUThtSlc3Z0M0OTJwM1B3OWdBWk0ifSwicHVycG9zZXMiOlsiYXV0aGVudGljY" +
            "XRpb24iXSwidHlwZSI6IkVjZHNhU2VjcDI1NmsxVmVyaWZpY2F0aW9uS2V5MjAxOSJ9XX19XSwidXBkYXRlQ29tbWl0bWVudCI6I" +
            "kVpRDRmZDdGUU04WmtTMF9xVDVva0Y5T1J3enFLSDZFSVB6WWRnU2hIYzg4X2cifSwic3VmZml4RGF0YSI6eyJkZWx0YUhhc2giO" +
            "iJFaURCSFA5RDNLcXBHWE9pTmlhR01rb3pGOEVZNWxRdFVyaW0zcDBVaUEweGxRIiwicmVjb3ZlcnlDb21taXRtZW50IjoiRWlET" +
            "ldBSXM0cng3eWpRS1FMRlExZ2MxTUlrSDFQRVRqN3lDWlg2NnJtano1QSJ9fQ"
        val actualDidDocString =
            "{\"@context\":\"https://www.w3.org/ns/did-resolution/v1\",\"didDocument\":{\"id\":\"did:ion:EiBFVG2BhADKN48GN" +
            "NhD-y0EgrcjluWJg7tQbpe1rv6kMQ:eyJkZWx0YSI6eyJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOn" +
            "sicHVibGljS2V5cyI6W3siaWQiOiJSVTRfc2lnbl9JT05fMSIsInB1YmxpY0tleUp3ayI6eyJjcnYiOiJzZWNwMjU2azEiLCJrdH" +
            "kiOiJFQyIsIngiOiIzbE5VXzIwbnREeHBfZnBnbDRMUEdtYUc4c0M4Ty12N1Aydk5waG00R3BVIiwieSI6IkxzSUl4X1lOd0lsZ1" +
            "BDamNsbklBUnMtUThtSlc3Z0M0OTJwM1B3OWdBWk0ifSwicHVycG9zZXMiOlsiYXV0aGVudGljYXRpb24iXSwidHlwZSI6IkVjZH" +
            "NhU2VjcDI1NmsxVmVyaWZpY2F0aW9uS2V5MjAxOSJ9XX19XSwidXBkYXRlQ29tbWl0bWVudCI6IkVpRDRmZDdGUU04WmtTMF9xVD" +
            "Vva0Y5T1J3enFLSDZFSVB6WWRnU2hIYzg4X2cifSwic3VmZml4RGF0YSI6eyJkZWx0YUhhc2giOiJFaURCSFA5RDNLcXBHWE9pTm" +
            "lhR01rb3pGOEVZNWxRdFVyaW0zcDBVaUEweGxRIiwicmVjb3ZlcnlDb21taXRtZW50IjoiRWlETldBSXM0cng3eWpRS1FMRlExZ2" +
            "MxTUlrSDFQRVRqN3lDWlg2NnJtano1QSJ9fQ\",\"@context\":[\"https://www.w3.org/ns/did/v1\",{\"@base\":\"did:ion:E" +
            "iBFVG2BhADKN48GNNhD-y0EgrcjluWJg7tQbpe1rv6kMQ:eyJkZWx0YSI6eyJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIi" +
            "wiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJSVTRfc2lnbl9JT05fMSIsInB1YmxpY0tleUp3ayI6eyJjcnYiOiJzZW" +
            "NwMjU2azEiLCJrdHkiOiJFQyIsIngiOiIzbE5VXzIwbnREeHBfZnBnbDRMUEdtYUc4c0M4Ty12N1Aydk5waG00R3BVIiwieSI6Ik" +
            "xzSUl4X1lOd0lsZ1BDamNsbklBUnMtUThtSlc3Z0M0OTJwM1B3OWdBWk0ifSwicHVycG9zZXMiOlsiYXV0aGVudGljYXRpb24iXS" +
            "widHlwZSI6IkVjZHNhU2VjcDI1NmsxVmVyaWZpY2F0aW9uS2V5MjAxOSJ9XX19XSwidXBkYXRlQ29tbWl0bWVudCI6IkVpRDRmZD" +
            "dGUU04WmtTMF9xVDVva0Y5T1J3enFLSDZFSVB6WWRnU2hIYzg4X2cifSwic3VmZml4RGF0YSI6eyJkZWx0YUhhc2giOiJFaURCSF" +
            "A5RDNLcXBHWE9pTmlhR01rb3pGOEVZNWxRdFVyaW0zcDBVaUEweGxRIiwicmVjb3ZlcnlDb21taXRtZW50IjoiRWlETldBSXM0cn" +
            "g3eWpRS1FMRlExZ2MxTUlrSDFQRVRqN3lDWlg2NnJtano1QSJ9fQ\"}],\"verificationMethod\":[{\"id\":\"#RU4_sign_ION_1" +
            "\",\"controller\":\"did:ion:EiBFVG2BhADKN48GNNhD-y0EgrcjluWJg7tQbpe1rv6kMQ:eyJkZWx0YSI6eyJwYXRjaGVzIjpbe" +
            "yJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJSVTRfc2lnbl9JT05fMSIsInB1YmxpY" +
            "0tleUp3ayI6eyJjcnYiOiJzZWNwMjU2azEiLCJrdHkiOiJFQyIsIngiOiIzbE5VXzIwbnREeHBfZnBnbDRMUEdtYUc4c0M4Ty12N" +
            "1Aydk5waG00R3BVIiwieSI6IkxzSUl4X1lOd0lsZ1BDamNsbklBUnMtUThtSlc3Z0M0OTJwM1B3OWdBWk0ifSwicHVycG9zZXMiO" +
            "lsiYXV0aGVudGljYXRpb24iXSwidHlwZSI6IkVjZHNhU2VjcDI1NmsxVmVyaWZpY2F0aW9uS2V5MjAxOSJ9XX19XSwidXBkYXRlQ" +
            "29tbWl0bWVudCI6IkVpRDRmZDdGUU04WmtTMF9xVDVva0Y5T1J3enFLSDZFSVB6WWRnU2hIYzg4X2cifSwic3VmZml4RGF0YSI6e" +
            "yJkZWx0YUhhc2giOiJFaURCSFA5RDNLcXBHWE9pTmlhR01rb3pGOEVZNWxRdFVyaW0zcDBVaUEweGxRIiwicmVjb3ZlcnlDb21ta" +
            "XRtZW50IjoiRWlETldBSXM0cng3eWpRS1FMRlExZ2MxTUlrSDFQRVRqN3lDWlg2NnJtano1QSJ9fQ\",\"type\":\"EcdsaSecp256k" +
            "1VerificationKey2019\",\"publicKeyJwk\":{\"crv\":\"secp256k1\",\"kty\":\"EC\",\"x\":\"3lNU_20ntDxp_fpgl4LPGmaG8sC8" +
            "O-v7P2vNphm4GpU\",\"y\":\"LsIIx_YNwIlgPCjclnIARs-Q8mJW7gC492p3Pw9gAZM\"}}],\"authentication\":[\"did:ion:EiB" +
            "FVG2BhADKN48GNNhD-y0EgrcjluWJg7tQbpe1rv6kMQ:eyJkZWx0YSI6eyJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwi" +
            "ZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJSVTRfc2lnbl9JT05fMSIsInB1YmxpY0tleUp3ayI6eyJjcnYiOiJzZWNw" +
            "MjU2azEiLCJrdHkiOiJFQyIsIngiOiIzbE5VXzIwbnREeHBfZnBnbDRMUEdtYUc4c0M4Ty12N1Aydk5waG00R3BVIiwieSI6Ikxz" +
            "SUl4X1lOd0lsZ1BDamNsbklBUnMtUThtSlc3Z0M0OTJwM1B3OWdBWk0ifSwicHVycG9zZXMiOlsiYXV0aGVudGljYXRpb24iXSwi" +
            "dHlwZSI6IkVjZHNhU2VjcDI1NmsxVmVyaWZpY2F0aW9uS2V5MjAxOSJ9XX19XSwidXBkYXRlQ29tbWl0bWVudCI6IkVpRDRmZDdG" +
            "UU04WmtTMF9xVDVva0Y5T1J3enFLSDZFSVB6WWRnU2hIYzg4X2cifSwic3VmZml4RGF0YSI6eyJkZWx0YUhhc2giOiJFaURCSFA5" +
            "RDNLcXBHWE9pTmlhR01rb3pGOEVZNWxRdFVyaW0zcDBVaUEweGxRIiwicmVjb3ZlcnlDb21taXRtZW50IjoiRWlETldBSXM0cng3" +
            "eWpRS1FMRlExZ2MxTUlrSDFQRVRqN3lDWlg2NnJtano1QSJ9fQ#RU4_sign_ION_1\"]},\"methodMetadata\":{\"published\":f" +
            "alse,\"recoveryCommitment\":\"EiDNWAIs4rx7yjQKQLFQ1gc1MIkH1PETj7yCZX66rmjz5A\",\"updateCommitment\":\"EiD4f" +
            "d7FQM8ZkS0_qT5okF9ORwzqKH6EIPzYdgShHc88_g\"},\"resolverMetadata\":{\"duration\":403.544624,\"identifier\":\"" +
            "did:ion:EiBFVG2BhADKN48GNNhD-y0EgrcjluWJg7tQbpe1rv6kMQ:eyJkZWx0YSI6eyJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZ" +
            "XBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJSVTRfc2lnbl9JT05fMSIsInB1YmxpY0tleUp3ayI6eyJjc" +
            "nYiOiJzZWNwMjU2azEiLCJrdHkiOiJFQyIsIngiOiIzbE5VXzIwbnREeHBfZnBnbDRMUEdtYUc4c0M4Ty12N1Aydk5waG00R3BVI" +
            "iwieSI6IkxzSUl4X1lOd0lsZ1BDamNsbklBUnMtUThtSlc3Z0M0OTJwM1B3OWdBWk0ifSwicHVycG9zZXMiOlsiYXV0aGVudGljY" +
            "XRpb24iXSwidHlwZSI6IkVjZHNhU2VjcDI1NmsxVmVyaWZpY2F0aW9uS2V5MjAxOSJ9XX19XSwidXBkYXRlQ29tbWl0bWVudCI6I" +
            "kVpRDRmZDdGUU04WmtTMF9xVDVva0Y5T1J3enFLSDZFSVB6WWRnU2hIYzg4X2cifSwic3VmZml4RGF0YSI6eyJkZWx0YUhhc2giO" +
            "iJFaURCSFA5RDNLcXBHWE9pTmlhR01rb3pGOEVZNWxRdFVyaW0zcDBVaUEweGxRIiwicmVjb3ZlcnlDb21taXRtZW50IjoiRWlET" +
            "ldBSXM0cng3eWpRS1FMRlExZ2MxTUlrSDFQRVRqN3lDWlg2NnJtano1QSJ9fQ\",\"driverId\":\"discover.did.microsoft.co" +
            "m/did:ion\",\"didUrl\":{\"didUrlString\":\"did:ion:EiBFVG2BhADKN48GNNhD-y0EgrcjluWJg7tQbpe1rv6kMQ:eyJkZWx0" +
            "YSI6eyJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJSVTRfc2ln" +
            "bl9JT05fMSIsInB1YmxpY0tleUp3ayI6eyJjcnYiOiJzZWNwMjU2azEiLCJrdHkiOiJFQyIsIngiOiIzbE5VXzIwbnREeHBfZnBn" +
            "bDRMUEdtYUc4c0M4Ty12N1Aydk5waG00R3BVIiwieSI6IkxzSUl4X1lOd0lsZ1BDamNsbklBUnMtUThtSlc3Z0M0OTJwM1B3OWdB" +
            "Wk0ifSwicHVycG9zZXMiOlsiYXV0aGVudGljYXRpb24iXSwidHlwZSI6IkVjZHNhU2VjcDI1NmsxVmVyaWZpY2F0aW9uS2V5MjAx" +
            "OSJ9XX19XSwidXBkYXRlQ29tbWl0bWVudCI6IkVpRDRmZDdGUU04WmtTMF9xVDVva0Y5T1J3enFLSDZFSVB6WWRnU2hIYzg4X2ci" +
            "fSwic3VmZml4RGF0YSI6eyJkZWx0YUhhc2giOiJFaURCSFA5RDNLcXBHWE9pTmlhR01rb3pGOEVZNWxRdFVyaW0zcDBVaUEweGxR" +
            "IiwicmVjb3ZlcnlDb21taXRtZW50IjoiRWlETldBSXM0cng3eWpRS1FMRlExZ2MxTUlrSDFQRVRqN3lDWlg2NnJtano1QSJ9fQ\"," +
            "\"did\":{\"didString\":\"did:ion:EiBFVG2BhADKN48GNNhD-y0EgrcjluWJg7tQbpe1rv6kMQ:eyJkZWx0YSI6eyJwYXRjaGVzI" +
            "jpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJSVTRfc2lnbl9JT05fMSIsInB1Y" +
            "mxpY0tleUp3ayI6eyJjcnYiOiJzZWNwMjU2azEiLCJrdHkiOiJFQyIsIngiOiIzbE5VXzIwbnREeHBfZnBnbDRMUEdtYUc4c0M4T" +
            "y12N1Aydk5waG00R3BVIiwieSI6IkxzSUl4X1lOd0lsZ1BDamNsbklBUnMtUThtSlc3Z0M0OTJwM1B3OWdBWk0ifSwicHVycG9zZ" +
            "XMiOlsiYXV0aGVudGljYXRpb24iXSwidHlwZSI6IkVjZHNhU2VjcDI1NmsxVmVyaWZpY2F0aW9uS2V5MjAxOSJ9XX19XSwidXBkY" +
            "XRlQ29tbWl0bWVudCI6IkVpRDRmZDdGUU04WmtTMF9xVDVva0Y5T1J3enFLSDZFSVB6WWRnU2hIYzg4X2cifSwic3VmZml4RGF0Y" +
            "SI6eyJkZWx0YUhhc2giOiJFaURCSFA5RDNLcXBHWE9pTmlhR01rb3pGOEVZNWxRdFVyaW0zcDBVaUEweGxRIiwicmVjb3ZlcnlDb" +
            "21taXRtZW50IjoiRWlETldBSXM0cng3eWpRS1FMRlExZ2MxTUlrSDFQRVRqN3lDWlg2NnJtano1QSJ9fQ\",\"method\":\"ion\",\"m" +
            "ethodSpecificId\":\"EiBFVG2BhADKN48GNNhD-y0EgrcjluWJg7tQbpe1rv6kMQ:eyJkZWx0YSI6eyJwYXRjaGVzIjpbeyJhY3R" +
            "pb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJSVTRfc2lnbl9JT05fMSIsInB1YmxpY0tleUp" +
            "3ayI6eyJjcnYiOiJzZWNwMjU2azEiLCJrdHkiOiJFQyIsIngiOiIzbE5VXzIwbnREeHBfZnBnbDRMUEdtYUc4c0M4Ty12N1Aydk5" +
            "waG00R3BVIiwieSI6IkxzSUl4X1lOd0lsZ1BDamNsbklBUnMtUThtSlc3Z0M0OTJwM1B3OWdBWk0ifSwicHVycG9zZXMiOlsiYXV" +
            "0aGVudGljYXRpb24iXSwidHlwZSI6IkVjZHNhU2VjcDI1NmsxVmVyaWZpY2F0aW9uS2V5MjAxOSJ9XX19XSwidXBkYXRlQ29tbWl" +
            "0bWVudCI6IkVpRDRmZDdGUU04WmtTMF9xVDVva0Y5T1J3enFLSDZFSVB6WWRnU2hIYzg4X2cifSwic3VmZml4RGF0YSI6eyJkZWx" +
            "0YUhhc2giOiJFaURCSFA5RDNLcXBHWE9pTmlhR01rb3pGOEVZNWxRdFVyaW0zcDBVaUEweGxRIiwicmVjb3ZlcnlDb21taXRtZW5" +
            "0IjoiRWlETldBSXM0cng3eWpRS1FMRlExZ2MxTUlrSDFQRVRqN3lDWlg2NnJtano1QSJ9fQ\",\"parseTree\":null,\"parseRule" +
            "Count\":null},\"parameters\":null,\"parametersMap\":{},\"path\":\"\",\"query\":null,\"fragment\":null,\"parseTree\"" +
            ":null,\"parseRuleCount\":null}}}"
        val actualDidDoc = defaultTestSerializer.decodeFromString(IdentifierResponse.serializer(), actualDidDocString)
        coEvery { mockedResolver.resolve(suppliedDidWithoutServiceEndpoint) } returns KotlinResult.success(actualDidDoc.didDocument)
        runBlocking {
            val actualDomainUrlResult = linkedDomainsService.fetchDocumentAndVerifyLinkedDomains(suppliedDidWithoutServiceEndpoint)
            assertThat(actualDomainUrlResult).isInstanceOf(KotlinResult.success(LinkedDomainMissing)::class.java)
        }
    }

    @Test
    fun `test failing signature verification mocking network call`() {
        val rpDid =
            "did:ion:EiBGMAR43ZWai9JKvh31ShjvNaX0dYRYI0XtLnZIa88CqQ?-ion-initial-state=eyJkZWx0YV9oYXNoIjoiRWlCYl" +
            "lRMEViTmNtMnJkc2dBMndJM0FRQTRWRlFSUTVYSnk5QzFTOWV3elFBQSIsInJlY292ZXJ5X2tleSI6eyJrdHkiOiJFQyIsImNydi" +
            "I6InNlY3AyNTZrMSIsIngiOiJNRGE4djdNNC1ETi1WeTZaWVlsUFluUHR3dFRNanZFOUgzLXdYWDJWSktjIiwieSI6IkJDcG1SNn" +
            "Bla0p2QnFFYnBINExwbnVMd1NKTEdkck5QcjNBaFdJR21iWDQifSwicmVjb3ZlcnlfY29tbWl0bWVudCI6IkVpREM3Y0J2Unp3US" +
            "1CZkhSQ2x1dG5MQU8wUlZyS3V6c0pTSEFtXzhFTERocVEifQ.eyJ1cGRhdGVfY29tbWl0bWVudCI6IkVpQXNsRml6MWNRU3Rranl" +
            "uUy03eERUbGtLOExvZ01FLWc2bkFBXy0wREVyWGciLCJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOns" +
            "icHVibGljS2V5cyI6W3siaWQiOiJWYThfc2lnbl9JT05fMSIsInR5cGUiOiJFY2RzYVNlY3AyNTZrMVZlcmlmaWNhdGlvbktleTI" +
            "wMTkiLCJqd2siOnsia3R5IjoiRUMiLCJjcnYiOiJzZWNwMjU2azEiLCJ4IjoiaEEzRWFaanZnR3BpMUFuV21TRXpQaGhlSTU1TXp" +
            "oZHdEVXRYY0FTMzZGayIsInkiOiJFNDRwSnA3X21kNlo2LXlEbmdPSzlMWWFXX0xUQzhCWGdTVk1RQ1plTnJBIn0sInVzYWdlIjp" +
            "bIm9wcyIsImF1dGgiLCJnZW5lcmFsIl19XX19XX0"
        val rpDidDoc =
            "{\"@context\":\"https://www.w3.org/ns/did-resolution/v1\",\"didDocument\":{\"id\":\"did:ion:EiBGMAR43ZWai9JKv" +
            "h31ShjvNaX0dYRYI0XtLnZIa88CqQ?-ion-initial-state=eyJkZWx0YV9oYXNoIjoiRWlCYllRMEViTmNtMnJkc2dBMndJM0F" +
            "RQTRWRlFSUTVYSnk5QzFTOWV3elFBQSIsInJlY292ZXJ5X2tleSI6eyJrdHkiOiJFQyIsImNydiI6InNlY3AyNTZrMSIsIngiOiJ" +
            "NRGE4djdNNC1ETi1WeTZaWVlsUFluUHR3dFRNanZFOUgzLXdYWDJWSktjIiwieSI6IkJDcG1SNnBla0p2QnFFYnBINExwbnVMd1N" +
            "KTEdkck5QcjNBaFdJR21iWDQifSwicmVjb3ZlcnlfY29tbWl0bWVudCI6IkVpREM3Y0J2Unp3US1CZkhSQ2x1dG5MQU8wUlZyS3V" +
            "6c0pTSEFtXzhFTERocVEifQ.eyJ1cGRhdGVfY29tbWl0bWVudCI6IkVpQXNsRml6MWNRU3RranluUy03eERUbGtLOExvZ01FLWc2" +
            "bkFBXy0wREVyWGciLCJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQi" +
            "OiJWYThfc2lnbl9JT05fMSIsInR5cGUiOiJFY2RzYVNlY3AyNTZrMVZlcmlmaWNhdGlvbktleTIwMTkiLCJqd2siOnsia3R5Ijoi" +
            "RUMiLCJjcnYiOiJzZWNwMjU2azEiLCJ4IjoiaEEzRWFaanZnR3BpMUFuV21TRXpQaGhlSTU1TXpoZHdEVXRYY0FTMzZGayIsInki" +
            "OiJFNDRwSnA3X21kNlo2LXlEbmdPSzlMWWFXX0xUQzhCWGdTVk1RQ1plTnJBIn0sInVzYWdlIjpbIm9wcyIsImF1dGgiLCJnZW5l" +
            "cmFsIl19XX19XX0\",\"@context\":[\"https://www.w3.org/ns/did/v1\",{\"@base\":\"did:ion:EiBGMAR43ZWai9JKvh31Sh" +
            "jvNaX0dYRYI0XtLnZIa88CqQ?-ion-initial-state=eyJkZWx0YV9oYXNoIjoiRWlCYllRMEViTmNtMnJkc2dBMndJM0FRQTRW" +
            "RlFSUTVYSnk5QzFTOWV3elFBQSIsInJlY292ZXJ5X2tleSI6eyJrdHkiOiJFQyIsImNydiI6InNlY3AyNTZrMSIsIngiOiJNRGE4" +
            "djdNNC1ETi1WeTZaWVlsUFluUHR3dFRNanZFOUgzLXdYWDJWSktjIiwieSI6IkJDcG1SNnBla0p2QnFFYnBINExwbnVMd1NKTEdk" +
            "ck5QcjNBaFdJR21iWDQifSwicmVjb3ZlcnlfY29tbWl0bWVudCI6IkVpREM3Y0J2Unp3US1CZkhSQ2x1dG5MQU8wUlZyS3V6c0pT" +
            "SEFtXzhFTERocVEifQ.eyJ1cGRhdGVfY29tbWl0bWVudCI6IkVpQXNsRml6MWNRU3RranluUy03eERUbGtLOExvZ01FLWc2bkFBX" +
            "y0wREVyWGciLCJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJWY" +
            "Thfc2lnbl9JT05fMSIsInR5cGUiOiJFY2RzYVNlY3AyNTZrMVZlcmlmaWNhdGlvbktleTIwMTkiLCJqd2siOnsia3R5IjoiRUMiL" +
            "CJjcnYiOiJzZWNwMjU2azEiLCJ4IjoiaEEzRWFaanZnR3BpMUFuV21TRXpQaGhlSTU1TXpoZHdEVXRYY0FTMzZGayIsInkiOiJFN" +
            "DRwSnA3X21kNlo2LXlEbmdPSzlMWWFXX0xUQzhCWGdTVk1RQ1plTnJBIn0sInVzYWdlIjpbIm9wcyIsImF1dGgiLCJnZW5lcmFsI" +
            "l19XX19XX0\"}],\"service\":[{\"id\":\"#anyServiceEndpointId\",\"type\":\"LinkedDomains\",\"serviceEndpoint\":\"htt" +
            "p://any.endpoint\"}],\"verificationMethod\":[{\"id\":\"#Va8_sign_ION_1\",\"controller\":\"\",\"type\":\"EcdsaSecp2" +
            "56k1VerificationKey2019\",\"publicKeyJwk\":{\"kty\":\"EC\",\"crv\":\"secp256k1\",\"x\":\"hA3EaZjvgGpi1AnWmSEzPhheI" +
            "55MzhdwDUtXcAS36Fk\",\"y\":\"E44pJp7_md6Z6-yDngOK9LYaW_LTC8BXgSVMQCZeNrA\"}}],\"authentication\":[\"#Va8_sig" +
            "n_ION_1\"]},\"methodMetadata\":{\"operationPublicKeys\":[{\"id\":\"#Va8_sign_ION_1\",\"controller\":\"\",\"type\":\"" +
            "EcdsaSecp256k1VerificationKey2019\",\"publicKeyJwk\":{\"kty\":\"EC\",\"crv\":\"secp256k1\",\"x\":\"hA3EaZjvgGpi1An" +
            "WmSEzPhheI55MzhdwDUtXcAS36Fk\",\"y\":\"E44pJp7_md6Z6-yDngOK9LYaW_LTC8BXgSVMQCZeNrA\"}}],\"recoveryKey\":{\"k" +
            "ty\":\"EC\",\"crv\":\"secp256k1\",\"x\":\"MDa8v7M4-DN-Vy6ZYYlPYnPtwtTMjvE9H3-wXX2VJKc\",\"y\":\"BCpmR6pekJvBqEbpH4" +
            "LpnuLwSJLGdrNPr3AhWIGmbX4\"}},\"resolverMetadata\":{\"driverId\":\"did:ion\",\"driver\":\"HttpDriver\",\"retriev" +
            "ed\":\"2020-05-31T09:20:41.162Z\",\"duration\":\"118.4098ms\"}}"
        val wellKnownDoc =
            "eyJhbGciOiJFUzI1NksiLCJraWQiOiJkaWQ6aW9uOkVpRDg5aXV0OFBNaFJ6NXpTbFBsX3prSGdFZlJCaUE5emRYWXdBZHJrMV9K" +
            "WWc6ZXlKa1pXeDBZU0k2ZXlKd1lYUmphR1Z6SWpwYmV5SmhZM1JwYjI0aU9pSnlaWEJzWVdObElpd2laRzlqZFcxbGJuUWlPbnNp" +
            "Y0hWaWJHbGpYMnRsZVhNaU9sdDdJbWxrSWpvaWMybG5YMkZrT0RSa1pqSXpJaXdpYW5kcklqcDdJbU55ZGlJNkluTmxZM0F5TlRa" +
            "ck1TSXNJbXQwZVNJNklrVkRJaXdpZUNJNklsbE1NMnhsTkMxMllsTnNhMlpJTm5Kb1FXSmxiRUpYVERCeVZsQmllVkpZVEd0SVVt" +
            "eDNSRUZaT1RRaUxDSjVJam9pWVdGRFpHMWlkakYwUjFodFpVMVFXVWRVWTBacUxVRm5jMmQyYzJSTkxYbEdSMnRuVlUxUVgwNHRW" +
            "U0o5TENKd2RYSndiM05sSWpwYkltRjFkR2dpTENKblpXNWxjbUZzSWwwc0luUjVjR1VpT2lKRlkyUnpZVk5sWTNBeU5UWnJNVlps" +
            "Y21sbWFXTmhkR2x2Ymt0bGVUSXdNVGtpZlYwc0luTmxjblpwWTJWZlpXNWtjRzlwYm5SeklqcGJleUpsYm1Sd2IybHVkQ0k2ZXlK" +
            "dmNtbG5hVzV6SWpwYkltaDBkSEJ6T2k4dlkyOXVkRzl6YjNWdWFYWmxjbk5wZEhrdFpHbGtaR1Z0Ynk1aGVuVnlaWGRsWW5OcGRH" +
            "VnpMbTVsZEM4aVhYMHNJbWxrSWpvaWJHbHVhMlZrWkc5dFlXbHVjeUlzSW5SNWNHVWlPaUpNYVc1clpXUkViMjFoYVc1ekluMWRm" +
            "WDFkTENKMWNHUmhkR1ZmWTI5dGJXbDBiV1Z1ZENJNklrVnBRUzFIU0ZKUlZrY3hWMkYwYkdwclNWUXhPR3h2YldaWGJUWTBOMlF3" +
            "WW10bFJtcENSMnRNVWpaTVJYY2lmU3dpYzNWbVptbDRYMlJoZEdFaU9uc2laR1ZzZEdGZmFHRnphQ0k2SWtWcFFYQnRhMGRwYTJS" +
            "aVgzcEhiMUpyVlU1dmJtZDBUWEpsWlMxeFRqaFBlRXRzVm1KdllVazVaRzlEZFhjaUxDSnlaV052ZG1WeWVWOWpiMjF0YVhSdFpX" +
            "NTBJam9pUldsQ2IxOXZWa0pZU0VKbVpHaGZSRnBrWmpscVUzWndRMWxuUm1Oa2NYYzNjRFpGYTFwUGFuVTNiVVJFUVNKOWZRI3Np" +
            "Z19hZDg0ZGYyMyJ9.eyJzdWIiOiJkaWQ6aW9uOkVpRDg5aXV0OFBNaFJ6NXpTbFBsX3prSGdFZlJCaUE5emRYWXdBZHJrMV9KWWc" +
            "6ZXlKa1pXeDBZU0k2ZXlKd1lYUmphR1Z6SWpwYmV5SmhZM1JwYjI0aU9pSnlaWEJzWVdObElpd2laRzlqZFcxbGJuUWlPbnNpY0h" +
            "WaWJHbGpYMnRsZVhNaU9sdDdJbWxrSWpvaWMybG5YMkZrT0RSa1pqSXpJaXdpYW5kcklqcDdJbU55ZGlJNkluTmxZM0F5TlRack1" +
            "TSXNJbXQwZVNJNklrVkRJaXdpZUNJNklsbE1NMnhsTkMxMllsTnNhMlpJTm5Kb1FXSmxiRUpYVERCeVZsQmllVkpZVEd0SVVteDN" +
            "SRUZaT1RRaUxDSjVJam9pWVdGRFpHMWlkakYwUjFodFpVMVFXVWRVWTBacUxVRm5jMmQyYzJSTkxYbEdSMnRuVlUxUVgwNHRWU0o" +
            "5TENKd2RYSndiM05sSWpwYkltRjFkR2dpTENKblpXNWxjbUZzSWwwc0luUjVjR1VpT2lKRlkyUnpZVk5sWTNBeU5UWnJNVlpsY21" +
            "sbWFXTmhkR2x2Ymt0bGVUSXdNVGtpZlYwc0luTmxjblpwWTJWZlpXNWtjRzlwYm5SeklqcGJleUpsYm1Sd2IybHVkQ0k2ZXlKdmN" +
            "tbG5hVzV6SWpwYkltaDBkSEJ6T2k4dlkyOXVkRzl6YjNWdWFYWmxjbk5wZEhrdFpHbGtaR1Z0Ynk1aGVuVnlaWGRsWW5OcGRHVnp" +
            "MbTVsZEM4aVhYMHNJbWxrSWpvaWJHbHVhMlZrWkc5dFlXbHVjeUlzSW5SNWNHVWlPaUpNYVc1clpXUkViMjFoYVc1ekluMWRmWDF" +
            "kTENKMWNHUmhkR1ZmWTI5dGJXbDBiV1Z1ZENJNklrVnBRUzFIU0ZKUlZrY3hWMkYwYkdwclNWUXhPR3h2YldaWGJUWTBOMlF3WW1" +
            "0bFJtcENSMnRNVWpaTVJYY2lmU3dpYzNWbVptbDRYMlJoZEdFaU9uc2laR1ZzZEdGZmFHRnphQ0k2SWtWcFFYQnRhMGRwYTJSaVg" +
            "zcEhiMUpyVlU1dmJtZDBUWEpsWlMxeFRqaFBlRXRzVm1KdllVazVaRzlEZFhjaUxDSnlaV052ZG1WeWVWOWpiMjF0YVhSdFpXNTB" +
            "Jam9pUldsQ2IxOXZWa0pZU0VKbVpHaGZSRnBrWmpscVUzWndRMWxuUm1Oa2NYYzNjRFpGYTFwUGFuVTNiVVJFUVNKOWZRIiwiaXN" +
            "zIjoiZGlkOmlvbjpFaUQ4OWl1dDhQTWhSejV6U2xQbF96a0hnRWZSQmlBOXpkWFl3QWRyazFfSllnOmV5SmtaV3gwWVNJNmV5Snd" +
            "ZWFJqYUdWeklqcGJleUpoWTNScGIyNGlPaUp5WlhCc1lXTmxJaXdpWkc5amRXMWxiblFpT25zaWNIVmliR2xqWDJ0bGVYTWlPbHQ" +
            "3SW1sa0lqb2ljMmxuWDJGa09EUmtaakl6SWl3aWFuZHJJanA3SW1OeWRpSTZJbk5sWTNBeU5UWnJNU0lzSW10MGVTSTZJa1ZESWl" +
            "3aWVDSTZJbGxNTTJ4bE5DMTJZbE5zYTJaSU5uSm9RV0psYkVKWFREQnlWbEJpZVZKWVRHdElVbXgzUkVGWk9UUWlMQ0o1SWpvaVl" +
            "XRkRaRzFpZGpGMFIxaHRaVTFRV1VkVVkwWnFMVUZuYzJkMmMyUk5MWGxHUjJ0blZVMVFYMDR0VlNKOUxDSndkWEp3YjNObElqcGJ" +
            "JbUYxZEdnaUxDSm5aVzVsY21Gc0lsMHNJblI1Y0dVaU9pSkZZMlJ6WVZObFkzQXlOVFpyTVZabGNtbG1hV05oZEdsdmJrdGxlVEl" +
            "3TVRraWZWMHNJbk5sY25acFkyVmZaVzVrY0c5cGJuUnpJanBiZXlKbGJtUndiMmx1ZENJNmV5SnZjbWxuYVc1eklqcGJJbWgwZEh" +
            "Cek9pOHZZMjl1ZEc5emIzVnVhWFpsY25OcGRIa3RaR2xrWkdWdGJ5NWhlblZ5WlhkbFluTnBkR1Z6TG01bGRDOGlYWDBzSW1sa0l" +
            "qb2liR2x1YTJWa1pHOXRZV2x1Y3lJc0luUjVjR1VpT2lKTWFXNXJaV1JFYjIxaGFXNXpJbjFkZlgxZExDSjFjR1JoZEdWZlkyOXR" +
            "iV2wwYldWdWRDSTZJa1ZwUVMxSFNGSlJWa2N4VjJGMGJHcHJTVlF4T0d4dmJXWlhiVFkwTjJRd1ltdGxSbXBDUjJ0TVVqWk1SWGN" +
            "pZlN3aWMzVm1abWw0WDJSaGRHRWlPbnNpWkdWc2RHRmZhR0Z6YUNJNklrVnBRWEJ0YTBkcGEyUmlYM3BIYjFKclZVNXZibWQwVFh" +
            "KbFpTMXhUamhQZUV0c1ZtSnZZVWs1Wkc5RGRYY2lMQ0p5WldOdmRtVnllVjlqYjIxdGFYUnRaVzUwSWpvaVJXbENiMTl2VmtKWVN" +
            "FSm1aR2hmUkZwa1pqbHFVM1p3UTFsblJtTmtjWGMzY0RaRmExcFBhblUzYlVSRVFTSjlmUSIsIm5iZiI6MTYwNzk4ODE5OSwiZXh" +
            "wIjoyMzk2OTA2NTk5LCJ2YyI6eyJAY29udGV4dCI6WyJodHRwczovL3d3dy53My5vcmcvMjAxOC9jcmVkZW50aWFscy92MSIsImh" +
            "0dHBzOi8vaWRlbnRpdHkuZm91bmRhdGlvbi8ud2VsbC1rbm93bi9jb250ZXh0cy9kaWQtY29uZmlndXJhdGlvbi12MC4wLmpzb25" +
            "sZCJdLCJpc3N1ZXIiOiJkaWQ6aW9uOkVpRDg5aXV0OFBNaFJ6NXpTbFBsX3prSGdFZlJCaUE5emRYWXdBZHJrMV9KWWc6ZXlKa1p" +
            "XeDBZU0k2ZXlKd1lYUmphR1Z6SWpwYmV5SmhZM1JwYjI0aU9pSnlaWEJzWVdObElpd2laRzlqZFcxbGJuUWlPbnNpY0hWaWJHbGp" +
            "YMnRsZVhNaU9sdDdJbWxrSWpvaWMybG5YMkZrT0RSa1pqSXpJaXdpYW5kcklqcDdJbU55ZGlJNkluTmxZM0F5TlRack1TSXNJbXQ" +
            "wZVNJNklrVkRJaXdpZUNJNklsbE1NMnhsTkMxMllsTnNhMlpJTm5Kb1FXSmxiRUpYVERCeVZsQmllVkpZVEd0SVVteDNSRUZaT1R" +
            "RaUxDSjVJam9pWVdGRFpHMWlkakYwUjFodFpVMVFXVWRVWTBacUxVRm5jMmQyYzJSTkxYbEdSMnRuVlUxUVgwNHRWU0o5TENKd2R" +
            "YSndiM05sSWpwYkltRjFkR2dpTENKblpXNWxjbUZzSWwwc0luUjVjR1VpT2lKRlkyUnpZVk5sWTNBeU5UWnJNVlpsY21sbWFXTmh" +
            "kR2x2Ymt0bGVUSXdNVGtpZlYwc0luTmxjblpwWTJWZlpXNWtjRzlwYm5SeklqcGJleUpsYm1Sd2IybHVkQ0k2ZXlKdmNtbG5hVzV" +
            "6SWpwYkltaDBkSEJ6T2k4dlkyOXVkRzl6YjNWdWFYWmxjbk5wZEhrdFpHbGtaR1Z0Ynk1aGVuVnlaWGRsWW5OcGRHVnpMbTVsZEM" +
            "4aVhYMHNJbWxrSWpvaWJHbHVhMlZrWkc5dFlXbHVjeUlzSW5SNWNHVWlPaUpNYVc1clpXUkViMjFoYVc1ekluMWRmWDFkTENKMWN" +
            "HUmhkR1ZmWTI5dGJXbDBiV1Z1ZENJNklrVnBRUzFIU0ZKUlZrY3hWMkYwYkdwclNWUXhPR3h2YldaWGJUWTBOMlF3WW10bFJtcEN" +
            "SMnRNVWpaTVJYY2lmU3dpYzNWbVptbDRYMlJoZEdFaU9uc2laR1ZzZEdGZmFHRnphQ0k2SWtWcFFYQnRhMGRwYTJSaVgzcEhiMUp" +
            "yVlU1dmJtZDBUWEpsWlMxeFRqaFBlRXRzVm1KdllVazVaRzlEZFhjaUxDSnlaV052ZG1WeWVWOWpiMjF0YVhSdFpXNTBJam9pUld" +
            "sQ2IxOXZWa0pZU0VKbVpHaGZSRnBrWmpscVUzWndRMWxuUm1Oa2NYYzNjRFpGYTFwUGFuVTNiVVJFUVNKOWZRIiwiaXNzdWFuY2V" +
            "EYXRlIjoiMjAyMC0xMi0xNFQyMzoyMzoxOS40NzZaIiwiZXhwaXJhdGlvbkRhdGUiOiIyMDQ1LTEyLTE0VDIzOjIzOjE5LjQ3Nlo" +
            "iLCJ0eXBlIjpbIlZlcmlmaWFibGVDcmVkZW50aWFsIiwiRG9tYWluTGlua2FnZUNyZWRlbnRpYWwiXSwiY3JlZGVudGlhbFN1Ymp" +
            "lY3QiOnsiaWQiOiJkaWQ6aW9uOkVpRDg5aXV0OFBNaFJ6NXpTbFBsX3prSGdFZlJCaUE5emRYWXdBZHJrMV9KWWc6ZXlKa1pXeDB" +
            "ZU0k2ZXlKd1lYUmphR1Z6SWpwYmV5SmhZM1JwYjI0aU9pSnlaWEJzWVdObElpd2laRzlqZFcxbGJuUWlPbnNpY0hWaWJHbGpYMnR" +
            "sZVhNaU9sdDdJbWxrSWpvaWMybG5YMkZrT0RSa1pqSXpJaXdpYW5kcklqcDdJbU55ZGlJNkluTmxZM0F5TlRack1TSXNJbXQwZVN" +
            "JNklrVkRJaXdpZUNJNklsbE1NMnhsTkMxMllsTnNhMlpJTm5Kb1FXSmxiRUpYVERCeVZsQmllVkpZVEd0SVVteDNSRUZaT1RRaUx" +
            "DSjVJam9pWVdGRFpHMWlkakYwUjFodFpVMVFXVWRVWTBacUxVRm5jMmQyYzJSTkxYbEdSMnRuVlUxUVgwNHRWU0o5TENKd2RYSnd" +
            "iM05sSWpwYkltRjFkR2dpTENKblpXNWxjbUZzSWwwc0luUjVjR1VpT2lKRlkyUnpZVk5sWTNBeU5UWnJNVlpsY21sbWFXTmhkR2x" +
            "2Ymt0bGVUSXdNVGtpZlYwc0luTmxjblpwWTJWZlpXNWtjRzlwYm5SeklqcGJleUpsYm1Sd2IybHVkQ0k2ZXlKdmNtbG5hVzV6SWp" +
            "wYkltaDBkSEJ6T2k4dlkyOXVkRzl6YjNWdWFYWmxjbk5wZEhrdFpHbGtaR1Z0Ynk1aGVuVnlaWGRsWW5OcGRHVnpMbTVsZEM4aVh" +
            "YMHNJbWxrSWpvaWJHbHVhMlZrWkc5dFlXbHVjeUlzSW5SNWNHVWlPaUpNYVc1clpXUkViMjFoYVc1ekluMWRmWDFkTENKMWNHUmh" +
            "kR1ZmWTI5dGJXbDBiV1Z1ZENJNklrVnBRUzFIU0ZKUlZrY3hWMkYwYkdwclNWUXhPR3h2YldaWGJUWTBOMlF3WW10bFJtcENSMnR" +
            "NVWpaTVJYY2lmU3dpYzNWbVptbDRYMlJoZEdFaU9uc2laR1ZzZEdGZmFHRnphQ0k2SWtWcFFYQnRhMGRwYTJSaVgzcEhiMUpyVlU" +
            "1dmJtZDBUWEpsWlMxeFRqaFBlRXRzVm1KdllVazVaRzlEZFhjaUxDSnlaV052ZG1WeWVWOWpiMjF0YVhSdFpXNTBJam9pUldsQ2I" +
            "xOXZWa0pZU0VKbVpHaGZSRnBrWmpscVUzWndRMWxuUm1Oa2NYYzNjRFpGYTFwUGFuVTNiVVJFUVNKOWZRIiwib3JpZ2luIjoiaHR" +
            "0cHM6Ly9jb250b3NvdW5pdmVyc2l0eS1kaWRkZW1vLmF6dXJld2Vic2l0ZXMubmV0LyJ9fX0.PPRATSXicMcMIGe8Y_c8YZIA9VY" +
            "XZsu8UMvfG6ZtZ0Hp_XVc9N9ZzxemAxCMj9wkz-l5ssuTgYOSzLQdXLP1Zw"
        coEvery { linkedDomainsService.resolveIdentifierDocument(rpDid) } returns KotlinResult.success(
            defaultTestSerializer.decodeFromString(
                IdentifierResponse.serializer(),
                rpDidDoc
            ).didDocument
        )
        coEvery { mockedJwtValidator.verifySignature(any()) } returns false
        coEvery { linkedDomainsService["getWellKnownConfigDocument"]("http://any.endpoint") } returns KotlinResult.success(
            LinkedDomainsResponse(
                "",
                listOf(wellKnownDoc)
            )
        )

        runBlocking {
            val actualLinkedDomainsResultResponse = linkedDomainsService.fetchDocumentAndVerifyLinkedDomains(rpDid)
            assertThat(actualLinkedDomainsResultResponse).isInstanceOf(KotlinResult.success(LinkedDomainUnVerified)::class.java)
        }
    }

    @Test
    fun `test linked domains not matching case mocking network call`() {
        val rpDid =
            "did:ion:EiBGMAR43ZWai9JKvh31ShjvNaX0dYRYI0XtLnZIa88CqQ?-ion-initial-state=eyJkZWx0YV9oYXNoIjoiRWlCYl" +
            "lRMEViTmNtMnJkc2dBMndJM0FRQTRWRlFSUTVYSnk5QzFTOWV3elFBQSIsInJlY292ZXJ5X2tleSI6eyJrdHkiOiJFQyIsImNydi" +
            "I6InNlY3AyNTZrMSIsIngiOiJNRGE4djdNNC1ETi1WeTZaWVlsUFluUHR3dFRNanZFOUgzLXdYWDJWSktjIiwieSI6IkJDcG1SNn" +
            "Bla0p2QnFFYnBINExwbnVMd1NKTEdkck5QcjNBaFdJR21iWDQifSwicmVjb3ZlcnlfY29tbWl0bWVudCI6IkVpREM3Y0J2Unp3US" +
            "1CZkhSQ2x1dG5MQU8wUlZyS3V6c0pTSEFtXzhFTERocVEifQ.eyJ1cGRhdGVfY29tbWl0bWVudCI6IkVpQXNsRml6MWNRU3Rranl" +
            "uUy03eERUbGtLOExvZ01FLWc2bkFBXy0wREVyWGciLCJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOns" +
            "icHVibGljS2V5cyI6W3siaWQiOiJWYThfc2lnbl9JT05fMSIsInR5cGUiOiJFY2RzYVNlY3AyNTZrMVZlcmlmaWNhdGlvbktleTI" +
            "wMTkiLCJqd2siOnsia3R5IjoiRUMiLCJjcnYiOiJzZWNwMjU2azEiLCJ4IjoiaEEzRWFaanZnR3BpMUFuV21TRXpQaGhlSTU1TXp" +
            "oZHdEVXRYY0FTMzZGayIsInkiOiJFNDRwSnA3X21kNlo2LXlEbmdPSzlMWWFXX0xUQzhCWGdTVk1RQ1plTnJBIn0sInVzYWdlIjp" +
            "bIm9wcyIsImF1dGgiLCJnZW5lcmFsIl19XX19XX0"
        val rpDidDoc =
            "{\"@context\":\"https://www.w3.org/ns/did-resolution/v1\",\"didDocument\":{\"id\":\"did:ion:EiBGMAR43ZWai9JKv" +
            "h31ShjvNaX0dYRYI0XtLnZIa88CqQ?-ion-initial-state=eyJkZWx0YV9oYXNoIjoiRWlCYllRMEViTmNtMnJkc2dBMndJM0F" +
            "RQTRWRlFSUTVYSnk5QzFTOWV3elFBQSIsInJlY292ZXJ5X2tleSI6eyJrdHkiOiJFQyIsImNydiI6InNlY3AyNTZrMSIsIngiOiJ" +
            "NRGE4djdNNC1ETi1WeTZaWVlsUFluUHR3dFRNanZFOUgzLXdYWDJWSktjIiwieSI6IkJDcG1SNnBla0p2QnFFYnBINExwbnVMd1N" +
            "KTEdkck5QcjNBaFdJR21iWDQifSwicmVjb3ZlcnlfY29tbWl0bWVudCI6IkVpREM3Y0J2Unp3US1CZkhSQ2x1dG5MQU8wUlZyS3V" +
            "6c0pTSEFtXzhFTERocVEifQ.eyJ1cGRhdGVfY29tbWl0bWVudCI6IkVpQXNsRml6MWNRU3RranluUy03eERUbGtLOExvZ01FLWc2" +
            "bkFBXy0wREVyWGciLCJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQi" +
            "OiJWYThfc2lnbl9JT05fMSIsInR5cGUiOiJFY2RzYVNlY3AyNTZrMVZlcmlmaWNhdGlvbktleTIwMTkiLCJqd2siOnsia3R5Ijoi" +
            "RUMiLCJjcnYiOiJzZWNwMjU2azEiLCJ4IjoiaEEzRWFaanZnR3BpMUFuV21TRXpQaGhlSTU1TXpoZHdEVXRYY0FTMzZGayIsInki" +
            "OiJFNDRwSnA3X21kNlo2LXlEbmdPSzlMWWFXX0xUQzhCWGdTVk1RQ1plTnJBIn0sInVzYWdlIjpbIm9wcyIsImF1dGgiLCJnZW5l" +
            "cmFsIl19XX19XX0\",\"@context\":[\"https://www.w3.org/ns/did/v1\",{\"@base\":\"did:ion:EiBGMAR43ZWai9JKvh31Sh" +
            "jvNaX0dYRYI0XtLnZIa88CqQ?-ion-initial-state=eyJkZWx0YV9oYXNoIjoiRWlCYllRMEViTmNtMnJkc2dBMndJM0FRQTRW" +
            "RlFSUTVYSnk5QzFTOWV3elFBQSIsInJlY292ZXJ5X2tleSI6eyJrdHkiOiJFQyIsImNydiI6InNlY3AyNTZrMSIsIngiOiJNRGE4" +
            "djdNNC1ETi1WeTZaWVlsUFluUHR3dFRNanZFOUgzLXdYWDJWSktjIiwieSI6IkJDcG1SNnBla0p2QnFFYnBINExwbnVMd1NKTEdk" +
            "ck5QcjNBaFdJR21iWDQifSwicmVjb3ZlcnlfY29tbWl0bWVudCI6IkVpREM3Y0J2Unp3US1CZkhSQ2x1dG5MQU8wUlZyS3V6c0pT" +
            "SEFtXzhFTERocVEifQ.eyJ1cGRhdGVfY29tbWl0bWVudCI6IkVpQXNsRml6MWNRU3RranluUy03eERUbGtLOExvZ01FLWc2bkFBX" +
            "y0wREVyWGciLCJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJWY" +
            "Thfc2lnbl9JT05fMSIsInR5cGUiOiJFY2RzYVNlY3AyNTZrMVZlcmlmaWNhdGlvbktleTIwMTkiLCJqd2siOnsia3R5IjoiRUMiL" +
            "CJjcnYiOiJzZWNwMjU2azEiLCJ4IjoiaEEzRWFaanZnR3BpMUFuV21TRXpQaGhlSTU1TXpoZHdEVXRYY0FTMzZGayIsInkiOiJFN" +
            "DRwSnA3X21kNlo2LXlEbmdPSzlMWWFXX0xUQzhCWGdTVk1RQ1plTnJBIn0sInVzYWdlIjpbIm9wcyIsImF1dGgiLCJnZW5lcmFsI" +
            "l19XX19XX0\"}],\"service\":[{\"id\":\"#anyServiceEndpointId\",\"type\":\"LinkedDomains\",\"serviceEndpoint\":\"htt" +
            "p://any.endpoint\"}],\"verificationMethod\":[{\"id\":\"#Va8_sign_ION_1\",\"controller\":\"\",\"type\":\"EcdsaSecp2" +
            "56k1VerificationKey2019\",\"publicKeyJwk\":{\"kty\":\"EC\",\"crv\":\"secp256k1\",\"x\":\"hA3EaZjvgGpi1AnWmSEzPhheI" +
            "55MzhdwDUtXcAS36Fk\",\"y\":\"E44pJp7_md6Z6-yDngOK9LYaW_LTC8BXgSVMQCZeNrA\"}}],\"authentication\":[\"#Va8_sig" +
            "n_ION_1\"]},\"methodMetadata\":{\"operationPublicKeys\":[{\"id\":\"#Va8_sign_ION_1\",\"controller\":\"\",\"type\":\"" +
            "EcdsaSecp256k1VerificationKey2019\",\"publicKeyJwk\":{\"kty\":\"EC\",\"crv\":\"secp256k1\",\"x\":\"hA3EaZjvgGpi1An" +
            "WmSEzPhheI55MzhdwDUtXcAS36Fk\",\"y\":\"E44pJp7_md6Z6-yDngOK9LYaW_LTC8BXgSVMQCZeNrA\"}}],\"recoveryKey\":{\"k" +
            "ty\":\"EC\",\"crv\":\"secp256k1\",\"x\":\"MDa8v7M4-DN-Vy6ZYYlPYnPtwtTMjvE9H3-wXX2VJKc\",\"y\":\"BCpmR6pekJvBqEbpH4" +
            "LpnuLwSJLGdrNPr3AhWIGmbX4\"}},\"resolverMetadata\":{\"driverId\":\"did:ion\",\"driver\":\"HttpDriver\",\"retriev" +
            "ed\":\"2020-05-31T09:20:41.162Z\",\"duration\":\"118.4098ms\"}}"
        val wellKnownDoc =
            "eyJhbGciOiJFUzI1NksiLCJraWQiOiJkaWQ6aW9uOkVpRDg5aXV0OFBNaFJ6NXpTbFBsX3prSGdFZlJCaUE5emRYWXdBZHJrMV9K" +
            "WWc6ZXlKa1pXeDBZU0k2ZXlKd1lYUmphR1Z6SWpwYmV5SmhZM1JwYjI0aU9pSnlaWEJzWVdObElpd2laRzlqZFcxbGJuUWlPbnNp" +
            "Y0hWaWJHbGpYMnRsZVhNaU9sdDdJbWxrSWpvaWMybG5YMkZrT0RSa1pqSXpJaXdpYW5kcklqcDdJbU55ZGlJNkluTmxZM0F5TlRa" +
            "ck1TSXNJbXQwZVNJNklrVkRJaXdpZUNJNklsbE1NMnhsTkMxMllsTnNhMlpJTm5Kb1FXSmxiRUpYVERCeVZsQmllVkpZVEd0SVVt" +
            "eDNSRUZaT1RRaUxDSjVJam9pWVdGRFpHMWlkakYwUjFodFpVMVFXVWRVWTBacUxVRm5jMmQyYzJSTkxYbEdSMnRuVlUxUVgwNHRW" +
            "U0o5TENKd2RYSndiM05sSWpwYkltRjFkR2dpTENKblpXNWxjbUZzSWwwc0luUjVjR1VpT2lKRlkyUnpZVk5sWTNBeU5UWnJNVlps" +
            "Y21sbWFXTmhkR2x2Ymt0bGVUSXdNVGtpZlYwc0luTmxjblpwWTJWZlpXNWtjRzlwYm5SeklqcGJleUpsYm1Sd2IybHVkQ0k2ZXlK" +
            "dmNtbG5hVzV6SWpwYkltaDBkSEJ6T2k4dlkyOXVkRzl6YjNWdWFYWmxjbk5wZEhrdFpHbGtaR1Z0Ynk1aGVuVnlaWGRsWW5OcGRH" +
            "VnpMbTVsZEM4aVhYMHNJbWxrSWpvaWJHbHVhMlZrWkc5dFlXbHVjeUlzSW5SNWNHVWlPaUpNYVc1clpXUkViMjFoYVc1ekluMWRm" +
            "WDFkTENKMWNHUmhkR1ZmWTI5dGJXbDBiV1Z1ZENJNklrVnBRUzFIU0ZKUlZrY3hWMkYwYkdwclNWUXhPR3h2YldaWGJUWTBOMlF3" +
            "WW10bFJtcENSMnRNVWpaTVJYY2lmU3dpYzNWbVptbDRYMlJoZEdFaU9uc2laR1ZzZEdGZmFHRnphQ0k2SWtWcFFYQnRhMGRwYTJS" +
            "aVgzcEhiMUpyVlU1dmJtZDBUWEpsWlMxeFRqaFBlRXRzVm1KdllVazVaRzlEZFhjaUxDSnlaV052ZG1WeWVWOWpiMjF0YVhSdFpX" +
            "NTBJam9pUldsQ2IxOXZWa0pZU0VKbVpHaGZSRnBrWmpscVUzWndRMWxuUm1Oa2NYYzNjRFpGYTFwUGFuVTNiVVJFUVNKOWZRI3Np" +
            "Z19hZDg0ZGYyMyJ9.eyJzdWIiOiJkaWQ6aW9uOkVpRDg5aXV0OFBNaFJ6NXpTbFBsX3prSGdFZlJCaUE5emRYWXdBZHJrMV9KWWc" +
            "6ZXlKa1pXeDBZU0k2ZXlKd1lYUmphR1Z6SWpwYmV5SmhZM1JwYjI0aU9pSnlaWEJzWVdObElpd2laRzlqZFcxbGJuUWlPbnNpY0h" +
            "WaWJHbGpYMnRsZVhNaU9sdDdJbWxrSWpvaWMybG5YMkZrT0RSa1pqSXpJaXdpYW5kcklqcDdJbU55ZGlJNkluTmxZM0F5TlRack1" +
            "TSXNJbXQwZVNJNklrVkRJaXdpZUNJNklsbE1NMnhsTkMxMllsTnNhMlpJTm5Kb1FXSmxiRUpYVERCeVZsQmllVkpZVEd0SVVteDN" +
            "SRUZaT1RRaUxDSjVJam9pWVdGRFpHMWlkakYwUjFodFpVMVFXVWRVWTBacUxVRm5jMmQyYzJSTkxYbEdSMnRuVlUxUVgwNHRWU0o" +
            "5TENKd2RYSndiM05sSWpwYkltRjFkR2dpTENKblpXNWxjbUZzSWwwc0luUjVjR1VpT2lKRlkyUnpZVk5sWTNBeU5UWnJNVlpsY21" +
            "sbWFXTmhkR2x2Ymt0bGVUSXdNVGtpZlYwc0luTmxjblpwWTJWZlpXNWtjRzlwYm5SeklqcGJleUpsYm1Sd2IybHVkQ0k2ZXlKdmN" +
            "tbG5hVzV6SWpwYkltaDBkSEJ6T2k4dlkyOXVkRzl6YjNWdWFYWmxjbk5wZEhrdFpHbGtaR1Z0Ynk1aGVuVnlaWGRsWW5OcGRHVnp" +
            "MbTVsZEM4aVhYMHNJbWxrSWpvaWJHbHVhMlZrWkc5dFlXbHVjeUlzSW5SNWNHVWlPaUpNYVc1clpXUkViMjFoYVc1ekluMWRmWDF" +
            "kTENKMWNHUmhkR1ZmWTI5dGJXbDBiV1Z1ZENJNklrVnBRUzFIU0ZKUlZrY3hWMkYwYkdwclNWUXhPR3h2YldaWGJUWTBOMlF3WW1" +
            "0bFJtcENSMnRNVWpaTVJYY2lmU3dpYzNWbVptbDRYMlJoZEdFaU9uc2laR1ZzZEdGZmFHRnphQ0k2SWtWcFFYQnRhMGRwYTJSaVg" +
            "zcEhiMUpyVlU1dmJtZDBUWEpsWlMxeFRqaFBlRXRzVm1KdllVazVaRzlEZFhjaUxDSnlaV052ZG1WeWVWOWpiMjF0YVhSdFpXNTB" +
            "Jam9pUldsQ2IxOXZWa0pZU0VKbVpHaGZSRnBrWmpscVUzWndRMWxuUm1Oa2NYYzNjRFpGYTFwUGFuVTNiVVJFUVNKOWZRIiwiaXN" +
            "zIjoiZGlkOmlvbjpFaUQ4OWl1dDhQTWhSejV6U2xQbF96a0hnRWZSQmlBOXpkWFl3QWRyazFfSllnOmV5SmtaV3gwWVNJNmV5Snd" +
            "ZWFJqYUdWeklqcGJleUpoWTNScGIyNGlPaUp5WlhCc1lXTmxJaXdpWkc5amRXMWxiblFpT25zaWNIVmliR2xqWDJ0bGVYTWlPbHQ" +
            "3SW1sa0lqb2ljMmxuWDJGa09EUmtaakl6SWl3aWFuZHJJanA3SW1OeWRpSTZJbk5sWTNBeU5UWnJNU0lzSW10MGVTSTZJa1ZESWl" +
            "3aWVDSTZJbGxNTTJ4bE5DMTJZbE5zYTJaSU5uSm9RV0psYkVKWFREQnlWbEJpZVZKWVRHdElVbXgzUkVGWk9UUWlMQ0o1SWpvaVl" +
            "XRkRaRzFpZGpGMFIxaHRaVTFRV1VkVVkwWnFMVUZuYzJkMmMyUk5MWGxHUjJ0blZVMVFYMDR0VlNKOUxDSndkWEp3YjNObElqcGJ" +
            "JbUYxZEdnaUxDSm5aVzVsY21Gc0lsMHNJblI1Y0dVaU9pSkZZMlJ6WVZObFkzQXlOVFpyTVZabGNtbG1hV05oZEdsdmJrdGxlVEl" +
            "3TVRraWZWMHNJbk5sY25acFkyVmZaVzVrY0c5cGJuUnpJanBiZXlKbGJtUndiMmx1ZENJNmV5SnZjbWxuYVc1eklqcGJJbWgwZEh" +
            "Cek9pOHZZMjl1ZEc5emIzVnVhWFpsY25OcGRIa3RaR2xrWkdWdGJ5NWhlblZ5WlhkbFluTnBkR1Z6TG01bGRDOGlYWDBzSW1sa0l" +
            "qb2liR2x1YTJWa1pHOXRZV2x1Y3lJc0luUjVjR1VpT2lKTWFXNXJaV1JFYjIxaGFXNXpJbjFkZlgxZExDSjFjR1JoZEdWZlkyOXR" +
            "iV2wwYldWdWRDSTZJa1ZwUVMxSFNGSlJWa2N4VjJGMGJHcHJTVlF4T0d4dmJXWlhiVFkwTjJRd1ltdGxSbXBDUjJ0TVVqWk1SWGN" +
            "pZlN3aWMzVm1abWw0WDJSaGRHRWlPbnNpWkdWc2RHRmZhR0Z6YUNJNklrVnBRWEJ0YTBkcGEyUmlYM3BIYjFKclZVNXZibWQwVFh" +
            "KbFpTMXhUamhQZUV0c1ZtSnZZVWs1Wkc5RGRYY2lMQ0p5WldOdmRtVnllVjlqYjIxdGFYUnRaVzUwSWpvaVJXbENiMTl2VmtKWVN" +
            "FSm1aR2hmUkZwa1pqbHFVM1p3UTFsblJtTmtjWGMzY0RaRmExcFBhblUzYlVSRVFTSjlmUSIsIm5iZiI6MTYwNzk4ODE5OSwiZXh" +
            "wIjoyMzk2OTA2NTk5LCJ2YyI6eyJAY29udGV4dCI6WyJodHRwczovL3d3dy53My5vcmcvMjAxOC9jcmVkZW50aWFscy92MSIsImh" +
            "0dHBzOi8vaWRlbnRpdHkuZm91bmRhdGlvbi8ud2VsbC1rbm93bi9jb250ZXh0cy9kaWQtY29uZmlndXJhdGlvbi12MC4wLmpzb25" +
            "sZCJdLCJpc3N1ZXIiOiJkaWQ6aW9uOkVpRDg5aXV0OFBNaFJ6NXpTbFBsX3prSGdFZlJCaUE5emRYWXdBZHJrMV9KWWc6ZXlKa1p" +
            "XeDBZU0k2ZXlKd1lYUmphR1Z6SWpwYmV5SmhZM1JwYjI0aU9pSnlaWEJzWVdObElpd2laRzlqZFcxbGJuUWlPbnNpY0hWaWJHbGp" +
            "YMnRsZVhNaU9sdDdJbWxrSWpvaWMybG5YMkZrT0RSa1pqSXpJaXdpYW5kcklqcDdJbU55ZGlJNkluTmxZM0F5TlRack1TSXNJbXQ" +
            "wZVNJNklrVkRJaXdpZUNJNklsbE1NMnhsTkMxMllsTnNhMlpJTm5Kb1FXSmxiRUpYVERCeVZsQmllVkpZVEd0SVVteDNSRUZaT1R" +
            "RaUxDSjVJam9pWVdGRFpHMWlkakYwUjFodFpVMVFXVWRVWTBacUxVRm5jMmQyYzJSTkxYbEdSMnRuVlUxUVgwNHRWU0o5TENKd2R" +
            "YSndiM05sSWpwYkltRjFkR2dpTENKblpXNWxjbUZzSWwwc0luUjVjR1VpT2lKRlkyUnpZVk5sWTNBeU5UWnJNVlpsY21sbWFXTmh" +
            "kR2x2Ymt0bGVUSXdNVGtpZlYwc0luTmxjblpwWTJWZlpXNWtjRzlwYm5SeklqcGJleUpsYm1Sd2IybHVkQ0k2ZXlKdmNtbG5hVzV" +
            "6SWpwYkltaDBkSEJ6T2k4dlkyOXVkRzl6YjNWdWFYWmxjbk5wZEhrdFpHbGtaR1Z0Ynk1aGVuVnlaWGRsWW5OcGRHVnpMbTVsZEM" +
            "4aVhYMHNJbWxrSWpvaWJHbHVhMlZrWkc5dFlXbHVjeUlzSW5SNWNHVWlPaUpNYVc1clpXUkViMjFoYVc1ekluMWRmWDFkTENKMWN" +
            "HUmhkR1ZmWTI5dGJXbDBiV1Z1ZENJNklrVnBRUzFIU0ZKUlZrY3hWMkYwYkdwclNWUXhPR3h2YldaWGJUWTBOMlF3WW10bFJtcEN" +
            "SMnRNVWpaTVJYY2lmU3dpYzNWbVptbDRYMlJoZEdFaU9uc2laR1ZzZEdGZmFHRnphQ0k2SWtWcFFYQnRhMGRwYTJSaVgzcEhiMUp" +
            "yVlU1dmJtZDBUWEpsWlMxeFRqaFBlRXRzVm1KdllVazVaRzlEZFhjaUxDSnlaV052ZG1WeWVWOWpiMjF0YVhSdFpXNTBJam9pUld" +
            "sQ2IxOXZWa0pZU0VKbVpHaGZSRnBrWmpscVUzWndRMWxuUm1Oa2NYYzNjRFpGYTFwUGFuVTNiVVJFUVNKOWZRIiwiaXNzdWFuY2V" +
            "EYXRlIjoiMjAyMC0xMi0xNFQyMzoyMzoxOS40NzZaIiwiZXhwaXJhdGlvbkRhdGUiOiIyMDQ1LTEyLTE0VDIzOjIzOjE5LjQ3Nlo" +
            "iLCJ0eXBlIjpbIlZlcmlmaWFibGVDcmVkZW50aWFsIiwiRG9tYWluTGlua2FnZUNyZWRlbnRpYWwiXSwiY3JlZGVudGlhbFN1Ymp" +
            "lY3QiOnsiaWQiOiJkaWQ6aW9uOkVpRDg5aXV0OFBNaFJ6NXpTbFBsX3prSGdFZlJCaUE5emRYWXdBZHJrMV9KWWc6ZXlKa1pXeDB" +
            "ZU0k2ZXlKd1lYUmphR1Z6SWpwYmV5SmhZM1JwYjI0aU9pSnlaWEJzWVdObElpd2laRzlqZFcxbGJuUWlPbnNpY0hWaWJHbGpYMnR" +
            "sZVhNaU9sdDdJbWxrSWpvaWMybG5YMkZrT0RSa1pqSXpJaXdpYW5kcklqcDdJbU55ZGlJNkluTmxZM0F5TlRack1TSXNJbXQwZVN" +
            "JNklrVkRJaXdpZUNJNklsbE1NMnhsTkMxMllsTnNhMlpJTm5Kb1FXSmxiRUpYVERCeVZsQmllVkpZVEd0SVVteDNSRUZaT1RRaUx" +
            "DSjVJam9pWVdGRFpHMWlkakYwUjFodFpVMVFXVWRVWTBacUxVRm5jMmQyYzJSTkxYbEdSMnRuVlUxUVgwNHRWU0o5TENKd2RYSnd" +
            "iM05sSWpwYkltRjFkR2dpTENKblpXNWxjbUZzSWwwc0luUjVjR1VpT2lKRlkyUnpZVk5sWTNBeU5UWnJNVlpsY21sbWFXTmhkR2x" +
            "2Ymt0bGVUSXdNVGtpZlYwc0luTmxjblpwWTJWZlpXNWtjRzlwYm5SeklqcGJleUpsYm1Sd2IybHVkQ0k2ZXlKdmNtbG5hVzV6SWp" +
            "wYkltaDBkSEJ6T2k4dlkyOXVkRzl6YjNWdWFYWmxjbk5wZEhrdFpHbGtaR1Z0Ynk1aGVuVnlaWGRsWW5OcGRHVnpMbTVsZEM4aVh" +
            "YMHNJbWxrSWpvaWJHbHVhMlZrWkc5dFlXbHVjeUlzSW5SNWNHVWlPaUpNYVc1clpXUkViMjFoYVc1ekluMWRmWDFkTENKMWNHUmh" +
            "kR1ZmWTI5dGJXbDBiV1Z1ZENJNklrVnBRUzFIU0ZKUlZrY3hWMkYwYkdwclNWUXhPR3h2YldaWGJUWTBOMlF3WW10bFJtcENSMnR" +
            "NVWpaTVJYY2lmU3dpYzNWbVptbDRYMlJoZEdFaU9uc2laR1ZzZEdGZmFHRnphQ0k2SWtWcFFYQnRhMGRwYTJSaVgzcEhiMUpyVlU" +
            "1dmJtZDBUWEpsWlMxeFRqaFBlRXRzVm1KdllVazVaRzlEZFhjaUxDSnlaV052ZG1WeWVWOWpiMjF0YVhSdFpXNTBJam9pUldsQ2I" +
            "xOXZWa0pZU0VKbVpHaGZSRnBrWmpscVUzWndRMWxuUm1Oa2NYYzNjRFpGYTFwUGFuVTNiVVJFUVNKOWZRIiwib3JpZ2luIjoiaHR" +
            "0cHM6Ly9jb250b3NvdW5pdmVyc2l0eS1kaWRkZW1vLmF6dXJld2Vic2l0ZXMubmV0LyJ9fX0.PPRATSXicMcMIGe8Y_c8YZIA9VY" +
            "XZsu8UMvfG6ZtZ0Hp_XVc9N9ZzxemAxCMj9wkz-l5ssuTgYOSzLQdXLP1Zw"
        coEvery { linkedDomainsService.resolveIdentifierDocument(rpDid) } returns KotlinResult.success(
            defaultTestSerializer.decodeFromString(
                IdentifierResponse.serializer(),
                rpDidDoc
            ).didDocument
        )
        coEvery { mockedJwtValidator.verifySignature(any()) } returns true
        coEvery { mockedJwtValidator.validateDidInHeaderAndPayload(any(), any()) } returns false
        coEvery { linkedDomainsService["getWellKnownConfigDocument"]("http://any.endpoint") } returns KotlinResult.success(
            LinkedDomainsResponse(
                "",
                listOf(wellKnownDoc)
            )
        )
        runBlocking {
            val actualLinkedDomainsResultResponse = linkedDomainsService.fetchDocumentAndVerifyLinkedDomains(rpDid)
            assertThat(actualLinkedDomainsResultResponse).isInstanceOf(KotlinResult.success(LinkedDomainUnVerified)::class.java)
        }
    }

    @Test
    fun `test using injected root of trust resolver and verifying successfully`() {
        // Arrange
        val mockRootOfTrustResolver = spyk(MockRootOfTrustResolver(true))
        val suppliedDidWithSingleServiceEndpoint =
            "did:ion:EiA8HR28m5KUig9elPRXkmKvvBGXcOoxpUrCscTdGJcIXQ?-ion-initial-state=eyJkZWx0YV9oYXNoIjoiRWlDVD" +
            "V5MG5nNTJCNzVjYWFqWU9qVjBRMmpxSng0NDZSajhRTjFpaHdteUpJZyIsInJlY292ZXJ5X2NvbW1pdG1lbnQiOiJFaURsOER4eX" +
            "ZLa3lvRmJsUWp0OXllU2J3TXkwR083MFM1R2FUU1F0UlF0aFJRIn0.eyJ1cGRhdGVfY29tbWl0bWVudCI6IkVpRGFXS2sycjJiSW" +
            "JsRWFpRUhtRU5Kc2h6czhtY1hJd2hTV0Z1YmtWQlJ3WWciLCJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbn" +
            "QiOnsicHVibGljX2tleXMiOlt7ImlkIjoic2lnX2VkMmM1ZWRmIiwidHlwZSI6IkVjZHNhU2VjcDI1NmsxVmVyaWZpY2F0aW9uS2" +
            "V5MjAxOSIsImp3ayI6eyJrdHkiOiJFQyIsImNydiI6InNlY3AyNTZrMSIsIngiOiJoTzhYaXhtWUxNOVVVMmFWOW9kc2VsSDNobD" +
            "JtbVFPLS1GTzNKa2JrekVrIiwieSI6InBDWEpxbXpUbzVQQkdRTERibnRtdUFaSElZQnFZOG1DZVdkaWhpb0tGUmMifSwicHVycG" +
            "9zZSI6WyJhdXRoIiwiZ2VuZXJhbCJdfV19fV19"
        val linkedDomainsService: LinkedDomainsService =
            spyk(
                LinkedDomainsService(
                    mockk(relaxed = true),
                    mockedResolver,
                    mockedJwtDomainLinkageCredentialValidator,
                    mockRootOfTrustResolver
                )
            )
        val didMetadata = DidMetadata()
        didMetadata.id = suppliedDidWithSingleServiceEndpoint
        val rpDidDoc =
            "{\"@context\":\"https://www.w3.org/ns/did-resolution/v1\",\"didDocument\":{\"id\":\"did:ion:EiBGMAR43ZWai9JKv" +
            "h31ShjvNaX0dYRYI0XtLnZIa88CqQ?-ion-initial-state=eyJkZWx0YV9oYXNoIjoiRWlCYllRMEViTmNtMnJkc2dBMndJM0F" +
            "RQTRWRlFSUTVYSnk5QzFTOWV3elFBQSIsInJlY292ZXJ5X2tleSI6eyJrdHkiOiJFQyIsImNydiI6InNlY3AyNTZrMSIsIngiOiJ" +
            "NRGE4djdNNC1ETi1WeTZaWVlsUFluUHR3dFRNanZFOUgzLXdYWDJWSktjIiwieSI6IkJDcG1SNnBla0p2QnFFYnBINExwbnVMd1N" +
            "KTEdkck5QcjNBaFdJR21iWDQifSwicmVjb3ZlcnlfY29tbWl0bWVudCI6IkVpREM3Y0J2Unp3US1CZkhSQ2x1dG5MQU8wUlZyS3V" +
            "6c0pTSEFtXzhFTERocVEifQ.eyJ1cGRhdGVfY29tbWl0bWVudCI6IkVpQXNsRml6MWNRU3RranluUy03eERUbGtLOExvZ01FLWc2" +
            "bkFBXy0wREVyWGciLCJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQi" +
            "OiJWYThfc2lnbl9JT05fMSIsInR5cGUiOiJFY2RzYVNlY3AyNTZrMVZlcmlmaWNhdGlvbktleTIwMTkiLCJqd2siOnsia3R5Ijoi" +
            "RUMiLCJjcnYiOiJzZWNwMjU2azEiLCJ4IjoiaEEzRWFaanZnR3BpMUFuV21TRXpQaGhlSTU1TXpoZHdEVXRYY0FTMzZGayIsInki" +
            "OiJFNDRwSnA3X21kNlo2LXlEbmdPSzlMWWFXX0xUQzhCWGdTVk1RQ1plTnJBIn0sInVzYWdlIjpbIm9wcyIsImF1dGgiLCJnZW5l" +
            "cmFsIl19XX19XX0\",\"@context\":[\"https://www.w3.org/ns/did/v1\",{\"@base\":\"did:ion:EiBGMAR43ZWai9JKvh31Sh" +
            "jvNaX0dYRYI0XtLnZIa88CqQ?-ion-initial-state=eyJkZWx0YV9oYXNoIjoiRWlCYllRMEViTmNtMnJkc2dBMndJM0FRQTRW" +
            "RlFSUTVYSnk5QzFTOWV3elFBQSIsInJlY292ZXJ5X2tleSI6eyJrdHkiOiJFQyIsImNydiI6InNlY3AyNTZrMSIsIngiOiJNRGE4" +
            "djdNNC1ETi1WeTZaWVlsUFluUHR3dFRNanZFOUgzLXdYWDJWSktjIiwieSI6IkJDcG1SNnBla0p2QnFFYnBINExwbnVMd1NKTEdk" +
            "ck5QcjNBaFdJR21iWDQifSwicmVjb3ZlcnlfY29tbWl0bWVudCI6IkVpREM3Y0J2Unp3US1CZkhSQ2x1dG5MQU8wUlZyS3V6c0pT" +
            "SEFtXzhFTERocVEifQ.eyJ1cGRhdGVfY29tbWl0bWVudCI6IkVpQXNsRml6MWNRU3RranluUy03eERUbGtLOExvZ01FLWc2bkFBX" +
            "y0wREVyWGciLCJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJWY" +
            "Thfc2lnbl9JT05fMSIsInR5cGUiOiJFY2RzYVNlY3AyNTZrMVZlcmlmaWNhdGlvbktleTIwMTkiLCJqd2siOnsia3R5IjoiRUMiL" +
            "CJjcnYiOiJzZWNwMjU2azEiLCJ4IjoiaEEzRWFaanZnR3BpMUFuV21TRXpQaGhlSTU1TXpoZHdEVXRYY0FTMzZGayIsInkiOiJFN" +
            "DRwSnA3X21kNlo2LXlEbmdPSzlMWWFXX0xUQzhCWGdTVk1RQ1plTnJBIn0sInVzYWdlIjpbIm9wcyIsImF1dGgiLCJnZW5lcmFsI" +
            "l19XX19XX0\"}],\"service\":[{\"id\":\"#anyServiceEndpointId\",\"type\":\"LinkedDomains\",\"serviceEndpoint\":\"htt" +
            "p://any.endpoint\"}],\"verificationMethod\":[{\"id\":\"#Va8_sign_ION_1\",\"controller\":\"\",\"type\":\"EcdsaSecp2" +
            "56k1VerificationKey2019\",\"publicKeyJwk\":{\"kty\":\"EC\",\"crv\":\"secp256k1\",\"x\":\"hA3EaZjvgGpi1AnWmSEzPhheI" +
            "55MzhdwDUtXcAS36Fk\",\"y\":\"E44pJp7_md6Z6-yDngOK9LYaW_LTC8BXgSVMQCZeNrA\"}}],\"authentication\":[\"#Va8_sig" +
            "n_ION_1\"]},\"methodMetadata\":{\"operationPublicKeys\":[{\"id\":\"#Va8_sign_ION_1\",\"controller\":\"\",\"type\":\"" +
            "EcdsaSecp256k1VerificationKey2019\",\"publicKeyJwk\":{\"kty\":\"EC\",\"crv\":\"secp256k1\",\"x\":\"hA3EaZjvgGpi1An" +
            "WmSEzPhheI55MzhdwDUtXcAS36Fk\",\"y\":\"E44pJp7_md6Z6-yDngOK9LYaW_LTC8BXgSVMQCZeNrA\"}}],\"recoveryKey\":{\"k" +
            "ty\":\"EC\",\"crv\":\"secp256k1\",\"x\":\"MDa8v7M4-DN-Vy6ZYYlPYnPtwtTMjvE9H3-wXX2VJKc\",\"y\":\"BCpmR6pekJvBqEbpH4" +
            "LpnuLwSJLGdrNPr3AhWIGmbX4\"}},\"resolverMetadata\":{\"driverId\":\"did:ion\",\"driver\":\"HttpDriver\",\"retriev" +
            "ed\":\"2020-05-31T09:20:41.162Z\",\"duration\":\"118.4098ms\"}}"
        coEvery { linkedDomainsService.resolveIdentifierDocument(suppliedDidWithSingleServiceEndpoint) } returns KotlinResult.success(
            defaultTestSerializer.decodeFromString(
                IdentifierResponse.serializer(),
                rpDidDoc
            ).didDocument
        )

        // Act and Assert
        runBlocking {
            val actualLinkedDomainsResultResponse =
                linkedDomainsService.fetchDocumentAndVerifyLinkedDomains(suppliedDidWithSingleServiceEndpoint)
            assertThat(actualLinkedDomainsResultResponse).isInstanceOf(KotlinResult.success(LinkedDomainVerified)::class.java)
            assertThat((actualLinkedDomainsResultResponse.getOrNull() as? LinkedDomainVerified)?.domainUrl).isEqualTo("discover.did.microsoft.com")
        }

        coVerify(exactly = 1) { mockRootOfTrustResolver.resolve(any()) }
    }

    @Test
    fun `test injecting root of trust resolver which throws and executes fallback option`() {
        // Arrange
        val mockRootOfTrustResolver = spyk(MockRootOfTrustResolver(true))
        val suppliedDidWithSingleServiceEndpoint =
            "did:ion:EiA8HR28m5KUig9elPRXkmKvvBGXcOoxpUrCscTdGJcIXQ?-ion-initial-state=eyJkZWx0YV9oYXNoIjoiRWlDVD" +
            "V5MG5nNTJCNzVjYWFqWU9qVjBRMmpxSng0NDZSajhRTjFpaHdteUpJZyIsInJlY292ZXJ5X2NvbW1pdG1lbnQiOiJFaURsOER4eX" +
            "ZLa3lvRmJsUWp0OXllU2J3TXkwR083MFM1R2FUU1F0UlF0aFJRIn0.eyJ1cGRhdGVfY29tbWl0bWVudCI6IkVpRGFXS2sycjJiSW" +
            "JsRWFpRUhtRU5Kc2h6czhtY1hJd2hTV0Z1YmtWQlJ3WWciLCJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbn" +
            "QiOnsicHVibGljX2tleXMiOlt7ImlkIjoic2lnX2VkMmM1ZWRmIiwidHlwZSI6IkVjZHNhU2VjcDI1NmsxVmVyaWZpY2F0aW9uS2" +
            "V5MjAxOSIsImp3ayI6eyJrdHkiOiJFQyIsImNydiI6InNlY3AyNTZrMSIsIngiOiJoTzhYaXhtWUxNOVVVMmFWOW9kc2VsSDNobD" +
            "JtbVFPLS1GTzNKa2JrekVrIiwieSI6InBDWEpxbXpUbzVQQkdRTERibnRtdUFaSElZQnFZOG1DZVdkaWhpb0tGUmMifSwicHVycG" +
            "9zZSI6WyJhdXRoIiwiZ2VuZXJhbCJdfV19fV19"
        val linkedDomainsService: LinkedDomainsService =
            spyk(
                LinkedDomainsService(
                    mockk(relaxed = true),
                    mockedResolver,
                    mockedJwtDomainLinkageCredentialValidator,
                    mockRootOfTrustResolver
                )
            )
        val didMetadata = DidMetadata()
        didMetadata.id = suppliedDidWithSingleServiceEndpoint
        val expectedResponseString =
            "{\"@context\":\"https://www.w3.org/ns/did-resolution/v1\",\"didDocument\":{\"id\":\"did:ion:EiA8HR28m5KUig9el" +
            "PRXkmKvvBGXcOoxpUrCscTdGJcIXQ?-ion-initial-state=eyJkZWx0YV9oYXNoIjoiRWlDVDV5MG5nNTJCNzVjYWFqWU9qVjB" +
            "RMmpxSng0NDZSajhRTjFpaHdteUpJZyIsInJlY292ZXJ5X2NvbW1pdG1lbnQiOiJFaURsOER4eXZLa3lvRmJsUWp0OXllU2J3TXk" +
            "wR083MFM1R2FUU1F0UlF0aFJRIn0.eyJ1cGRhdGVfY29tbWl0bWVudCI6IkVpRGFXS2sycjJiSWJsRWFpRUhtRU5Kc2h6czhtY1h" +
            "Jd2hTV0Z1YmtWQlJ3WWciLCJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljX2tleXMiOlt" +
            "7ImlkIjoic2lnX2VkMmM1ZWRmIiwidHlwZSI6IkVjZHNhU2VjcDI1NmsxVmVyaWZpY2F0aW9uS2V5MjAxOSIsImp3ayI6eyJrdHk" +
            "iOiJFQyIsImNydiI6InNlY3AyNTZrMSIsIngiOiJoTzhYaXhtWUxNOVVVMmFWOW9kc2VsSDNobDJtbVFPLS1GTzNKa2JrekVrIiw" +
            "ieSI6InBDWEpxbXpUbzVQQkdRTERibnRtdUFaSElZQnFZOG1DZVdkaWhpb0tGUmMifSwicHVycG9zZSI6WyJhdXRoIiwiZ2VuZXJ" +
            "hbCJdfV19fV19\",\"@context\":[\"https://www.w3.org/ns/did/v1\",{\"@base\":\"did:ion:EiA8HR28m5KUig9elPRXkmKv" +
            "vBGXcOoxpUrCscTdGJcIXQ?-ion-initial-state=eyJkZWx0YV9oYXNoIjoiRWlDVDV5MG5nNTJCNzVjYWFqWU9qVjBRMmpxSn" +
            "g0NDZSajhRTjFpaHdteUpJZyIsInJlY292ZXJ5X2NvbW1pdG1lbnQiOiJFaURsOER4eXZLa3lvRmJsUWp0OXllU2J3TXkwR083MF" +
            "M1R2FUU1F0UlF0aFJRIn0.eyJ1cGRhdGVfY29tbWl0bWVudCI6IkVpRGFXS2sycjJiSWJsRWFpRUhtRU5Kc2h6czhtY1hJd2hTV0" +
            "Z1YmtWQlJ3WWciLCJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljX2tleXMiOlt7ImlkIj" +
            "oic2lnX2VkMmM1ZWRmIiwidHlwZSI6IkVjZHNhU2VjcDI1NmsxVmVyaWZpY2F0aW9uS2V5MjAxOSIsImp3ayI6eyJrdHkiOiJFQy" +
            "IsImNydiI6InNlY3AyNTZrMSIsIngiOiJoTzhYaXhtWUxNOVVVMmFWOW9kc2VsSDNobDJtbVFPLS1GTzNKa2JrekVrIiwieSI6In" +
            "BDWEpxbXpUbzVQQkdRTERibnRtdUFaSElZQnFZOG1DZVdkaWhpb0tGUmMifSwicHVycG9zZSI6WyJhdXRoIiwiZ2VuZXJhbCJdfV" +
            "19fV19\"}],\"service\":[{\"id\":\"#linkeddomains\",\"type\":\"LinkedDomains\",\"serviceEndpoint\":\"https://issuer" +
            "testng.com\"}],\"verificationMethod\":[{\"id\":\"#sig_ed2c5edf\",\"controller\":\"\",\"type\":\"EcdsaSecp256k1Veri" +
            "ficationKey2019\",\"publicKeyJwk\":{\"kty\":\"EC\",\"crv\":\"secp256k1\",\"x\":\"hO8XixmYLM9UU2aV9odselH3hl2mmQO--" +
            "FO3JkbkzEk\",\"y\":\"pCXJqmzTo5PBGQLDbntmuAZHIYBqY8mCeWdihioKFRc\"}}],\"authentication\":[\"#sig_ed2c5edf\"]}" +
            ",\"methodMetadata\":{\"published\":false,\"recoveryCommitment\":\"EiDl8DxyvKkyoFblQjt9yeSbwMy0GO70S5GaTSQtR" +
            "QthRQ\",\"updateCommitment\":\"EiDaWKk2r2bIblEaiEHmENJshzs8mcXIwhSWFubkVBRwYg\"},\"resolverMetadata\":{\"dri" +
            "verId\":\"did:ion\",\"driver\":\"HttpDriver\",\"retrieved\":\"2020-10-25T06:39:09.486Z\",\"duration\":\"117.3589ms" +
            "\"}}"
        val expectedResponse = defaultTestSerializer.decodeFromString(IdentifierResponse.serializer(), expectedResponseString)
        val expectedWellKnownConfigDocumentResponse =
            "{\"@context\":\"https://identity.foundation/.well-known/contexts/did-configuration-v0.0.jsonld\",\"linked" +
            "_dids\":[\"eyJhbGciOiJFUzI1NksiLCJraWQiOiJkaWQ6aW9uOkVpQThIUjI4bTVLVWlnOWVsUFJYa21LdnZCR1hjT294cFVyQ3N" +
            "jVGRHSmNJWFE_LWlvbi1pbml0aWFsLXN0YXRlPWV5SmtaV3gwWVY5b1lYTm9Jam9pUldsRFZEVjVNRzVuTlRKQ056VmpZV0ZxV1U" +
            "5cVZqQlJNbXB4U25nME5EWlNhamhSVGpGcGFIZHRlVXBKWnlJc0luSmxZMjkyWlhKNVgyTnZiVzFwZEcxbGJuUWlPaUpGYVVSc09" +
            "FUjRlWFpMYTNsdlJtSnNVV3AwT1hsbFUySjNUWGt3UjA4M01GTTFSMkZVVTFGMFVsRjBhRkpSSW4wLmV5SjFjR1JoZEdWZlkyOXR" +
            "iV2wwYldWdWRDSTZJa1ZwUkdGWFMyc3ljakppU1dKc1JXRnBSVWh0UlU1S2MyaDZjemh0WTFoSmQyaFRWMFoxWW10V1FsSjNXV2N" +
            "pTENKd1lYUmphR1Z6SWpwYmV5SmhZM1JwYjI0aU9pSnlaWEJzWVdObElpd2laRzlqZFcxbGJuUWlPbnNpY0hWaWJHbGpYMnRsZVh" +
            "NaU9sdDdJbWxrSWpvaWMybG5YMlZrTW1NMVpXUm1JaXdpZEhsd1pTSTZJa1ZqWkhOaFUyVmpjREkxTm1zeFZtVnlhV1pwWTJGMGF" +
            "XOXVTMlY1TWpBeE9TSXNJbXAzYXlJNmV5SnJkSGtpT2lKRlF5SXNJbU55ZGlJNkluTmxZM0F5TlRack1TSXNJbmdpT2lKb1R6aFl" +
            "hWGh0V1V4Tk9WVlZNbUZXT1c5a2MyVnNTRE5vYkRKdGJWRlBMUzFHVHpOS2EySnJla1ZySWl3aWVTSTZJbkJEV0VweGJYcFVielZ" +
            "RUWtkUlRFUmliblJ0ZFVGYVNFbFpRbkZaT0cxRFpWZGthV2hwYjB0R1VtTWlmU3dpY0hWeWNHOXpaU0k2V3lKaGRYUm9JaXdpWjJ" +
            "WdVpYSmhiQ0pkZlYxOWZWMTkjc2lnX2VkMmM1ZWRmIn0.eyJzdWIiOiJkaWQ6aW9uOkVpQThIUjI4bTVLVWlnOWVsUFJYa21LdnZ" +
            "CR1hjT294cFVyQ3NjVGRHSmNJWFE_LWlvbi1pbml0aWFsLXN0YXRlPWV5SmtaV3gwWVY5b1lYTm9Jam9pUldsRFZEVjVNRzVuTlR" +
            "KQ056VmpZV0ZxV1U5cVZqQlJNbXB4U25nME5EWlNhamhSVGpGcGFIZHRlVXBKWnlJc0luSmxZMjkyWlhKNVgyTnZiVzFwZEcxbGJ" +
            "uUWlPaUpGYVVSc09FUjRlWFpMYTNsdlJtSnNVV3AwT1hsbFUySjNUWGt3UjA4M01GTTFSMkZVVTFGMFVsRjBhRkpSSW4wLmV5SjF" +
            "jR1JoZEdWZlkyOXRiV2wwYldWdWRDSTZJa1ZwUkdGWFMyc3ljakppU1dKc1JXRnBSVWh0UlU1S2MyaDZjemh0WTFoSmQyaFRWMFo" +
            "xWW10V1FsSjNXV2NpTENKd1lYUmphR1Z6SWpwYmV5SmhZM1JwYjI0aU9pSnlaWEJzWVdObElpd2laRzlqZFcxbGJuUWlPbnNpY0h" +
            "WaWJHbGpYMnRsZVhNaU9sdDdJbWxrSWpvaWMybG5YMlZrTW1NMVpXUm1JaXdpZEhsd1pTSTZJa1ZqWkhOaFUyVmpjREkxTm1zeFZ" +
            "tVnlhV1pwWTJGMGFXOXVTMlY1TWpBeE9TSXNJbXAzYXlJNmV5SnJkSGtpT2lKRlF5SXNJbU55ZGlJNkluTmxZM0F5TlRack1TSXN" +
            "JbmdpT2lKb1R6aFlhWGh0V1V4Tk9WVlZNbUZXT1c5a2MyVnNTRE5vYkRKdGJWRlBMUzFHVHpOS2EySnJla1ZySWl3aWVTSTZJbkJ" +
            "EV0VweGJYcFVielZRUWtkUlRFUmliblJ0ZFVGYVNFbFpRbkZaT0cxRFpWZGthV2hwYjB0R1VtTWlmU3dpY0hWeWNHOXpaU0k2V3l" +
            "KaGRYUm9JaXdpWjJWdVpYSmhiQ0pkZlYxOWZWMTkiLCJpc3MiOiJkaWQ6aW9uOkVpQThIUjI4bTVLVWlnOWVsUFJYa21LdnZCR1h" +
            "jT294cFVyQ3NjVGRHSmNJWFE_LWlvbi1pbml0aWFsLXN0YXRlPWV5SmtaV3gwWVY5b1lYTm9Jam9pUldsRFZEVjVNRzVuTlRKQ05" +
            "6VmpZV0ZxV1U5cVZqQlJNbXB4U25nME5EWlNhamhSVGpGcGFIZHRlVXBKWnlJc0luSmxZMjkyWlhKNVgyTnZiVzFwZEcxbGJuUWl" +
            "PaUpGYVVSc09FUjRlWFpMYTNsdlJtSnNVV3AwT1hsbFUySjNUWGt3UjA4M01GTTFSMkZVVTFGMFVsRjBhRkpSSW4wLmV5SjFjR1J" +
            "oZEdWZlkyOXRiV2wwYldWdWRDSTZJa1ZwUkdGWFMyc3ljakppU1dKc1JXRnBSVWh0UlU1S2MyaDZjemh0WTFoSmQyaFRWMFoxWW1" +
            "0V1FsSjNXV2NpTENKd1lYUmphR1Z6SWpwYmV5SmhZM1JwYjI0aU9pSnlaWEJzWVdObElpd2laRzlqZFcxbGJuUWlPbnNpY0hWaWJ" +
            "HbGpYMnRsZVhNaU9sdDdJbWxrSWpvaWMybG5YMlZrTW1NMVpXUm1JaXdpZEhsd1pTSTZJa1ZqWkhOaFUyVmpjREkxTm1zeFZtVnl" +
            "hV1pwWTJGMGFXOXVTMlY1TWpBeE9TSXNJbXAzYXlJNmV5SnJkSGtpT2lKRlF5SXNJbU55ZGlJNkluTmxZM0F5TlRack1TSXNJbmd" +
            "pT2lKb1R6aFlhWGh0V1V4Tk9WVlZNbUZXT1c5a2MyVnNTRE5vYkRKdGJWRlBMUzFHVHpOS2EySnJla1ZySWl3aWVTSTZJbkJEV0V" +
            "weGJYcFVielZRUWtkUlRFUmliblJ0ZFVGYVNFbFpRbkZaT0cxRFpWZGthV2hwYjB0R1VtTWlmU3dpY0hWeWNHOXpaU0k2V3lKaGR" +
            "YUm9JaXdpWjJWdVpYSmhiQ0pkZlYxOWZWMTkiLCJuYmYiOjE2MDM0MTU2NjQsImV4cCI6MjM5MjMzNDA2NCwidmMiOnsiQGNvbnR" +
            "leHQiOlsiaHR0cHM6Ly93d3cudzMub3JnLzIwMTgvY3JlZGVudGlhbHMvdjEiLCJodHRwczovL2lkZW50aXR5LmZvdW5kYXRpb24" +
            "vLndlbGwta25vd24vY29udGV4dHMvZGlkLWNvbmZpZ3VyYXRpb24tdjAuMC5qc29ubGQiXSwiaXNzdWVyIjoiZGlkOmlvbjpFaUE" +
            "4SFIyOG01S1VpZzllbFBSWGttS3Z2QkdYY09veHBVckNzY1RkR0pjSVhRPy1pb24taW5pdGlhbC1zdGF0ZT1leUprWld4MFlWOW9" +
            "ZWE5vSWpvaVJXbERWRFY1TUc1bk5USkNOelZqWVdGcVdVOXFWakJSTW1weFNuZzBORFpTYWpoUlRqRnBhSGR0ZVVwSlp5SXNJbkp" +
            "sWTI5MlpYSjVYMk52YlcxcGRHMWxiblFpT2lKRmFVUnNPRVI0ZVhaTGEzbHZSbUpzVVdwME9YbGxVMkozVFhrd1IwODNNRk0xUjJ" +
            "GVVUxRjBVbEYwYUZKUkluMC5leUoxY0dSaGRHVmZZMjl0YldsMGJXVnVkQ0k2SWtWcFJHRlhTMnN5Y2pKaVNXSnNSV0ZwUlVodFJ" +
            "VNUtjMmg2Y3podFkxaEpkMmhUVjBaMVltdFdRbEozV1djaUxDSndZWFJqYUdWeklqcGJleUpoWTNScGIyNGlPaUp5WlhCc1lXTmx" +
            "JaXdpWkc5amRXMWxiblFpT25zaWNIVmliR2xqWDJ0bGVYTWlPbHQ3SW1sa0lqb2ljMmxuWDJWa01tTTFaV1JtSWl3aWRIbHdaU0k" +
            "2SWtWalpITmhVMlZqY0RJMU5tc3hWbVZ5YVdacFkyRjBhVzl1UzJWNU1qQXhPU0lzSW1wM2F5STZleUpyZEhraU9pSkZReUlzSW1" +
            "OeWRpSTZJbk5sWTNBeU5UWnJNU0lzSW5naU9pSm9UemhZYVhodFdVeE5PVlZWTW1GV09XOWtjMlZzU0ROb2JESnRiVkZQTFMxR1R" +
            "6TkthMkpyZWtWcklpd2llU0k2SW5CRFdFcHhiWHBVYnpWUVFrZFJURVJpYm5SdGRVRmFTRWxaUW5GWk9HMURaVmRrYVdocGIwdEd" +
            "VbU1pZlN3aWNIVnljRzl6WlNJNld5SmhkWFJvSWl3aVoyVnVaWEpoYkNKZGZWMTlmVjE5IiwiaXNzdWFuY2VEYXRlIjoiMjAyMC0" +
            "xMC0yM1QwMToxNDoyNC43NzRaIiwiZXhwaXJhdGlvbkRhdGUiOiIyMDQ1LTEwLTIzVDAxOjE0OjI0Ljc3NFoiLCJ0eXBlIjpbIlZ" +
            "lcmlmaWFibGVDcmVkZW50aWFsIiwiRG9tYWluTGlua2FnZUNyZWRlbnRpYWwiXSwiY3JlZGVudGlhbFN1YmplY3QiOnsiaWQiOiJ" +
            "kaWQ6aW9uOkVpQThIUjI4bTVLVWlnOWVsUFJYa21LdnZCR1hjT294cFVyQ3NjVGRHSmNJWFE_LWlvbi1pbml0aWFsLXN0YXRlPWV" +
            "5SmtaV3gwWVY5b1lYTm9Jam9pUldsRFZEVjVNRzVuTlRKQ056VmpZV0ZxV1U5cVZqQlJNbXB4U25nME5EWlNhamhSVGpGcGFIZHR" +
            "lVXBKWnlJc0luSmxZMjkyWlhKNVgyTnZiVzFwZEcxbGJuUWlPaUpGYVVSc09FUjRlWFpMYTNsdlJtSnNVV3AwT1hsbFUySjNUWGt" +
            "3UjA4M01GTTFSMkZVVTFGMFVsRjBhRkpSSW4wLmV5SjFjR1JoZEdWZlkyOXRiV2wwYldWdWRDSTZJa1ZwUkdGWFMyc3ljakppU1d" +
            "Kc1JXRnBSVWh0UlU1S2MyaDZjemh0WTFoSmQyaFRWMFoxWW10V1FsSjNXV2NpTENKd1lYUmphR1Z6SWpwYmV5SmhZM1JwYjI0aU9" +
            "pSnlaWEJzWVdObElpd2laRzlqZFcxbGJuUWlPbnNpY0hWaWJHbGpYMnRsZVhNaU9sdDdJbWxrSWpvaWMybG5YMlZrTW1NMVpXUm1" +
            "JaXdpZEhsd1pTSTZJa1ZqWkhOaFUyVmpjREkxTm1zeFZtVnlhV1pwWTJGMGFXOXVTMlY1TWpBeE9TSXNJbXAzYXlJNmV5SnJkSGt" +
            "pT2lKRlF5SXNJbU55ZGlJNkluTmxZM0F5TlRack1TSXNJbmdpT2lKb1R6aFlhWGh0V1V4Tk9WVlZNbUZXT1c5a2MyVnNTRE5vYkR" +
            "KdGJWRlBMUzFHVHpOS2EySnJla1ZySWl3aWVTSTZJbkJEV0VweGJYcFVielZRUWtkUlRFUmliblJ0ZFVGYVNFbFpRbkZaT0cxRFp" +
            "WZGthV2hwYjB0R1VtTWlmU3dpY0hWeWNHOXpaU0k2V3lKaGRYUm9JaXdpWjJWdVpYSmhiQ0pkZlYxOWZWMTkiLCJvcmlnaW4iOiJ" +
            "odHRwczovL2lzc3VlcnRlc3RuZy5jb20ifX19.luP4vIE30u_kj5CINDhbjZcsHDbEEXS5hMFnzd7qVKs2OtseDJlYcgLOgbCqI1" +
            "7v3edXx9OVkErQ-sSuIGvt8g\"]}"
        val expectedWellKnownConfigDocument =
            defaultTestSerializer.decodeFromString(LinkedDomainsResponse.serializer(), expectedWellKnownConfigDocumentResponse)
        val expectedDomainUrl = "https://discover.did.microsoft.com"
        coEvery { linkedDomainsService.resolveIdentifierDocument(suppliedDidWithSingleServiceEndpoint) } returns KotlinResult.success(
            expectedResponse.didDocument
        )
        coEvery { linkedDomainsService["getWellKnownConfigDocument"](expectedDomainUrl) } returns KotlinResult.success(
            expectedWellKnownConfigDocument
        )
        coEvery { mockedJwtValidator.verifySignature(any()) } returns true
        coEvery { mockedJwtValidator.validateDidInHeaderAndPayload(any(), any()) } returns true

        // Act and Assert
        runBlocking {
            val actualLinkedDomainsResultResponse =
                linkedDomainsService.fetchDocumentAndVerifyLinkedDomains(suppliedDidWithSingleServiceEndpoint)
            assertThat(actualLinkedDomainsResultResponse).isInstanceOf(KotlinResult.success(LinkedDomainVerified)::class.java)
            assertThat((actualLinkedDomainsResultResponse.getOrNull() as? LinkedDomainVerified)?.domainUrl)
                .isEqualTo("discover.did.microsoft.com")
        }

        coVerify(exactly = 1) { mockRootOfTrustResolver.resolve(any()) }
    }
}
