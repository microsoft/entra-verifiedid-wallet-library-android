package com.microsoft.walletlibrary.did.sdk.datasource.network.credentialOperations

import com.microsoft.walletlibrary.did.sdk.credential.service.models.serviceResponses.IssuanceServiceResponse
import com.microsoft.walletlibrary.did.sdk.credential.service.validators.JwtValidator
import com.microsoft.walletlibrary.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.walletlibrary.did.sdk.datasource.network.apis.HttpAgentApiProvider
import com.microsoft.walletlibrary.did.sdk.datasource.network.apis.HttpAgentIssuanceApi
import com.microsoft.walletlibrary.did.sdk.util.controlflow.DidInHeaderAndPayloadNotMatching
import com.microsoft.walletlibrary.util.defaultTestSerializer
import com.microsoft.walletlibrary.util.http.httpagent.IResponse
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

class SendVerifiableCredentialIssuanceRequestNetworkOperationTest {
    private val expectedIssuerDid = "did:example:expected-issuer"
    private val attackerIssuerDid = "did:example:attacker"
    private val serializedCredential = "serialized-credential"
    private val response = IResponse(200, emptyMap(), ByteArray(0))

    @Test
    fun sendIssuanceResponse_withAttackerSigner_throwsIssuerMismatch() {
        val testContext = createTestContext(expectedIssuerDid)
        every {
            testContext.jwtValidator.validateDidInHeaderAndPayload(testContext.jwsToken, expectedIssuerDid)
        } returns false

        assertThatThrownBy {
            runBlocking { testContext.operation.toResult(response) }
        }.isInstanceOf(DidInHeaderAndPayloadNotMatching::class.java)

        verify(exactly = 1) {
            testContext.jwtValidator.validateDidInHeaderAndPayload(testContext.jwsToken, expectedIssuerDid)
        }
    }

    @Test
    fun sendIssuanceResponse_withPayloadIssuerMismatch_throwsIssuerMismatch() {
        val testContext = createTestContext(attackerIssuerDid)
        every {
            testContext.jwtValidator.validateDidInHeaderAndPayload(testContext.jwsToken, expectedIssuerDid)
        } returns true

        assertThatThrownBy {
            runBlocking { testContext.operation.toResult(response) }
        }.isInstanceOf(DidInHeaderAndPayloadNotMatching::class.java)
    }

    @Test
    fun sendIssuanceResponse_withMatchingIssuer_returnsCredential() {
        val testContext = createTestContext(expectedIssuerDid)
        every {
            testContext.jwtValidator.validateDidInHeaderAndPayload(testContext.jwsToken, expectedIssuerDid)
        } returns true

        val result = runBlocking { testContext.operation.toResult(response) }

        assertThat(result.isSuccess).isTrue
        val credential = result.getOrThrow()
        assertThat(credential.raw).isEqualTo(serializedCredential)
        assertThat(credential.contents.iss).isEqualTo(expectedIssuerDid)
        verify(exactly = 1) {
            testContext.jwtValidator.validateDidInHeaderAndPayload(testContext.jwsToken, expectedIssuerDid)
        }
    }

    private fun createTestContext(payloadIssuerDid: String): TestContext {
        val credentialContent = """
            {
              "jti": "credential-id",
              "vc": {
                "@context": ["https://www.w3.org/2018/credentials/v1"],
                "type": ["VerifiableCredential"],
                "credentialSubject": {"name": "Test User"}
              },
              "sub": "did:example:subject",
              "iss": "$payloadIssuerDid",
              "iat": 1
            }
        """.trimIndent()
        val jwsToken: JwsToken = mockk {
            every { content() } returns credentialContent
        }
        mockkObject(JwsToken.Companion)
        every { JwsToken.deserialize(serializedCredential) } returns jwsToken

        val issuanceApi: HttpAgentIssuanceApi = mockk {
            every { parseIssuance(response) } returns IssuanceServiceResponse(serializedCredential)
        }
        val apiProvider: HttpAgentApiProvider = mockk {
            every { issuanceApis } returns issuanceApi
        }
        val jwtValidator: JwtValidator = mockk {
            coEvery { verifySignature(jwsToken) } returns true
        }
        val operation = SendVerifiableCredentialIssuanceRequestNetworkOperation(
            "",
            "",
            expectedIssuerDid,
            apiProvider,
            jwtValidator,
            defaultTestSerializer
        )
        return TestContext(operation, jwtValidator, jwsToken)
    }

    private data class TestContext(
        val operation: SendVerifiableCredentialIssuanceRequestNetworkOperation,
        val jwtValidator: JwtValidator,
        val jwsToken: JwsToken
    )
}
