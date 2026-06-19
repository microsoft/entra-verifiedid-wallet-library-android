/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.did.sdk

import android.net.Uri
import android.util.Base64
import com.microsoft.walletlibrary.did.sdk.credential.models.CredentialStatusDescriptor
import com.microsoft.walletlibrary.did.sdk.credential.service.validators.JwtValidator
import com.microsoft.walletlibrary.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.walletlibrary.did.sdk.datasource.network.apis.HttpAgentApiProvider
import com.microsoft.walletlibrary.did.sdk.util.Constants
import com.microsoft.walletlibrary.did.sdk.util.log.SdkLog
import com.microsoft.walletlibrary.verifiedid.VerifiedId
import com.microsoft.walletlibrary.verifiedid.VerifiedIdStatus
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import java.io.ByteArrayInputStream
import java.util.Date
import java.util.zip.GZIPInputStream
import com.microsoft.walletlibrary.verifiedid.VerifiableCredential as WalletVerifiableCredential

/**
 * Determines the [VerifiedIdStatus] of a [VerifiedId].
 *
 * Check order:
 * 1. Expiry — evaluated from the credential's own `expiresOn` field, no network call.
 * 2. W3C StatusList2021 — fetches the issuer's status list and checks the credential's bit.
 *
 * The signature and issuer validation performed here is defense-in-depth. The authoritative status
 * enforcement happens server-side when a [VerifiedId] is presented.
 *
 * Returns [VerifiedIdStatus.NoStatusEndpoint] when the credential carries no status endpoint, and
 * [VerifiedIdStatus.Unknown] when the status could not be determined (e.g. network or format error).
 */
internal class StatusCheckService(
    private val apiProvider: HttpAgentApiProvider,
    private val json: Json,
    private val jwtValidator: JwtValidator
) {

    private companion object {
        const val TAG = "VID_STATUS_CHECK"
        val HTTP_SUCCESS_RANGE = 200..299
        // Clock skew for the status list JWT exp check (mirrors the server's clockTolerance).
        const val STATUS_LIST_CLOCK_SKEW_SECONDS = 300L
    }

    suspend fun checkVerifiedIdStatus(verifiedId: VerifiedId): VerifiedIdStatus {
        // 1. Expiry check — from the credential's own expiresOn field, no network call.
        verifiedId.expiresOn?.let { expiresOn ->
            if (Date().after(expiresOn)) {
                SdkLog.i("$TAG result=Expired (past expiresOn)")
                return VerifiedIdStatus.Expired
            }
        }

        // 2. StatusList2021 — only applicable to JWT-backed VerifiableCredential impls.
        val credential = verifiedId as? WalletVerifiableCredential
            ?: run {
                SdkLog.i("$TAG result=NoStatusEndpoint (not a JWT-backed VerifiableCredential)")
                return VerifiedIdStatus.NoStatusEndpoint
            }

        val descriptor = credential.raw.contents.vc.credentialStatus
            ?: run {
                SdkLog.i("$TAG result=NoStatusEndpoint (VC has no credentialStatus)")
                return VerifiedIdStatus.NoStatusEndpoint
            }

        SdkLog.i("$TAG starting status check: credentialStatus.type=${descriptor.type}")
        return fetchAndCheckStatusList(descriptor, credential.raw.contents.iss)
    }

    private suspend fun fetchAndCheckStatusList(descriptor: CredentialStatusDescriptor, issuerDid: String): VerifiedIdStatus {
        val url = resolveStatusListUrl(descriptor.effectiveStatusListCredential)
            ?: run {
                // DID-relative status list credential (any DID method, matching the Entra server's
                // isDidRelatedUrl) or the older urn:uuid: form — resolve via the issuer's IdentityHub.
                val statusCred = descriptor.effectiveStatusListCredential
                if ((statusCred.startsWith("did:") || descriptor.id.startsWith("urn:uuid:")) && issuerDid.isNotEmpty()) {
                    SdkLog.i("$TAG path=IdentityHub")
                    return checkStatusViaIdentityHub(descriptor, issuerDid)
                }
                SdkLog.w("$TAG result=Unknown (status list credential is neither a fetchable URL, a did: relative URL, nor a urn:uuid)")
                return VerifiedIdStatus.Unknown
            }

        SdkLog.i("$TAG path=DirectUrl")
        val response = apiProvider.statusListApi.getStatusListCredential(url).getOrElse {
            SdkLog.w("$TAG result=Unknown (status list fetch failed)", it)
            return VerifiedIdStatus.Unknown
        }
        if (response.status !in HTTP_SUCCESS_RANGE) {
            SdkLog.w("$TAG result=Unknown (status list fetch returned HTTP ${response.status})")
            return VerifiedIdStatus.Unknown
        }

        return try {
            val body = response.body.decodeToString()

            val (encodedList, statusPurpose) = extractStatusListInfo(body, issuerDid)
                ?: run {
                    SdkLog.w("$TAG result=Unknown (could not extract status list from response)")
                    return VerifiedIdStatus.Unknown
                }

            if (!statusPurposeMatches(descriptor, statusPurpose)) {
                SdkLog.w("$TAG result=Unknown (statusPurpose mismatch: credential=${descriptor.statusPurpose}, list=$statusPurpose)")
                return VerifiedIdStatus.Unknown
            }

            val decompressed = decodeAndDecompress(encodedList)
                ?: run {
                    SdkLog.w("$TAG result=Unknown (status list decode/decompress failed)")
                    return VerifiedIdStatus.Unknown
                }

            val isFlagged = checkBit(decompressed, descriptor.effectiveStatusListIndex)
                ?: run {
                    SdkLog.w("$TAG result=Unknown (status list index out of range)")
                    return VerifiedIdStatus.Unknown
                }

            val result = if (!isFlagged) {
                VerifiedIdStatus.Valid
            } else when (statusPurpose) {
                "suspension" -> VerifiedIdStatus.Suspended
                else -> VerifiedIdStatus.Revoked
            }
            SdkLog.i("$TAG result=$result (path=DirectUrl, statusPurpose=$statusPurpose)")
            result
        } catch (e: Exception) {
            SdkLog.w("$TAG result=Unknown (exception while checking status list)", e)
            VerifiedIdStatus.Unknown
        }
    }

    /**
     * Resolves the status list credential URL to a fetchable HTTPS URL.
     * Supports direct HTTPS URLs and `did:web:` DID URLs.
     *
     * For `did:web:domain?service=IdentityHub&queries=xxx`:
     *  1. Fetches the DID document at https://domain/.well-known/did.json
     *  2. Locates the named service endpoint
     *  3. Returns `<endpoint>?queries=xxx`
     */
    private suspend fun resolveStatusListUrl(url: String): String? {
        if (url.startsWith("https://")) return if (isWellFormedHttpsUrl(url)) url else null
        if (url.startsWith("did:web:")) return resolveDidWebUrl(url)
        return null
    }

    /** True if [url] parses as an https URL with a non-empty host. */
    private fun isWellFormedHttpsUrl(url: String): Boolean {
        return try {
            val uri = java.net.URI(url)
            uri.scheme.equals("https", ignoreCase = true) && !uri.host.isNullOrBlank()
        } catch (_: Exception) {
            false
        }
    }

    private suspend fun resolveDidWebUrl(didUrl: String): String? {
        val parsed = Uri.parse(didUrl)
        val did = didUrl.substringBefore('?')
        val serviceName = parsed.getQueryParameter("service") ?: "IdentityHub"
        val queries = parsed.getQueryParameter("queries")

        val didDocumentUrl = didWebToDocumentUrl(did) ?: return null

        val response = apiProvider.statusListApi.getStatusListCredential(didDocumentUrl).getOrElse {
            return null
        }
        if (response.status !in HTTP_SUCCESS_RANGE) return null

        return try {
            val body = response.body.decodeToString()
            val root = json.parseToJsonElement(body).jsonObject
            val services = root["service"]?.jsonArray ?: return null

            val serviceEndpoint = services.firstOrNull { serviceElement ->
                val svc = serviceElement.jsonObject
                val id = svc["id"]?.jsonPrimitive?.content ?: ""
                val type = svc["type"]?.jsonPrimitive?.content ?: ""
                // Match by exact service type or a fragment-anchored id (e.g. "did:web:...#IdentityHub").
                // The id fragment is anchored with '#' so an unrelated service whose id merely ends
                // with the same text (e.g. "#MyIdentityHub") is not mistakenly selected.
                type == serviceName || id.endsWith("#$serviceName")
            }?.jsonObject?.get("serviceEndpoint")?.let { endpoint ->
                // serviceEndpoint can be a plain string or an array of strings
                try { endpoint.jsonPrimitive.content } catch (_: Exception) {
                    try { endpoint.jsonArray.firstOrNull()?.jsonPrimitive?.content } catch (_: Exception) { null }
                }
            } ?: return null

            if (queries != null) {
                Uri.parse(serviceEndpoint).buildUpon()
                    .appendQueryParameter("queries", queries)
                    .build()
                    .toString()
            } else {
                serviceEndpoint
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun didWebToDocumentUrl(did: String): String? {
        if (!did.startsWith("did:web:")) return null

        // did:web is ':'-separated: the first element is the host (with an optional %3A-encoded
        // port); the rest are path segments. With no path segments the document lives at
        // /.well-known/did.json, otherwise at /<path>/did.json.
        val segments = did.removePrefix("did:web:").split(":").map { Uri.decode(it) }
        val host = segments.first()
        val pathSegments = segments.drop(1).ifEmpty { listOf(".well-known") }

        val builder = Uri.Builder().scheme("https").encodedAuthority(host)
        pathSegments.forEach { builder.appendPath(it) }
        builder.appendPath("did.json")
        return builder.build().toString()
    }

    /**
     * Extracts (encodedList, statusPurpose) from a status list credential, which MUST be a signed JWT.
     * Verifies the signature, binds the signer DID to [expectedIssuerDid], and enforces `exp` when
     * present. Unsigned bodies and any failed check yield null (treated as Unknown), mirroring the
     * Entra status service which never reads bits from unsigned data.
     */
    private suspend fun extractStatusListInfo(responseBody: String, expectedIssuerDid: String): Pair<String, String>? {
        return try {
            // Require a signed JWT; never read bits from unsigned bytes.
            val jwsToken = tryDeserializeJws(responseBody)
                ?: run {
                    SdkLog.w("$TAG status list is not a signed JWT; refusing to trust unsigned status data")
                    return null
                }
            // Verify signature.
            if (!jwtValidator.verifySignature(jwsToken)) {
                SdkLog.w("$TAG status list JWT signature verification failed")
                return null
            }
            // Signer DID must be the credential's issuer.
            if (expectedIssuerDid.isNotBlank() &&
                !jwtValidator.validateDidInHeaderAndPayload(jwsToken, expectedIssuerDid)) {
                SdkLog.w("$TAG status list JWT signer does not match credential issuer")
                return null
            }
            val jsonBody = jwsToken.content()
            val root = json.parseToJsonElement(jsonBody).jsonObject
            // Reject an expired (replayed) list; no exp = no constraint.
            val exp = root["exp"]?.jsonPrimitive?.longOrNull
            if (exp != null && exp + STATUS_LIST_CLOCK_SKEW_SECONDS < Date().time / 1000) {
                SdkLog.w("$TAG status list JWT is expired (exp=$exp); refusing to trust a possibly replayed list")
                return null
            }
            val subject = root["credentialSubject"]?.jsonObject
                ?: root["vc"]?.jsonObject?.get("credentialSubject")?.jsonObject
                ?: return null
            val encodedList = subject["encodedList"]?.jsonPrimitive?.content ?: return null
            val statusPurpose = subject["statusPurpose"]?.jsonPrimitive?.content ?: "revocation"
            Pair(encodedList, statusPurpose)
        } catch (e: Exception) {
            SdkLog.w("$TAG result=Unknown (failed to verify/parse status list response)", e)
            null
        }
    }

    /**
     * Attempts to parse [value] as a compact JWS using the JOSE library. Returns null when the
     * value is not a well-formed JWT (e.g. a plain JSON status list), avoiding brittle string checks.
     */
    private fun tryDeserializeJws(value: String): JwsToken? =
        try { JwsToken.deserialize(value) } catch (_: Exception) { null }

    /**
     * Base64url-decodes and GZIP-decompresses the StatusList2021 bitstring.
     */
    private fun decodeAndDecompress(encodedList: String): ByteArray? {
        return try {
            val decoded = Base64.decode(encodedList, Constants.BASE64_URL_SAFE)
            GZIPInputStream(ByteArrayInputStream(decoded)).readBytes()
        } catch (_: Exception) {
            null
        }
    }

    /** True if the credential's declared statusPurpose (if any) matches the list's; blank = no constraint. */
    private fun statusPurposeMatches(descriptor: CredentialStatusDescriptor, listStatusPurpose: String): Boolean {
        val declared = descriptor.statusPurpose
        return declared.isEmpty() || declared == listStatusPurpose
    }

    /**
     * Reads the bit at [index] using least-significant-bit-first ordering (index 0 = LSB of byte 0),
     * i.e. `1 shl (index % 8)`. This matches Entra's status list encoding; do not switch to
     * MSB-first, as that would break revocation detection for any index not a multiple of 8.
     *
     * Returns true if the bit is set (revoked/suspended), false if clear (valid), or null if [index]
     * is outside the bitstring (caller should treat as Unknown).
     */
    private fun checkBit(decompressed: ByteArray, index: Int): Boolean? {
        val byteIndex = index / 8
        val bitOffset = index % 8
        if (index < 0 || byteIndex >= decompressed.size) return null
        return (decompressed[byteIndex].toInt() and (1 shl bitOffset)) != 0
    }

    /**
     * Resolves a DID-relative status list credential via the issuer's IdentityHub. Handles both Entra
     * encodings: the older `urn:uuid:UUID?bit-index=N` id form, and the
     * `<issuerDid>?service=IdentityHub&queries=<base64url([{...,objectId}])>` statusListCredential form.
     * Flow:
     * 1. Resolve issuer DID document via the SDK's configured DID resolver.
     * 2. Find the `IdentityHub` service entry and take the first endpoint URL
     *    (the serializer normalises both `{"instances":[...]}` and `{"origins":[...]}` shapes).
     * 3. POST CollectionsQuery to that hub URL to get the StatusList2021 credential.
     * 4. Check the bit at [bitIndex].
     *
     * No hostnames are hardcoded here — every URL is sourced from either the VC itself
     * or the resolved DID document, with the resolver host controlled by the host app
     * via [com.microsoft.walletlibrary.did.sdk.VerifiableCredentialSdk.init]'s `resolverUrl` parameter.
     */
    private suspend fun checkStatusViaIdentityHub(descriptor: CredentialStatusDescriptor, issuerDid: String): VerifiedIdStatus {
        val objectId = resolveIdentityHubObjectId(descriptor)
            ?: run {
                SdkLog.w("$TAG result=Unknown (IdentityHub path: could not resolve status list object id)")
                return VerifiedIdStatus.Unknown
            }
        val bitIndex = resolveStatusListBitIndex(descriptor)

        // Resolve issuer DID document using the SDK's configured Resolver (no hardcoded host).
        val identifierDoc = com.microsoft.walletlibrary.did.sdk.VerifiableCredentialSdk
            .linkedDomainsService
            .resolveIdentifierDocument(issuerDid)
            .getOrElse {
                SdkLog.w("$TAG result=Unknown (IdentityHub path: DID document resolution failed)", it)
                return VerifiedIdStatus.Unknown
            }

        val hubUrl = identifierDoc.service
            .firstOrNull { it.type == "IdentityHub" }
            ?.serviceEndpoint
            ?.firstOrNull()
            ?: run {
                SdkLog.w("$TAG result=Unknown (IdentityHub path: no IdentityHub service endpoint in DID document)")
                return VerifiedIdStatus.Unknown
            }

        // POST CollectionsQuery to IdentityHub
        val requestBody = buildCollectionsQueryBody(issuerDid, objectId)
        val queryResponse = apiProvider.statusListApi.postCollectionsQuery(hubUrl, requestBody).getOrElse {
            SdkLog.w("$TAG result=Unknown (IdentityHub path: CollectionsQuery POST failed)", it)
            return VerifiedIdStatus.Unknown
        }
        if (queryResponse.status !in HTTP_SUCCESS_RANGE) {
            SdkLog.w("$TAG result=Unknown (IdentityHub path: CollectionsQuery returned HTTP ${queryResponse.status})")
            return VerifiedIdStatus.Unknown
        }

        return try {
            val responseBody = queryResponse.body.decodeToString()

            // Try direct parse first (in case response is the VC itself), then dig into envelope
            val statusListInfo = extractStatusListInfo(responseBody, issuerDid)
                ?: extractStatusListFromCollectionsResponse(responseBody, issuerDid)
                ?: run {
                    SdkLog.w("$TAG result=Unknown (IdentityHub path: could not extract status list from CollectionsQuery response)")
                    return VerifiedIdStatus.Unknown
                }

            val (encodedList, statusPurpose) = statusListInfo

            if (!statusPurposeMatches(descriptor, statusPurpose)) {
                SdkLog.w("$TAG result=Unknown (IdentityHub path: statusPurpose mismatch: credential=${descriptor.statusPurpose}, list=$statusPurpose)")
                return VerifiedIdStatus.Unknown
            }

            val decompressed = decodeAndDecompress(encodedList)
                ?: run {
                    SdkLog.w("$TAG result=Unknown (IdentityHub path: status list decode/decompress failed)")
                    return VerifiedIdStatus.Unknown
                }

            val isFlagged = checkBit(decompressed, bitIndex)
                ?: run {
                    SdkLog.w("$TAG result=Unknown (IdentityHub path: status list index out of range)")
                    return VerifiedIdStatus.Unknown
                }

            val result = if (!isFlagged) {
                VerifiedIdStatus.Valid
            } else when (statusPurpose) {
                "suspension" -> VerifiedIdStatus.Suspended
                else -> VerifiedIdStatus.Revoked
            }
            SdkLog.i("$TAG result=$result (path=IdentityHub, statusPurpose=$statusPurpose)")
            result
        } catch (e: Exception) {
            SdkLog.w("$TAG result=Unknown (IdentityHub path: exception while checking status list)", e)
            VerifiedIdStatus.Unknown
        }
    }

    /**
     * Resolves the IdentityHub object id (status list UUID) from a credentialStatus, supporting both
     * the `urn:uuid:<objectId>` id form and the `did:...?service=IdentityHub&queries=<base64url>` form.
     */
    private fun resolveIdentityHubObjectId(descriptor: CredentialStatusDescriptor): String? {
        // id form: "urn:uuid:<objectId>?bit-index=N" — objectId is the identifier body before any query.
        if (descriptor.id.startsWith("urn:uuid:")) {
            return descriptor.id.removePrefix("urn:uuid:").substringBefore('?')
        }
        // did-relative form: "<issuerDid>?service=IdentityHub&queries=<base64url([{...,objectId}])>".
        val statusCred = descriptor.effectiveStatusListCredential
        if (statusCred.startsWith("did:")) {
            val encodedQueries = Uri.parse(statusCred).getQueryParameter("queries") ?: return null
            return try {
                val decoded = Base64.decode(encodedQueries, Constants.BASE64_URL_SAFE).decodeToString()
                json.parseToJsonElement(decoded).jsonArray
                    .firstOrNull()?.jsonObject?.get("objectId")?.jsonPrimitive?.content
            } catch (_: Exception) {
                null
            }
        }
        return null
    }

    /** Bit index from the `urn:uuid:...?bit-index=N` id when present, else the descriptor's index. */
    private fun resolveStatusListBitIndex(descriptor: CredentialStatusDescriptor): Int {
        if (descriptor.id.startsWith("urn:uuid:")) {
            Uri.parse(descriptor.id).getQueryParameter("bit-index")?.toIntOrNull()?.let { return it }
        }
        return descriptor.effectiveStatusListIndex
    }

    private fun buildCollectionsQueryBody(issuerDid: String, objectId: String): String {
        val requestId = java.util.UUID.randomUUID().toString()
        val body = buildJsonObject {
            put("requestId", requestId)
            put("target", issuerDid)
            putJsonArray("messages") {
                addJsonObject {
                    putJsonObject("descriptor") {
                        put("method", "CollectionsQuery")
                        put("objectId", objectId)
                        put("schema", "https://w3id.org/vc-status-list-2021/v1")
                    }
                }
            }
        }
        return json.encodeToString(JsonObject.serializer(), body)
    }

    /**
     * Extracts the StatusList2021 (encodedList, statusPurpose) from an IdentityHub CollectionsQuery
     * response envelope. Digs into `replies[].entries[].data` entries.
     *
     * In Entra IdentityHub responses, `data` is a base64(url)-encoded JWT containing the VC.
     * We base64-decode it first to recover the JWT, then hand it to [extractStatusListInfo].
     * The raw string is also tried as a last-resort fallback in case a hub ever returns
     * the JWT or JSON directly.
     */
    private suspend fun extractStatusListFromCollectionsResponse(responseBody: String, expectedIssuerDid: String): Pair<String, String>? {
        return try {
            val root = json.parseToJsonElement(responseBody).jsonObject
            val replies = root["replies"]?.jsonArray ?: return null
            for (reply in replies) {
                val entries = reply.jsonObject["entries"]?.jsonArray ?: continue
                for (entry in entries) {
                    val data = entry.jsonObject["data"]?.jsonPrimitive?.content ?: continue
                    val decoded = base64DecodeToString(data)
                    if (decoded != null) {
                        val result = extractStatusListInfo(decoded, expectedIssuerDid)
                        if (result != null) return result
                    }
                    // Fallback: in case `data` is already a JWT or JSON.
                    val result = extractStatusListInfo(data, expectedIssuerDid)
                    if (result != null) return result
                }
            }
            null
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Base64url-decodes the IdentityHub `data` field to a UTF-8 string.
     *
     * IdentityHub entries are base64url-encoded (matching the status service, which encodes with
     * base64url), so a single URL-safe decode is used. Returns null if decoding fails or the result
     * does not look like a JWT (`eyJ`) or JSON (`{`).
     */
    private fun base64DecodeToString(encoded: String): String? {
        return try {
            val text = Base64.decode(encoded, Constants.BASE64_URL_SAFE).decodeToString()
            if (text.startsWith("eyJ") || text.trimStart().startsWith("{")) text else null
        } catch (_: Exception) {
            null
        }
    }
}
