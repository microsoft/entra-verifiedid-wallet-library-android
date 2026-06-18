/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.did.sdk

import com.microsoft.walletlibrary.did.sdk.credential.models.CredentialStatusDescriptor
import com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredentialContent
import com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredentialDescriptor
import com.microsoft.walletlibrary.did.sdk.credential.service.validators.JwtValidator
import com.microsoft.walletlibrary.did.sdk.datasource.network.apis.HttpAgentApiProvider
import com.microsoft.walletlibrary.did.sdk.datasource.network.apis.HttpAgentStatusListApi
import com.microsoft.walletlibrary.util.defaultTestSerializer
import com.microsoft.walletlibrary.util.http.httpagent.IResponse
import com.microsoft.walletlibrary.verifiedid.VerifiableCredential
import com.microsoft.walletlibrary.verifiedid.VerifiedId
import com.microsoft.walletlibrary.verifiedid.VerifiedIdStatus
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.Base64
import java.util.zip.GZIPOutputStream
import com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredential as SdkVerifiableCredential

class StatusCheckServiceTest {

    private val apiProvider: HttpAgentApiProvider = mockk()
    private val statusListApi: HttpAgentStatusListApi = mockk()
    private val jwtValidator: JwtValidator = mockk()
    private val statusCheckService = StatusCheckService(apiProvider, defaultTestSerializer, jwtValidator)

    init {
        every { apiProvider.statusListApi } returns statusListApi
    }

    private fun buildVerifiableCredential(
        credentialStatus: CredentialStatusDescriptor?,
        issuer: String = "did:web:issuer.example",
        expiry: Long? = null
    ): VerifiableCredential {
        val raw = SdkVerifiableCredential(
            "jti-123",
            "rawToken",
            VerifiableCredentialContent(
                jti = "vc-jti",
                vc = VerifiableCredentialDescriptor(
                    context = emptyList(),
                    type = listOf("VerifiableCredential"),
                    credentialSubject = mapOf("name" to "test"),
                    credentialStatus = credentialStatus
                ),
                sub = "did:subject",
                iss = issuer,
                iat = 1234567L,
                exp = expiry
            )
        )
        return VerifiableCredential(raw)
    }

    /** Builds a StatusList2021 status list JSON body whose bit at [flaggedIndex] (if non-null) is set. */
    private fun buildStatusListJson(statusPurpose: String, flaggedIndex: Int?): String {
        val bitstring = ByteArray(125)
        flaggedIndex?.let { index ->
            bitstring[index / 8] = (bitstring[index / 8].toInt() or (1 shl (index % 8))).toByte()
        }
        val compressed = ByteArrayOutputStream().also { baos ->
            GZIPOutputStream(baos).use { it.write(bitstring) }
        }.toByteArray()
        val encodedList = Base64.getUrlEncoder().withoutPadding().encodeToString(compressed)
        return """{"credentialSubject":{"encodedList":"$encodedList","statusPurpose":"$statusPurpose"}}"""
    }

    private fun directUrlStatus(index: Int) = CredentialStatusDescriptor(
        id = "status-1",
        type = "StatusList2021Entry",
        statusListIndex = index,
        statusListCredential = STATUS_LIST_URL
    )

    private fun okResponse(body: String) =
        Result.success(IResponse(200, emptyMap(), body.toByteArray()))

    @Test
    fun checkVerifiedIdStatus_expiredCredential_returnsExpired() {
        val pastEpochSeconds = System.currentTimeMillis() / 1000 - 100_000
        val verifiedId = buildVerifiableCredential(credentialStatus = null, expiry = pastEpochSeconds)

        val result = runBlocking { statusCheckService.checkVerifiedIdStatus(verifiedId) }

        assertEquals(VerifiedIdStatus.Expired, result)
    }

    @Test
    fun checkVerifiedIdStatus_notAVerifiableCredential_returnsNoStatusEndpoint() {
        val verifiedId = mockk<VerifiedId>()
        every { verifiedId.expiresOn } returns null

        val result = runBlocking { statusCheckService.checkVerifiedIdStatus(verifiedId) }

        assertEquals(VerifiedIdStatus.NoStatusEndpoint, result)
    }

    @Test
    fun checkVerifiedIdStatus_noCredentialStatus_returnsNoStatusEndpoint() {
        val verifiedId = buildVerifiableCredential(credentialStatus = null)

        val result = runBlocking { statusCheckService.checkVerifiedIdStatus(verifiedId) }

        assertEquals(VerifiedIdStatus.NoStatusEndpoint, result)
    }

    @Test
    fun checkVerifiedIdStatus_unresolvableUrlAndNotUrnUuid_returnsUnknown() {
        // Empty status list credential and a non urn:uuid id cannot be resolved.
        val descriptor = CredentialStatusDescriptor(id = "status-1", type = "StatusList2021Entry")
        val verifiedId = buildVerifiableCredential(credentialStatus = descriptor)

        val result = runBlocking { statusCheckService.checkVerifiedIdStatus(verifiedId) }

        assertEquals(VerifiedIdStatus.Unknown, result)
    }

    @Test
    fun checkVerifiedIdStatus_statusListBitClear_returnsValid() {
        val verifiedId = buildVerifiableCredential(credentialStatus = directUrlStatus(index = 5))
        coEvery { statusListApi.getStatusListCredential(STATUS_LIST_URL) } returns
            okResponse(buildStatusListJson(statusPurpose = "revocation", flaggedIndex = null))

        val result = runBlocking { statusCheckService.checkVerifiedIdStatus(verifiedId) }

        assertEquals(VerifiedIdStatus.Valid, result)
    }

    @Test
    fun checkVerifiedIdStatus_statusListBitSetForRevocation_returnsRevoked() {
        val verifiedId = buildVerifiableCredential(credentialStatus = directUrlStatus(index = 5))
        coEvery { statusListApi.getStatusListCredential(STATUS_LIST_URL) } returns
            okResponse(buildStatusListJson(statusPurpose = "revocation", flaggedIndex = 5))

        val result = runBlocking { statusCheckService.checkVerifiedIdStatus(verifiedId) }

        assertEquals(VerifiedIdStatus.Revoked, result)
    }

    @Test
    fun checkVerifiedIdStatus_statusListBitSetForSuspension_returnsSuspended() {
        val verifiedId = buildVerifiableCredential(credentialStatus = directUrlStatus(index = 5))
        coEvery { statusListApi.getStatusListCredential(STATUS_LIST_URL) } returns
            okResponse(buildStatusListJson(statusPurpose = "suspension", flaggedIndex = 5))

        val result = runBlocking { statusCheckService.checkVerifiedIdStatus(verifiedId) }

        assertEquals(VerifiedIdStatus.Suspended, result)
    }

    @Test
    fun checkVerifiedIdStatus_indexBeyondStatusListBitstring_returnsUnknown() {
        // The generated status list holds 1000 bits; an index past the end is indeterminate.
        val verifiedId = buildVerifiableCredential(credentialStatus = directUrlStatus(index = 100_000))
        coEvery { statusListApi.getStatusListCredential(STATUS_LIST_URL) } returns
            okResponse(buildStatusListJson(statusPurpose = "revocation", flaggedIndex = null))

        val result = runBlocking { statusCheckService.checkVerifiedIdStatus(verifiedId) }

        assertEquals(VerifiedIdStatus.Unknown, result)
    }

    @Test
    fun checkVerifiedIdStatus_statusListFetchFails_returnsUnknown() {
        val verifiedId = buildVerifiableCredential(credentialStatus = directUrlStatus(index = 5))
        coEvery { statusListApi.getStatusListCredential(STATUS_LIST_URL) } returns
            Result.failure(IOException("network down"))

        val result = runBlocking { statusCheckService.checkVerifiedIdStatus(verifiedId) }

        assertEquals(VerifiedIdStatus.Unknown, result)
    }

    @Test
    fun checkVerifiedIdStatus_statusListReturnsNon2xx_returnsUnknown() {
        // A host-provided HTTP agent may surface a non-2xx response as a success Result.
        val verifiedId = buildVerifiableCredential(credentialStatus = directUrlStatus(index = 5))
        val notFound = Result.success(IResponse(404, emptyMap(), ByteArray(0)))
        coEvery { statusListApi.getStatusListCredential(STATUS_LIST_URL) } returns notFound

        val result = runBlocking { statusCheckService.checkVerifiedIdStatus(verifiedId) }

        assertEquals(VerifiedIdStatus.Unknown, result)
    }

    @Test
    fun checkVerifiedIdStatus_jwtStatusListWithInvalidSignature_returnsUnknown() {
        val verifiedId = buildVerifiableCredential(credentialStatus = directUrlStatus(index = 5))
        val header = Base64.getUrlEncoder().withoutPadding()
            .encodeToString("""{"alg":"ES256"}""".toByteArray())
        val payload = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(buildStatusListJson(statusPurpose = "revocation", flaggedIndex = 5).toByteArray())
        val signedStatusList = "$header.$payload.AAAA"
        coEvery { statusListApi.getStatusListCredential(STATUS_LIST_URL) } returns okResponse(signedStatusList)
        coEvery { jwtValidator.verifySignature(any()) } returns false

        val result = runBlocking { statusCheckService.checkVerifiedIdStatus(verifiedId) }

        assertEquals(VerifiedIdStatus.Unknown, result)
    }

    @Test
    fun checkVerifiedIdStatus_jwtStatusListSignerNotCredentialIssuer_returnsUnknown() {
        val verifiedId = buildVerifiableCredential(credentialStatus = directUrlStatus(index = 5))
        coEvery { statusListApi.getStatusListCredential(STATUS_LIST_URL) } returns
            okResponse(signedStatusListJwt(statusPurpose = "revocation", flaggedIndex = 5))
        // Signature is valid, but the signer DID is not the credential's issuer.
        coEvery { jwtValidator.verifySignature(any()) } returns true
        every { jwtValidator.validateDidInHeaderAndPayload(any(), any()) } returns false

        val result = runBlocking { statusCheckService.checkVerifiedIdStatus(verifiedId) }

        assertEquals(VerifiedIdStatus.Unknown, result)
    }

    @Test
    fun checkVerifiedIdStatus_jwtStatusListSignedByIssuerAndBitSet_returnsRevoked() {
        val verifiedId = buildVerifiableCredential(credentialStatus = directUrlStatus(index = 5))
        coEvery { statusListApi.getStatusListCredential(STATUS_LIST_URL) } returns
            okResponse(signedStatusListJwt(statusPurpose = "revocation", flaggedIndex = 5))
        coEvery { jwtValidator.verifySignature(any()) } returns true
        every { jwtValidator.validateDidInHeaderAndPayload(any(), any()) } returns true

        val result = runBlocking { statusCheckService.checkVerifiedIdStatus(verifiedId) }

        assertEquals(VerifiedIdStatus.Revoked, result)
    }

    /** Builds a compact JWS whose payload is the StatusList2021 status list JSON. */
    private fun signedStatusListJwt(statusPurpose: String, flaggedIndex: Int?): String {
        val header = Base64.getUrlEncoder().withoutPadding()
            .encodeToString("""{"alg":"ES256"}""".toByteArray())
        val payload = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(buildStatusListJson(statusPurpose, flaggedIndex).toByteArray())
        return "$header.$payload.AAAA"
    }

    private companion object {
        const val STATUS_LIST_URL = "https://issuer.example/status/1"
    }
}
