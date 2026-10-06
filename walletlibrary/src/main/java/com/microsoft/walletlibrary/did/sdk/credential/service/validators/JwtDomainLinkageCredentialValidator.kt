// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.did.sdk.credential.service.validators

import com.microsoft.walletlibrary.did.sdk.canonicalizeLinkedDomainOrigin
import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.DomainLinkageCredential
import com.microsoft.walletlibrary.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.walletlibrary.did.sdk.util.log.SdkLog
import kotlinx.serialization.json.Json
import java.net.URI
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
internal class JwtDomainLinkageCredentialValidator @Inject constructor(
    private val jwtValidator: JwtValidator,
    private val serializer: Json,
    @Named("linkedDomainValidationHardeningEnabled")
    private val linkedDomainValidationHardeningEnabled: Boolean = false
) : DomainLinkageCredentialValidator {

    override suspend fun validate(
        domainLinkageCredential: String,
        rpDid: String,
        rpDomain: String
    ): DomainLinkageCredentialValidationResult {
        val jwt: JwsToken
        val parsedCredential: DomainLinkageCredential
        try {
            jwt = JwsToken.deserialize(domainLinkageCredential)
            parsedCredential = serializer.decodeFromString(
                DomainLinkageCredential.serializer(),
                jwt.content()
            )
        } catch (ex: Exception) {
            SdkLog.w("Unable to parse linked-domain credential.", ex)
            return DomainLinkageCredentialValidationResult.CLAIMS_INVALID
        }
        if (!jwtValidator.verifySignature(jwt)) {
            return DomainLinkageCredentialValidationResult.SIGNATURE_INVALID
        }
        if (!jwtValidator.validateDidInHeaderAndPayload(jwt, parsedCredential.issuer) ||
            !isCredentialSubjectIdValid(parsedCredential, rpDid)
        ) {
            return DomainLinkageCredentialValidationResult.DID_MISMATCH
        }
        if (!isCredentialSubjectOriginValid(parsedCredential, rpDomain)) {
            SdkLog.w(
                "Linked-domain credential origin mismatch. " +
                    "Expected origin='${originForLogging(rpDomain)}', " +
                    "observed origin='${originForLogging(parsedCredential.vc.credentialSubject.domainUrl)}', " +
                    "mismatch components='${originMismatchComponents(rpDomain, parsedCredential.vc.credentialSubject.domainUrl)}'."
            )
            return DomainLinkageCredentialValidationResult.ORIGIN_MISMATCH
        }
        return DomainLinkageCredentialValidationResult.VALID
    }

    private fun isCredentialSubjectIdValid(domainLinkageCredential: DomainLinkageCredential, rpDid: String): Boolean {
        return domainLinkageCredential.subject == domainLinkageCredential.vc.credentialSubject.did
            && domainLinkageCredential.issuer == domainLinkageCredential.vc.credentialSubject.did
            && domainLinkageCredential.vc.credentialSubject.did == rpDid
    }

    private fun isCredentialSubjectOriginValid(domainLinkageCredential: DomainLinkageCredential, rpDomain: String): Boolean {
        if (!linkedDomainValidationHardeningEnabled) {
            return domainLinkageCredential.vc.credentialSubject.domainUrl.equals(rpDomain, true)
        }
        val expectedOrigin = runCatching { canonicalizeLinkedDomainOrigin(rpDomain) }.getOrNull()
            ?: return false
        val observedOrigin = runCatching {
            canonicalizeLinkedDomainOrigin(domainLinkageCredential.vc.credentialSubject.domainUrl)
        }.getOrNull() ?: return false
        return expectedOrigin == observedOrigin
    }

    private fun originForLogging(origin: String): String {
        val parsedOrigin = parseOrigin(origin) ?: return INVALID_ORIGIN
        val port = parsedOrigin.port.takeIf { it >= 0 }?.let { ":$it" }.orEmpty()
        return "${parsedOrigin.scheme}://${parsedOrigin.host}$port"
    }

    private fun originMismatchComponents(expected: String, observed: String): String {
        val expectedOrigin = parseOrigin(expected) ?: return INVALID_ORIGIN_COMPONENT
        val observedOrigin = parseOrigin(observed) ?: return INVALID_ORIGIN_COMPONENT
        val components = mutableListOf<String>()
        if (expectedOrigin.scheme != observedOrigin.scheme) components.add(SCHEME_COMPONENT)
        if (expectedOrigin.host != observedOrigin.host) components.add(HOST_COMPONENT)
        if (expectedOrigin.port != observedOrigin.port) components.add(PORT_COMPONENT)
        return components.joinToString(",").ifEmpty { NON_ORIGIN_COMPONENTS_OR_FORMAT }
    }

    private fun parseOrigin(origin: String): LoggableOrigin? {
        val uri = runCatching { URI(origin) }.getOrNull() ?: return null
        val scheme = uri.scheme?.lowercase() ?: return null
        val host = uri.host?.lowercase() ?: return null
        return LoggableOrigin(scheme, host, uri.port)
    }

    private companion object {
        const val INVALID_ORIGIN = "<invalid-origin>"
        const val INVALID_ORIGIN_COMPONENT = "invalid_origin"
        const val SCHEME_COMPONENT = "scheme"
        const val HOST_COMPONENT = "host"
        const val PORT_COMPONENT = "port"
        const val NON_ORIGIN_COMPONENTS_OR_FORMAT = "non_origin_components_or_format"
    }

    private data class LoggableOrigin(val scheme: String, val host: String, val port: Int)
}