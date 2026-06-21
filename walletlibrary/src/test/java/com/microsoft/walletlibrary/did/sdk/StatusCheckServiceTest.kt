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
import com.microsoft.walletlibrary.did.sdk.identifier.models.identifierdocument.IdentifierDocument
import com.microsoft.walletlibrary.did.sdk.identifier.models.payload.document.IdentifierDocumentService
import com.microsoft.walletlibrary.util.defaultTestSerializer
import com.microsoft.walletlibrary.util.http.httpagent.IResponse
import com.microsoft.walletlibrary.verifiedid.VerifiableCredential
import com.microsoft.walletlibrary.verifiedid.VerifiedId
import com.microsoft.walletlibrary.verifiedid.VerifiedIdStatus
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
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
    private val mockLinkedDomainsService: LinkedDomainsService = mockk()

    init {
        every { apiProvider.statusListApi } returns statusListApi
    }

    @Before
    fun setUp() {
        mockkObject(VerifiableCredentialSdk)
        every { VerifiableCredentialSdk.linkedDomainsService } returns mockLinkedDomainsService
    }

    @After
    fun tearDown() {
        unmockkObject(VerifiableCredentialSdk)
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
    private fun buildStatusListJson(statusPurpose: String, flaggedIndex: Int?, exp: Long? = null): String {
        val bitstring = ByteArray(125)
        flaggedIndex?.let { index ->
            bitstring[index / 8] = (bitstring[index / 8].toInt() or (1 shl (index % 8))).toByte()
        }
        val compressed = ByteArrayOutputStream().also { baos ->
            GZIPOutputStream(baos).use { it.write(bitstring) }
        }.toByteArray()
        val encodedList = Base64.getUrlEncoder().withoutPadding().encodeToString(compressed)
        val expPart = if (exp != null) ""","exp":$exp""" else ""
        return """{"credentialSubject":{"encodedList":"$encodedList","statusPurpose":"$statusPurpose"}$expPart}"""
    }

    private fun directUrlStatus(index: Int, statusPurpose: String = "") = CredentialStatusDescriptor(
        id = "status-1",
        type = "StatusList2021Entry",
        statusPurpose = statusPurpose,
        statusListIndex = index,
        statusListCredential = STATUS_LIST_URL
    )

    private fun okResponse(body: String) =
        Result.success(IResponse(200, emptyMap(), body.toByteArray()))

    // ─── Direct URL path tests ─────────────────────────────────────────────────

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
            okResponse(signedStatusListJwt(statusPurpose = "revocation", flaggedIndex = null))
        coEvery { jwtValidator.verifySignature(any()) } returns true
        every { jwtValidator.validateDidInHeaderAndPayload(any(), any()) } returns true

        val result = runBlocking { statusCheckService.checkVerifiedIdStatus(verifiedId) }

        assertEquals(VerifiedIdStatus.Valid, result)
    }

    @Test
    fun checkVerifiedIdStatus_statusListBitSetForRevocation_returnsRevoked() {
        val verifiedId = buildVerifiableCredential(credentialStatus = directUrlStatus(index = 5))
        coEvery { statusListApi.getStatusListCredential(STATUS_LIST_URL) } returns
            okResponse(signedStatusListJwt(statusPurpose = "revocation", flaggedIndex = 5))
        coEvery { jwtValidator.verifySignature(any()) } returns true
        every { jwtValidator.validateDidInHeaderAndPayload(any(), any()) } returns true

        val result = runBlocking { statusCheckService.checkVerifiedIdStatus(verifiedId) }

        assertEquals(VerifiedIdStatus.Revoked, result)
    }

    @Test
    fun checkVerifiedIdStatus_statusListBitSetForSuspension_returnsSuspended() {
        val verifiedId = buildVerifiableCredential(credentialStatus = directUrlStatus(index = 5))
        coEvery { statusListApi.getStatusListCredential(STATUS_LIST_URL) } returns
            okResponse(signedStatusListJwt(statusPurpose = "suspension", flaggedIndex = 5))
        coEvery { jwtValidator.verifySignature(any()) } returns true
        every { jwtValidator.validateDidInHeaderAndPayload(any(), any()) } returns true

        val result = runBlocking { statusCheckService.checkVerifiedIdStatus(verifiedId) }

        assertEquals(VerifiedIdStatus.Suspended, result)
    }

    @Test
    fun checkVerifiedIdStatus_indexBeyondStatusListBitstring_returnsUnknown() {
        // The generated status list holds 1000 bits; an index past the end is indeterminate.
        val verifiedId = buildVerifiableCredential(credentialStatus = directUrlStatus(index = 100_000))
        coEvery { statusListApi.getStatusListCredential(STATUS_LIST_URL) } returns
            okResponse(signedStatusListJwt(statusPurpose = "revocation", flaggedIndex = null))
        coEvery { jwtValidator.verifySignature(any()) } returns true
        every { jwtValidator.validateDidInHeaderAndPayload(any(), any()) } returns true

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

    @Test
    fun checkVerifiedIdStatus_unsignedJsonStatusList_returnsUnknown() {
        // An attacker-controlled, unsigned JSON status list with the bit set must NOT be trusted:
        // a valid credential must never be reported Revoked/Suspended from unsigned status data.
        val verifiedId = buildVerifiableCredential(credentialStatus = directUrlStatus(index = 5))
        coEvery { statusListApi.getStatusListCredential(STATUS_LIST_URL) } returns
            okResponse(buildStatusListJson(statusPurpose = "revocation", flaggedIndex = 5))

        val result = runBlocking { statusCheckService.checkVerifiedIdStatus(verifiedId) }

        assertEquals(VerifiedIdStatus.Unknown, result)
    }

    @Test
    fun checkVerifiedIdStatus_statusPurposeMismatch_returnsUnknown() {
        // Credential declares "revocation" but the (validly signed) status list is for "suspension".
        // A set bit must not be interpreted against a purpose the issuer did not declare here.
        val verifiedId = buildVerifiableCredential(
            credentialStatus = directUrlStatus(index = 5, statusPurpose = "revocation")
        )
        coEvery { statusListApi.getStatusListCredential(STATUS_LIST_URL) } returns
            okResponse(signedStatusListJwt(statusPurpose = "suspension", flaggedIndex = 5))
        coEvery { jwtValidator.verifySignature(any()) } returns true
        every { jwtValidator.validateDidInHeaderAndPayload(any(), any()) } returns true

        val result = runBlocking { statusCheckService.checkVerifiedIdStatus(verifiedId) }

        assertEquals(VerifiedIdStatus.Unknown, result)
    }

    @Test
    fun checkVerifiedIdStatus_statusPurposeMatchesAndBitSet_returnsRevoked() {
        val verifiedId = buildVerifiableCredential(
            credentialStatus = directUrlStatus(index = 5, statusPurpose = "revocation")
        )
        coEvery { statusListApi.getStatusListCredential(STATUS_LIST_URL) } returns
            okResponse(signedStatusListJwt(statusPurpose = "revocation", flaggedIndex = 5))
        coEvery { jwtValidator.verifySignature(any()) } returns true
        every { jwtValidator.validateDidInHeaderAndPayload(any(), any()) } returns true

        val result = runBlocking { statusCheckService.checkVerifiedIdStatus(verifiedId) }

        assertEquals(VerifiedIdStatus.Revoked, result)
    }

    @Test
    fun checkVerifiedIdStatus_expiredSignedStatusList_returnsUnknown() {
        // A validly signed but expired status list could be a replay of a stale snapshot; it must
        // not be trusted to mark a now-valid credential as revoked.
        val pastEpochSeconds = System.currentTimeMillis() / 1000 - 100_000
        val verifiedId = buildVerifiableCredential(credentialStatus = directUrlStatus(index = 5))
        coEvery { statusListApi.getStatusListCredential(STATUS_LIST_URL) } returns
            okResponse(signedStatusListJwt(statusPurpose = "revocation", flaggedIndex = 5, exp = pastEpochSeconds))
        coEvery { jwtValidator.verifySignature(any()) } returns true
        every { jwtValidator.validateDidInHeaderAndPayload(any(), any()) } returns true

        val result = runBlocking { statusCheckService.checkVerifiedIdStatus(verifiedId) }

        assertEquals(VerifiedIdStatus.Unknown, result)
    }

    @Test
    fun checkVerifiedIdStatus_unexpiredSignedStatusListBitSet_returnsRevoked() {
        val futureEpochSeconds = System.currentTimeMillis() / 1000 + 100_000
        val verifiedId = buildVerifiableCredential(credentialStatus = directUrlStatus(index = 5))
        coEvery { statusListApi.getStatusListCredential(STATUS_LIST_URL) } returns
            okResponse(signedStatusListJwt(statusPurpose = "revocation", flaggedIndex = 5, exp = futureEpochSeconds))
        coEvery { jwtValidator.verifySignature(any()) } returns true
        every { jwtValidator.validateDidInHeaderAndPayload(any(), any()) } returns true

        val result = runBlocking { statusCheckService.checkVerifiedIdStatus(verifiedId) }

        assertEquals(VerifiedIdStatus.Revoked, result)
    }

    // ─── IdentityHub path tests ────────────────────────────────────────────────

    @Test
    fun checkVerifiedIdStatus_identityHub_didResolutionFails_returnsUnknown() {
        val descriptor = CredentialStatusDescriptor(
            id = "urn:uuid:550e8400-e29b-41d4-a716-446655440000?bit-index=7",
            type = "RevocationList2021Status",
            statusListCredential = ""
        )
        val verifiedId = buildVerifiableCredential(credentialStatus = descriptor)
        coEvery { mockLinkedDomainsService.resolveIdentifierDocument(any()) } returns
            Result.failure(IOException("resolution failed"))

        val result = runBlocking { statusCheckService.checkVerifiedIdStatus(verifiedId) }

        assertEquals(VerifiedIdStatus.Unknown, result)
    }

    @Test
    fun checkVerifiedIdStatus_identityHub_noIdentityHubServiceInDidDoc_returnsUnknown() {
        val descriptor = CredentialStatusDescriptor(
            id = "urn:uuid:550e8400-e29b-41d4-a716-446655440000?bit-index=7",
            type = "RevocationList2021Status",
            statusListCredential = ""
        )
        val verifiedId = buildVerifiableCredential(credentialStatus = descriptor)
        val identifierDoc = IdentifierDocument(id = "did:web:issuer.example").apply {
            service = listOf(
                IdentifierDocumentService(id = "#linked-domains", type = "LinkedDomains", serviceEndpoint = listOf("https://issuer.example"))
            )
        }
        coEvery { mockLinkedDomainsService.resolveIdentifierDocument(any()) } returns
            Result.success(identifierDoc)

        val result = runBlocking { statusCheckService.checkVerifiedIdStatus(verifiedId) }

        assertEquals(VerifiedIdStatus.Unknown, result)
    }

    @Test
    fun checkVerifiedIdStatus_identityHub_collectionsQueryPostFails_returnsUnknown() {
        val descriptor = CredentialStatusDescriptor(
            id = "urn:uuid:550e8400-e29b-41d4-a716-446655440000?bit-index=7",
            type = "RevocationList2021Status",
            statusListCredential = ""
        )
        val verifiedId = buildVerifiableCredential(credentialStatus = descriptor)
        val identifierDoc = IdentifierDocument(id = "did:web:issuer.example").apply {
            service = listOf(
                IdentifierDocumentService(id = "#hub", type = "IdentityHub", serviceEndpoint = listOf(IDENTITY_HUB_URL))
            )
        }
        coEvery { mockLinkedDomainsService.resolveIdentifierDocument(any()) } returns
            Result.success(identifierDoc)
        coEvery { statusListApi.postCollectionsQuery(IDENTITY_HUB_URL, any()) } returns
            Result.failure(IOException("network error"))

        val result = runBlocking { statusCheckService.checkVerifiedIdStatus(verifiedId) }

        assertEquals(VerifiedIdStatus.Unknown, result)
    }

    @Test
    fun checkVerifiedIdStatus_identityHub_collectionsQueryReturnsNon2xx_returnsUnknown() {
        val descriptor = CredentialStatusDescriptor(
            id = "urn:uuid:550e8400-e29b-41d4-a716-446655440000?bit-index=7",
            type = "RevocationList2021Status",
            statusListCredential = ""
        )
        val verifiedId = buildVerifiableCredential(credentialStatus = descriptor)
        val identifierDoc = IdentifierDocument(id = "did:web:issuer.example").apply {
            service = listOf(
                IdentifierDocumentService(id = "#hub", type = "IdentityHub", serviceEndpoint = listOf(IDENTITY_HUB_URL))
            )
        }
        coEvery { mockLinkedDomainsService.resolveIdentifierDocument(any()) } returns
            Result.success(identifierDoc)
        coEvery { statusListApi.postCollectionsQuery(IDENTITY_HUB_URL, any()) } returns
            Result.success(IResponse(500, emptyMap(), ByteArray(0)))

        val result = runBlocking { statusCheckService.checkVerifiedIdStatus(verifiedId) }

        assertEquals(VerifiedIdStatus.Unknown, result)
    }

    @Test
    fun checkVerifiedIdStatus_identityHub_envelopeWithBase64JwtEntry_bitSet_returnsRevoked() {
        val descriptor = CredentialStatusDescriptor(
            id = "urn:uuid:550e8400-e29b-41d4-a716-446655440000?bit-index=7",
            type = "RevocationList2021Status",
            statusListCredential = ""
        )
        val verifiedId = buildVerifiableCredential(credentialStatus = descriptor)
        val identifierDoc = IdentifierDocument(id = "did:web:issuer.example").apply {
            service = listOf(
                IdentifierDocumentService(id = "#hub", type = "IdentityHub", serviceEndpoint = listOf(IDENTITY_HUB_URL))
            )
        }
        coEvery { mockLinkedDomainsService.resolveIdentifierDocument(any()) } returns
            Result.success(identifierDoc)

        // Build a CollectionsQuery response envelope with base64url-encoded status list JWT in entries[].data
        val statusListJwt = signedStatusListJwt(statusPurpose = "revocation", flaggedIndex = 7)
        val base64Data = Base64.getUrlEncoder().withoutPadding().encodeToString(statusListJwt.toByteArray())
        val envelopeBody = """{"replies":[{"entries":[{"data":"$base64Data"}]}]}"""

        coEvery { statusListApi.postCollectionsQuery(IDENTITY_HUB_URL, any()) } returns
            okResponse(envelopeBody)
        coEvery { jwtValidator.verifySignature(any()) } returns true
        every { jwtValidator.validateDidInHeaderAndPayload(any(), any()) } returns true

        val result = runBlocking { statusCheckService.checkVerifiedIdStatus(verifiedId) }

        assertEquals(VerifiedIdStatus.Revoked, result)
    }

    @Test
    fun checkVerifiedIdStatus_identityHub_envelopeWithRawJwtEntry_bitClear_returnsValid() {
        val descriptor = CredentialStatusDescriptor(
            id = "urn:uuid:550e8400-e29b-41d4-a716-446655440000?bit-index=7",
            type = "RevocationList2021Status",
            statusListCredential = ""
        )
        val verifiedId = buildVerifiableCredential(credentialStatus = descriptor)
        val identifierDoc = IdentifierDocument(id = "did:web:issuer.example").apply {
            service = listOf(
                IdentifierDocumentService(id = "#hub", type = "IdentityHub", serviceEndpoint = listOf(IDENTITY_HUB_URL))
            )
        }
        coEvery { mockLinkedDomainsService.resolveIdentifierDocument(any()) } returns
            Result.success(identifierDoc)

        // entries[].data is the raw JWT (not base64-wrapped)
        val statusListJwt = signedStatusListJwt(statusPurpose = "revocation", flaggedIndex = null)
        val envelopeBody = """{"replies":[{"entries":[{"data":"$statusListJwt"}]}]}"""

        coEvery { statusListApi.postCollectionsQuery(IDENTITY_HUB_URL, any()) } returns
            okResponse(envelopeBody)
        coEvery { jwtValidator.verifySignature(any()) } returns true
        every { jwtValidator.validateDidInHeaderAndPayload(any(), any()) } returns true

        val result = runBlocking { statusCheckService.checkVerifiedIdStatus(verifiedId) }

        assertEquals(VerifiedIdStatus.Valid, result)
    }

    @Test
    fun checkVerifiedIdStatus_identityHub_directJwtResponse_bitSetSuspension_returnsSuspended() {
        // Some IdentityHub implementations return the status list JWT directly (not wrapped in envelope)
        val descriptor = CredentialStatusDescriptor(
            id = "urn:uuid:550e8400-e29b-41d4-a716-446655440000?bit-index=7",
            type = "RevocationList2021Status",
            statusListCredential = ""
        )
        val verifiedId = buildVerifiableCredential(credentialStatus = descriptor)
        val identifierDoc = IdentifierDocument(id = "did:web:issuer.example").apply {
            service = listOf(
                IdentifierDocumentService(id = "#hub", type = "IdentityHub", serviceEndpoint = listOf(IDENTITY_HUB_URL))
            )
        }
        coEvery { mockLinkedDomainsService.resolveIdentifierDocument(any()) } returns
            Result.success(identifierDoc)

        // Response body IS the signed JWT directly
        val statusListJwt = signedStatusListJwt(statusPurpose = "suspension", flaggedIndex = 7)
        coEvery { statusListApi.postCollectionsQuery(IDENTITY_HUB_URL, any()) } returns
            okResponse(statusListJwt)
        coEvery { jwtValidator.verifySignature(any()) } returns true
        every { jwtValidator.validateDidInHeaderAndPayload(any(), any()) } returns true

        val result = runBlocking { statusCheckService.checkVerifiedIdStatus(verifiedId) }

        assertEquals(VerifiedIdStatus.Suspended, result)
    }

    @Test
    fun checkVerifiedIdStatus_identityHub_envelopeWithInvalidSignature_returnsUnknown() {
        val descriptor = CredentialStatusDescriptor(
            id = "urn:uuid:550e8400-e29b-41d4-a716-446655440000?bit-index=7",
            type = "RevocationList2021Status",
            statusListCredential = ""
        )
        val verifiedId = buildVerifiableCredential(credentialStatus = descriptor)
        val identifierDoc = IdentifierDocument(id = "did:web:issuer.example").apply {
            service = listOf(
                IdentifierDocumentService(id = "#hub", type = "IdentityHub", serviceEndpoint = listOf(IDENTITY_HUB_URL))
            )
        }
        coEvery { mockLinkedDomainsService.resolveIdentifierDocument(any()) } returns
            Result.success(identifierDoc)

        val statusListJwt = signedStatusListJwt(statusPurpose = "revocation", flaggedIndex = 7)
        val base64Data = Base64.getUrlEncoder().withoutPadding().encodeToString(statusListJwt.toByteArray())
        val envelopeBody = """{"replies":[{"entries":[{"data":"$base64Data"}]}]}"""

        coEvery { statusListApi.postCollectionsQuery(IDENTITY_HUB_URL, any()) } returns
            okResponse(envelopeBody)
        coEvery { jwtValidator.verifySignature(any()) } returns false

        val result = runBlocking { statusCheckService.checkVerifiedIdStatus(verifiedId) }

        assertEquals(VerifiedIdStatus.Unknown, result)
    }

    // ─── didUrlQueryParameter tests ────────────────────────────────────────────

    /** Builds a compact JWS whose payload is the StatusList2021 status list JSON. */
    private fun signedStatusListJwt(statusPurpose: String, flaggedIndex: Int?, exp: Long? = null): String {
        val header = Base64.getUrlEncoder().withoutPadding()
            .encodeToString("""{"alg":"ES256"}""".toByteArray())
        val payload = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(buildStatusListJson(statusPurpose, flaggedIndex, exp).toByteArray())
        return "$header.$payload.AAAA"
    }

    /** Invokes the private didUrlQueryParameter via reflection so it stays private in production. */
    private fun invokeDidUrlQueryParameter(url: String, key: String): String? {
        val method = StatusCheckService::class.java
            .getDeclaredMethod("didUrlQueryParameter", String::class.java, String::class.java)
            .apply { isAccessible = true }
        return method.invoke(statusCheckService, url, key) as String?
    }

    @Test
    fun didUrlQueryParameter_didWebServiceAndQueries_returnsValues() {
        val url = "did:web:example.com?service=IdentityHub&queries=eyJhIjoxfQ"
        assertEquals("IdentityHub", invokeDidUrlQueryParameter(url, "service"))
        assertEquals("eyJhIjoxfQ", invokeDidUrlQueryParameter(url, "queries"))
    }

    @Test
    fun didUrlQueryParameter_urnUuidBitIndex_returnsValue() {
        assertEquals("42", invokeDidUrlQueryParameter("urn:uuid:550e8400-e29b-41d4-a716-446655440000?bit-index=42", "bit-index"))
    }

    @Test
    fun didUrlQueryParameter_percentEncodedValue_isUrlDecoded() {
        assertEquals("a/b c", invokeDidUrlQueryParameter("did:web:example.com?x=a%2Fb%20c", "x"))
    }

    @Test
    fun didUrlQueryParameter_fragmentIsExcluded() {
        assertEquals("IdentityHub", invokeDidUrlQueryParameter("did:web:example.com?service=IdentityHub#frag", "service"))
    }

    @Test
    fun didUrlQueryParameter_missingKey_returnsNull() {
        assertNull(invokeDidUrlQueryParameter("did:web:example.com?service=IdentityHub", "queries"))
    }

    @Test
    fun didUrlQueryParameter_noQuery_returnsNull() {
        assertNull(invokeDidUrlQueryParameter("did:web:example.com", "service"))
    }

    @Test
    fun didUrlQueryParameter_keyWithEmptyValue_returnsEmptyString() {
        assertEquals("", invokeDidUrlQueryParameter("did:web:example.com?service=", "service"))
    }

    private companion object {
        const val STATUS_LIST_URL = "https://issuer.example/status/1"
        const val IDENTITY_HUB_URL = "https://hub.issuer.example/collections"
    }
}
