// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.did.sdk

import com.microsoft.walletlibrary.did.sdk.credential.service.models.serviceResponses.LinkedDomainsResponse
import com.microsoft.walletlibrary.did.sdk.credential.service.validators.DomainLinkageCredentialValidationResult
import com.microsoft.walletlibrary.did.sdk.credential.service.validators.DomainLinkageCredentialValidator
import com.microsoft.walletlibrary.did.sdk.datasource.network.apis.HttpAgentApiProvider
import com.microsoft.walletlibrary.did.sdk.identifier.models.identifierdocument.IdentifierDocument
import com.microsoft.walletlibrary.did.sdk.identifier.models.payload.document.IdentifierDocumentService
import com.microsoft.walletlibrary.util.http.httpagent.IResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class IssuerLinkedDomainTest {
    private val apiProvider = mockk<HttpAgentApiProvider>(relaxed = true)
    private val linkedDomainsApi = apiProvider.linkedDomainsApis
    private val validator = mockk<DomainLinkageCredentialValidator>()
    private val service = LinkedDomainsService(apiProvider, mockk(), validator)
    private val did = "did:web:issuer.example"
    private val origin = "https://issuer.example:8443"
    private val response = IResponse(200, emptyMap(), ByteArray(0))

    @Test
    fun `matches later service entry and later credential with exact port`() = runBlocking {
        val document = document("https://other.example", "https://ISSUER.example:8443/")
        serve(listOf("unrelated", "matching"))
        coEvery { validator.validate("unrelated", did, origin, true) } returns
            DomainLinkageCredentialValidationResult.DID_MISMATCH
        coEvery { validator.validate("matching", did, origin, true) } returns
            DomainLinkageCredentialValidationResult.VALID

        assertThat(service.validateLinkedDomainForOrigin(document, origin)).isTrue
        coVerify(exactly = 1) {
            linkedDomainsApi.fetchWellKnownConfigDocument(
                "$origin/.well-known/did-configuration.json", true
            )
        }
    }

    @Test
    fun `same host on different port is not a matching origin`() = runBlocking {
        assertThat(service.validateLinkedDomainForOrigin(document("https://issuer.example"), origin)).isFalse
        coVerify(exactly = 0) { linkedDomainsApi.fetchWellKnownConfigDocument(any(), any()) }
    }

    @Test
    fun `missing and malformed linked domains do not become trusted`() = runBlocking {
        assertThat(service.validateLinkedDomainForOrigin(document(), origin)).isFalse
        assertThat(service.validateLinkedDomainForOrigin(document("not a url", "http://issuer.example:8443"), origin)).isFalse
        coVerify(exactly = 0) { linkedDomainsApi.fetchWellKnownConfigDocument(any(), any()) }
    }

    @Test
    fun `empty and invalid proof lists fail closed`() = runBlocking<Unit> {
        serve(emptyList())
        assertThat(service.validateLinkedDomainForOrigin(document(origin), origin)).isFalse
        serve(listOf("invalid"))
        coEvery { validator.validate("invalid", did, origin, true) } returns
            DomainLinkageCredentialValidationResult.ORIGIN_MISMATCH
        assertThat(service.validateLinkedDomainForOrigin(document(origin), origin)).isFalse
    }

    @Test
    fun `fetch failure and cancellation do not turn into verified trust`() = runBlocking<Unit> {
        coEvery {
            linkedDomainsApi.fetchWellKnownConfigDocument(any(), true)
        } returns Result.failure(IllegalStateException("fetch failed"))
        assertThat(runCatching { service.validateLinkedDomainForOrigin(document(origin), origin) }.isFailure).isTrue
        coEvery {
            linkedDomainsApi.fetchWellKnownConfigDocument(any(), true)
        } throws CancellationException()
        assertThat(runCatching { service.validateLinkedDomainForOrigin(document(origin), origin) }.exceptionOrNull())
            .isInstanceOf(CancellationException::class.java)
    }

    private fun document(vararg origins: String) = IdentifierDocument(id = did).apply {
        service = listOf(IdentifierDocumentService("#linked", "LinkedDomains", origins.toList()))
    }

    private fun serve(credentials: List<String>) {
        coEvery {
            linkedDomainsApi.fetchWellKnownConfigDocument(any(), true)
        } returns Result.success(response)
        every { linkedDomainsApi.toLinkedDomainsResponse(response) } returns
            LinkedDomainsResponse(context = "https://identity.foundation/.well-known/did-configuration/v1", linkedDids = credentials)
    }
}
