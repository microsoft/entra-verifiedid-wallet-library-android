// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.did.sdk.credential.service.validators

import com.microsoft.walletlibrary.did.sdk.credential.service.models.serviceResponses.LinkedDomainsResponse
import com.microsoft.walletlibrary.did.sdk.di.defaultTestSerializer
import com.microsoft.walletlibrary.did.sdk.util.log.SdkLog
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.junit.Test

class JwtDomainLinkageCredentialValidatorTest {

    private val jwtDomainLinkageCredentialValidator: JwtDomainLinkageCredentialValidator
    private val mockedJwtValidator: JwtValidator = mockk()
    private val docJwt =
        """{"@context":"https://identity.foundation/.well-known/contexts/did-configuration-v0.0.jsonld","linked_dids":["eyJhbGciOiJFUzI1NksiLCJraWQiOiJkaWQ6aW9uOkVpQThIUjI4bTVLVWlnOWVsUFJYa21LdnZCR1hjT294cFVyQ3NjVGRHSmNJWFE_LWlvbi1pbml0aWFsLXN0YXRlPWV5SmtaV3gwWVY5b1lYTm9Jam9pUldsRFZEVjVNRzVuTlRKQ056VmpZV0ZxV1U5cVZqQlJNbXB4U25nME5EWlNhamhSVGpGcGFIZHRlVXBKWnlJc0luSmxZMjkyWlhKNVgyTnZiVzFwZEcxbGJuUWlPaUpGYVVSc09FUjRlWFpMYTNsdlJtSnNVV3AwT1hsbFUySjNUWGt3UjA4M01GTTFSMkZVVTFGMFVsRjBhRkpSSW4wLmV5SjFjR1JoZEdWZlkyOXRiV2wwYldWdWRDSTZJa1ZwUkdGWFMyc3ljakppU1dKc1JXRnBSVWh0UlU1S2MyaDZjemh0WTFoSmQyaFRWMFoxWW10V1FsSjNXV2NpTENKd1lYUmphR1Z6SWpwYmV5SmhZM1JwYjI0aU9pSnlaWEJzWVdObElpd2laRzlqZFcxbGJuUWlPbnNpY0hWaWJHbGpYMnRsZVhNaU9sdDdJbWxrSWpvaWMybG5YMlZrTW1NMVpXUm1JaXdpZEhsd1pTSTZJa1ZqWkhOaFUyVmpjREkxTm1zeFZtVnlhV1pwWTJGMGFXOXVTMlY1TWpBeE9TSXNJbXAzYXlJNmV5SnJkSGtpT2lKRlF5SXNJbU55ZGlJNkluTmxZM0F5TlRack1TSXNJbmdpT2lKb1R6aFlhWGh0V1V4Tk9WVlZNbUZXT1c5a2MyVnNTRE5vYkRKdGJWRlBMUzFHVHpOS2EySnJla1ZySWl3aWVTSTZJbkJEV0VweGJYcFVielZRUWtkUlRFUmliblJ0ZFVGYVNFbFpRbkZaT0cxRFpWZGthV2hwYjB0R1VtTWlmU3dpY0hWeWNHOXpaU0k2V3lKaGRYUm9JaXdpWjJWdVpYSmhiQ0pkZlYxOWZWMTkjc2lnX2VkMmM1ZWRmIn0.eyJzdWIiOiJkaWQ6aW9uOkVpQThIUjI4bTVLVWlnOWVsUFJYa21LdnZCR1hjT294cFVyQ3NjVGRHSmNJWFE_LWlvbi1pbml0aWFsLXN0YXRlPWV5SmtaV3gwWVY5b1lYTm9Jam9pUldsRFZEVjVNRzVuTlRKQ056VmpZV0ZxV1U5cVZqQlJNbXB4U25nME5EWlNhamhSVGpGcGFIZHRlVXBKWnlJc0luSmxZMjkyWlhKNVgyTnZiVzFwZEcxbGJuUWlPaUpGYVVSc09FUjRlWFpMYTNsdlJtSnNVV3AwT1hsbFUySjNUWGt3UjA4M01GTTFSMkZVVTFGMFVsRjBhRkpSSW4wLmV5SjFjR1JoZEdWZlkyOXRiV2wwYldWdWRDSTZJa1ZwUkdGWFMyc3ljakppU1dKc1JXRnBSVWh0UlU1S2MyaDZjemh0WTFoSmQyaFRWMFoxWW10V1FsSjNXV2NpTENKd1lYUmphR1Z6SWpwYmV5SmhZM1JwYjI0aU9pSnlaWEJzWVdObElpd2laRzlqZFcxbGJuUWlPbnNpY0hWaWJHbGpYMnRsZVhNaU9sdDdJbWxrSWpvaWMybG5YMlZrTW1NMVpXUm1JaXdpZEhsd1pTSTZJa1ZqWkhOaFUyVmpjREkxTm1zeFZtVnlhV1pwWTJGMGFXOXVTMlY1TWpBeE9TSXNJbXAzYXlJNmV5SnJkSGtpT2lKRlF5SXNJbU55ZGlJNkluTmxZM0F5TlRack1TSXNJbmdpT2lKb1R6aFlhWGh0V1V4Tk9WVlZNbUZXT1c5a2MyVnNTRE5vYkRKdGJWRlBMUzFHVHpOS2EySnJla1ZySWl3aWVTSTZJbkJEV0VweGJYcFVielZRUWtkUlRFUmliblJ0ZFVGYVNFbFpRbkZaT0cxRFpWZGthV2hwYjB0R1VtTWlmU3dpY0hWeWNHOXpaU0k2V3lKaGRYUm9JaXdpWjJWdVpYSmhiQ0pkZlYxOWZWMTkiLCJpc3MiOiJkaWQ6aW9uOkVpQThIUjI4bTVLVWlnOWVsUFJYa21LdnZCR1hjT294cFVyQ3NjVGRHSmNJWFE_LWlvbi1pbml0aWFsLXN0YXRlPWV5SmtaV3gwWVY5b1lYTm9Jam9pUldsRFZEVjVNRzVuTlRKQ056VmpZV0ZxV1U5cVZqQlJNbXB4U25nME5EWlNhamhSVGpGcGFIZHRlVXBKWnlJc0luSmxZMjkyWlhKNVgyTnZiVzFwZEcxbGJuUWlPaUpGYVVSc09FUjRlWFpMYTNsdlJtSnNVV3AwT1hsbFUySjNUWGt3UjA4M01GTTFSMkZVVTFGMFVsRjBhRkpSSW4wLmV5SjFjR1JoZEdWZlkyOXRiV2wwYldWdWRDSTZJa1ZwUkdGWFMyc3ljakppU1dKc1JXRnBSVWh0UlU1S2MyaDZjemh0WTFoSmQyaFRWMFoxWW10V1FsSjNXV2NpTENKd1lYUmphR1Z6SWpwYmV5SmhZM1JwYjI0aU9pSnlaWEJzWVdObElpd2laRzlqZFcxbGJuUWlPbnNpY0hWaWJHbGpYMnRsZVhNaU9sdDdJbWxrSWpvaWMybG5YMlZrTW1NMVpXUm1JaXdpZEhsd1pTSTZJa1ZqWkhOaFUyVmpjREkxTm1zeFZtVnlhV1pwWTJGMGFXOXVTMlY1TWpBeE9TSXNJbXAzYXlJNmV5SnJkSGtpT2lKRlF5SXNJbU55ZGlJNkluTmxZM0F5TlRack1TSXNJbmdpT2lKb1R6aFlhWGh0V1V4Tk9WVlZNbUZXT1c5a2MyVnNTRE5vYkRKdGJWRlBMUzFHVHpOS2EySnJla1ZySWl3aWVTSTZJbkJEV0VweGJYcFVielZRUWtkUlRFUmliblJ0ZFVGYVNFbFpRbkZaT0cxRFpWZGthV2hwYjB0R1VtTWlmU3dpY0hWeWNHOXpaU0k2V3lKaGRYUm9JaXdpWjJWdVpYSmhiQ0pkZlYxOWZWMTkiLCJuYmYiOjE2MDM0MTU2NjQsImV4cCI6MjM5MjMzNDA2NCwidmMiOnsiQGNvbnRleHQiOlsiaHR0cHM6Ly93d3cudzMub3JnLzIwMTgvY3JlZGVudGlhbHMvdjEiLCJodHRwczovL2lkZW50aXR5LmZvdW5kYXRpb24vLndlbGwta25vd24vY29udGV4dHMvZGlkLWNvbmZpZ3VyYXRpb24tdjAuMC5qc29ubGQiXSwiaXNzdWVyIjoiZGlkOmlvbjpFaUE4SFIyOG01S1VpZzllbFBSWGttS3Z2QkdYY09veHBVckNzY1RkR0pjSVhRPy1pb24taW5pdGlhbC1zdGF0ZT1leUprWld4MFlWOW9ZWE5vSWpvaVJXbERWRFY1TUc1bk5USkNOelZqWVdGcVdVOXFWakJSTW1weFNuZzBORFpTYWpoUlRqRnBhSGR0ZVVwSlp5SXNJbkpsWTI5MlpYSjVYMk52YlcxcGRHMWxiblFpT2lKRmFVUnNPRVI0ZVhaTGEzbHZSbUpzVVdwME9YbGxVMkozVFhrd1IwODNNRk0xUjJGVVUxRjBVbEYwYUZKUkluMC5leUoxY0dSaGRHVmZZMjl0YldsMGJXVnVkQ0k2SWtWcFJHRlhTMnN5Y2pKaVNXSnNSV0ZwUlVodFJVNUtjMmg2Y3podFkxaEpkMmhUVjBaMVltdFdRbEozV1djaUxDSndZWFJqYUdWeklqcGJleUpoWTNScGIyNGlPaUp5WlhCc1lXTmxJaXdpWkc5amRXMWxiblFpT25zaWNIVmliR2xqWDJ0bGVYTWlPbHQ3SW1sa0lqb2ljMmxuWDJWa01tTTFaV1JtSWl3aWRIbHdaU0k2SWtWalpITmhVMlZqY0RJMU5tc3hWbVZ5YVdacFkyRjBhVzl1UzJWNU1qQXhPU0lzSW1wM2F5STZleUpyZEhraU9pSkZReUlzSW1OeWRpSTZJbk5sWTNBeU5UWnJNU0lzSW5naU9pSm9UemhZYVhodFdVeE5PVlZWTW1GV09XOWtjMlZzU0ROb2JESnRiVkZQTFMxR1R6TkthMkpyZWtWcklpd2llU0k2SW5CRFdFcHhiWHBVYnpWUVFrZFJURVJpYm5SdGRVRmFTRWxaUW5GWk9HMURaVmRrYVdocGIwdEdVbU1pZlN3aWNIVnljRzl6WlNJNld5SmhkWFJvSWl3aVoyVnVaWEpoYkNKZGZWMTlmVjE5IiwiaXNzdWFuY2VEYXRlIjoiMjAyMC0xMC0yM1QwMToxNDoyNC43NzRaIiwiZXhwaXJhdGlvbkRhdGUiOiIyMDQ1LTEwLTIzVDAxOjE0OjI0Ljc3NFoiLCJ0eXBlIjpbIlZlcmlmaWFibGVDcmVkZW50aWFsIiwiRG9tYWluTGlua2FnZUNyZWRlbnRpYWwiXSwiY3JlZGVudGlhbFN1YmplY3QiOnsiaWQiOiJkaWQ6aW9uOkVpQThIUjI4bTVLVWlnOWVsUFJYa21LdnZCR1hjT294cFVyQ3NjVGRHSmNJWFE_LWlvbi1pbml0aWFsLXN0YXRlPWV5SmtaV3gwWVY5b1lYTm9Jam9pUldsRFZEVjVNRzVuTlRKQ056VmpZV0ZxV1U5cVZqQlJNbXB4U25nME5EWlNhamhSVGpGcGFIZHRlVXBKWnlJc0luSmxZMjkyWlhKNVgyTnZiVzFwZEcxbGJuUWlPaUpGYVVSc09FUjRlWFpMYTNsdlJtSnNVV3AwT1hsbFUySjNUWGt3UjA4M01GTTFSMkZVVTFGMFVsRjBhRkpSSW4wLmV5SjFjR1JoZEdWZlkyOXRiV2wwYldWdWRDSTZJa1ZwUkdGWFMyc3ljakppU1dKc1JXRnBSVWh0UlU1S2MyaDZjemh0WTFoSmQyaFRWMFoxWW10V1FsSjNXV2NpTENKd1lYUmphR1Z6SWpwYmV5SmhZM1JwYjI0aU9pSnlaWEJzWVdObElpd2laRzlqZFcxbGJuUWlPbnNpY0hWaWJHbGpYMnRsZVhNaU9sdDdJbWxrSWpvaWMybG5YMlZrTW1NMVpXUm1JaXdpZEhsd1pTSTZJa1ZqWkhOaFUyVmpjREkxTm1zeFZtVnlhV1pwWTJGMGFXOXVTMlY1TWpBeE9TSXNJbXAzYXlJNmV5SnJkSGtpT2lKRlF5SXNJbU55ZGlJNkluTmxZM0F5TlRack1TSXNJbmdpT2lKb1R6aFlhWGh0V1V4Tk9WVlZNbUZXT1c5a2MyVnNTRE5vYkRKdGJWRlBMUzFHVHpOS2EySnJla1ZySWl3aWVTSTZJbkJEV0VweGJYcFVielZRUWtkUlRFUmliblJ0ZFVGYVNFbFpRbkZaT0cxRFpWZGthV2hwYjB0R1VtTWlmU3dpY0hWeWNHOXpaU0k2V3lKaGRYUm9JaXdpWjJWdVpYSmhiQ0pkZlYxOWZWMTkiLCJvcmlnaW4iOiJodHRwczovL2lzc3VlcnRlc3RuZy5jb20ifX19.luP4vIE30u_kj5CINDhbjZcsHDbEEXS5hMFnzd7qVKs2OtseDJlYcgLOgbCqI17v3edXx9OVkErQ-sSuIGvt8g"]}"""
    private val validRpDid =
        "did:ion:EiA8HR28m5KUig9elPRXkmKvvBGXcOoxpUrCscTdGJcIXQ?-ion-initial-state=eyJkZWx0YV9oYXNoIjoiRWlDVDV5MG5nNTJCNzVjYWFqWU9qVjBRMmpxSng0NDZSajhRTjFpaHdteUpJZyIsInJlY292ZXJ5X2NvbW1pdG1lbnQiOiJFaURsOER4eXZLa3lvRmJsUWp0OXllU2J3TXkwR083MFM1R2FUU1F0UlF0aFJRIn0.eyJ1cGRhdGVfY29tbWl0bWVudCI6IkVpRGFXS2sycjJiSWJsRWFpRUhtRU5Kc2h6czhtY1hJd2hTV0Z1YmtWQlJ3WWciLCJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljX2tleXMiOlt7ImlkIjoic2lnX2VkMmM1ZWRmIiwidHlwZSI6IkVjZHNhU2VjcDI1NmsxVmVyaWZpY2F0aW9uS2V5MjAxOSIsImp3ayI6eyJrdHkiOiJFQyIsImNydiI6InNlY3AyNTZrMSIsIngiOiJoTzhYaXhtWUxNOVVVMmFWOW9kc2VsSDNobDJtbVFPLS1GTzNKa2JrekVrIiwieSI6InBDWEpxbXpUbzVQQkdRTERibnRtdUFaSElZQnFZOG1DZVdkaWhpb0tGUmMifSwicHVycG9zZSI6WyJhdXRoIiwiZ2VuZXJhbCJdfV19fV19"
    private val invalidRpDid = "did:test:incorrect"
    private val validDomainUrl = "https://issuertestng.com"

    init {
        jwtDomainLinkageCredentialValidator = JwtDomainLinkageCredentialValidator(
            mockedJwtValidator,
            defaultTestSerializer,
            true
        )
    }

    @Test
    fun `validate well known config document with correct inputs`() {
        val response = defaultTestSerializer.decodeFromString(LinkedDomainsResponse.serializer(), docJwt)
        val domainLinkageCredentialJwt = response.linkedDids.first()
        coEvery { mockedJwtValidator.verifySignature(any()) } returns true
        coEvery { mockedJwtValidator.validateDidInHeaderAndPayload(any(), any()) } returns true
        runBlocking {
            val validated = jwtDomainLinkageCredentialValidator.validate(domainLinkageCredentialJwt, validRpDid, validDomainUrl)
            Assertions.assertThat(validated).isEqualTo(DomainLinkageCredentialValidationResult.VALID)
        }
    }

    @Test
    fun `equivalent relying party origins match signed credential origin`() {
        val response = defaultTestSerializer.decodeFromString(LinkedDomainsResponse.serializer(), docJwt)
        val domainLinkageCredentialJwt = response.linkedDids.first()
        coEvery { mockedJwtValidator.verifySignature(any()) } returns true
        coEvery { mockedJwtValidator.validateDidInHeaderAndPayload(any(), any()) } returns true
        val equivalentOrigins = listOf(
            "https://issuertestng.com/",
            "HTTPS://ISSUERTESTNG.COM",
            "https://issuertestng.com:443",
            "https://issuertestng.com."
        )

        runBlocking {
            equivalentOrigins.forEach { origin ->
                val result = jwtDomainLinkageCredentialValidator.validate(
                    domainLinkageCredentialJwt,
                    validRpDid,
                    origin
                )

                Assertions.assertThat(result)
                    .describedAs("origin %s", origin)
                    .isEqualTo(DomainLinkageCredentialValidationResult.VALID)
            }

            @Test
            fun `default legacy validation does not canonicalize equivalent origins`() {
                val response = defaultTestSerializer.decodeFromString(LinkedDomainsResponse.serializer(), docJwt)
                val domainLinkageCredentialJwt = response.linkedDids.first()
                coEvery { mockedJwtValidator.verifySignature(any()) } returns true
                coEvery { mockedJwtValidator.validateDidInHeaderAndPayload(any(), any()) } returns true
                val legacyValidator = JwtDomainLinkageCredentialValidator(
                    mockedJwtValidator,
                    defaultTestSerializer
                )

                runBlocking {
                    val result = legacyValidator.validate(
                        domainLinkageCredentialJwt,
                        validRpDid,
                        "$validDomainUrl/"
                    )

                    Assertions.assertThat(result)
                        .isEqualTo(DomainLinkageCredentialValidationResult.ORIGIN_MISMATCH)
                }
            }
        }
    }

    @Test
    fun `failing validation of well known config document with incorrect issuer DID`() {
        val response = defaultTestSerializer.decodeFromString(LinkedDomainsResponse.serializer(), docJwt)
        val domainLinkageCredentialJwt = response.linkedDids.first()
        coEvery { mockedJwtValidator.verifySignature(any()) } returns true
        coEvery { mockedJwtValidator.validateDidInHeaderAndPayload(any(), any()) } returns true
        runBlocking {
            val validated = jwtDomainLinkageCredentialValidator.validate(domainLinkageCredentialJwt, invalidRpDid, validDomainUrl)
            Assertions.assertThat(validated).isEqualTo(DomainLinkageCredentialValidationResult.DID_MISMATCH)
        }
    }

    @Test
    fun `failing validation of well known config document with incorrect domain url`() {
        val response = defaultTestSerializer.decodeFromString(LinkedDomainsResponse.serializer(), docJwt)
        val domainLinkageCredentialJwt = response.linkedDids.first()
        coEvery { mockedJwtValidator.verifySignature(any()) } returns true
        coEvery { mockedJwtValidator.validateDidInHeaderAndPayload(any(), any()) } returns true
        mockkObject(SdkLog)
        try {
            every { SdkLog.w(any(), any(), any()) } just runs

            runBlocking {
                val validated = jwtDomainLinkageCredentialValidator.validate(
                    domainLinkageCredentialJwt,
                    validRpDid,
                    "https://issuertestng.com/path?secret=not-logged"
                )
                Assertions.assertThat(validated).isEqualTo(DomainLinkageCredentialValidationResult.ORIGIN_MISMATCH)
            }

            verify(exactly = 1) {
                SdkLog.w(
                    match {
                        it.contains("Expected origin='https://issuertestng.com'") &&
                            it.contains("observed origin='https://issuertestng.com'") &&
                            it.contains("mismatch components='non_origin_components_or_format'") &&
                            !it.contains("not-logged")
                    },
                    null,
                    any()
                )
            }
        } finally {
            unmockkObject(SdkLog)
        }
    }

    @Test
    fun `failing validation of well known config document with invalid signature`() {
        val response = defaultTestSerializer.decodeFromString(LinkedDomainsResponse.serializer(), docJwt)
        val domainLinkageCredentialJwt = response.linkedDids.first()
        coEvery { mockedJwtValidator.verifySignature(any()) } returns false
        runBlocking {
            val validated = jwtDomainLinkageCredentialValidator.validate(domainLinkageCredentialJwt, validRpDid, validDomainUrl)
            Assertions.assertThat(validated).isEqualTo(DomainLinkageCredentialValidationResult.SIGNATURE_INVALID)
        }
    }

    @Test
    fun `failing validation of malformed credential returns claims invalid`() {
        runBlocking {
            val validated = jwtDomainLinkageCredentialValidator.validate(
                "not-a-jwt",
                validRpDid,
                validDomainUrl
            )

            Assertions.assertThat(validated)
                .isEqualTo(DomainLinkageCredentialValidationResult.CLAIMS_INVALID)
        }
    }

    @Test
    fun `failing header and payload DID validation returns DID mismatch`() {
        val response = defaultTestSerializer.decodeFromString(LinkedDomainsResponse.serializer(), docJwt)
        val domainLinkageCredentialJwt = response.linkedDids.first()
        coEvery { mockedJwtValidator.verifySignature(any()) } returns true
        coEvery { mockedJwtValidator.validateDidInHeaderAndPayload(any(), any()) } returns false

        runBlocking {
            val validated = jwtDomainLinkageCredentialValidator.validate(
                domainLinkageCredentialJwt,
                validRpDid,
                validDomainUrl
            )

            Assertions.assertThat(validated)
                .isEqualTo(DomainLinkageCredentialValidationResult.DID_MISMATCH)
        }
    }
}