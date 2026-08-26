package com.microsoft.walletlibrary.did.sdk.datasource.network.identifierOperations

import com.microsoft.walletlibrary.did.sdk.datasource.network.apis.HttpAgentApiProvider
import com.microsoft.walletlibrary.did.sdk.datasource.network.apis.HttpAgentIdentifierApi
import com.microsoft.walletlibrary.did.sdk.identifier.models.identifierdocument.IdentifierResponse
import com.microsoft.walletlibrary.did.sdk.util.controlflow.ResolverException
import com.microsoft.walletlibrary.util.http.httpagent.IResponse
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.Test

class ResolveIdentifierNetworkOperationTest {

    @Test
    fun `rejects malformed did before making request`() {
        val apiProvider: HttpAgentApiProvider = mockk(relaxed = true)

        val throwable = catchThrowable {
            ResolveIdentifierNetworkOperation(apiProvider, "https://resolver.example", "did:web:example.com:..:evil", true)
        }

        assertThat(throwable).isInstanceOf(ResolverException::class.java)
        assertThat(throwable.message).contains("is not a syntactically valid DID")
    }

    @Test
    fun `allows malformed did when hardening is disabled`() {
        val apiProvider: HttpAgentApiProvider = mockk(relaxed = true)

        val operation = ResolveIdentifierNetworkOperation(
            apiProvider,
            "https://resolver.example",
            "did:web:example.com:..:evil",
            false
        )

        assertThat(operation.identifier).isEqualTo("did:web:example.com:..:evil")
    }

    @Test
    fun `uses the sanitized did when constructing the resolver URL`() {
        val apiProvider: HttpAgentApiProvider = mockk()
        val identifierApi: HttpAgentIdentifierApi = mockk()
        val response: IResponse = mockk()
        val identifierResponse: IdentifierResponse = mockk()

        every { apiProvider.identifierApi } returns identifierApi
        coEvery { identifierApi.resolveIdentifier("https://resolver.example/did:example:123") } returns Result.success(response)
        every { identifierApi.toIdentifierResponse(response) } returns identifierResponse

        val operation = ResolveIdentifierNetworkOperation(apiProvider, "https://resolver.example", "did:example:123", true)

        runBlocking {
            val result = operation.fire()
            assertThat(result.isSuccess).isTrue()
            assertThat(result.getOrThrow()).isSameAs(identifierResponse)
        }
    }
}
