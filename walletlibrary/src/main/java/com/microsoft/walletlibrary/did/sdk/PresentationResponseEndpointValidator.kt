package com.microsoft.walletlibrary.did.sdk

import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainResult
import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainVerified
import com.microsoft.walletlibrary.did.sdk.util.controlflow.PresentationException
import com.microsoft.walletlibrary.util.http.httpagent.PublicNetworkAddressValidator
import java.net.IDN
import java.net.InetAddress
import java.net.URI
import java.util.Locale

internal object PresentationResponseEndpointValidator {
    fun validate(redirectUrl: String, linkedDomainResult: LinkedDomainResult): String {
        val verifiedLinkedDomain = linkedDomainResult as? LinkedDomainVerified
            ?: throw PresentationException("Presentation response destination is not linked to a verified domain.")
        val redirectUri = parseHttpsUri(redirectUrl, allowPathAndQuery = true)
        val linkedDomainValue = verifiedLinkedDomain.origin ?: verifiedLinkedDomain.domainUrl
        val linkedDomainUri = parseHttpsUri(
            if (linkedDomainValue.contains("://")) linkedDomainValue else "https://$linkedDomainValue",
            allowPathAndQuery = true
        )

        if (!hasMatchingDestination(redirectUri, linkedDomainUri)) {
            throw PresentationException("Presentation response destination does not match the verified domain.")
        }
        rejectNonPublicIpLiteral(redirectUri.host)
        return redirectUri.toASCIIString()
    }

    private fun hasMatchingDestination(redirectUri: URI, linkedDomainUri: URI): Boolean {
        val redirectHost = canonicalHost(redirectUri)
        val linkedDomainHost = canonicalHost(linkedDomainUri)
        val hasMatchingHost = redirectHost == linkedDomainHost ||
            (!isIpLiteral(linkedDomainHost) && redirectHost.endsWith(".$linkedDomainHost"))
        return hasMatchingHost && effectivePort(redirectUri) == effectivePort(linkedDomainUri)
    }

    private fun parseHttpsUri(value: String, allowPathAndQuery: Boolean): URI {
        val uri = try {
            URI(value)
        } catch (exception: Exception) {
            throw PresentationException("Presentation response destination is invalid.", exception)
        }
        if (!uri.isAbsolute ||
            !uri.scheme.equals("https", ignoreCase = true) ||
            uri.rawAuthority.isNullOrBlank() ||
            uri.rawUserInfo != null ||
            uri.host.isNullOrBlank() ||
            uri.rawFragment != null ||
            uri.port < -1 ||
            uri.port > 65535 ||
            uri.port == 0 ||
            (!allowPathAndQuery &&
                (!uri.rawPath.isNullOrEmpty() && uri.rawPath != "/" || uri.rawQuery != null))
        ) {
            throw PresentationException("Presentation response destination must be an absolute HTTPS URL.")
        }
        return uri
    }

    private fun canonicalHost(uri: URI): String =
        uri.host
            .trimEnd('.')
            .lowercase(Locale.ROOT)
            .let { host -> if (host.contains(":")) host else IDN.toASCII(host) }

    private fun effectivePort(uri: URI): Int = if (uri.port == -1) 443 else uri.port

    private fun isIpLiteral(host: String): Boolean =
        host.contains(":") || host.matches(Regex("^[0-9.]+$"))

    private fun rejectNonPublicIpLiteral(host: String) {
        if (!isIpLiteral(host)) {
            return
        }
        val address = try {
            InetAddress.getByName(host)
        } catch (exception: Exception) {
            throw PresentationException("Presentation response destination contains an invalid IP address.", exception)
        }
        if (!PublicNetworkAddressValidator.isPublic(address)) {
            throw PresentationException("Presentation response destination must use a public address.")
        }
    }
}
