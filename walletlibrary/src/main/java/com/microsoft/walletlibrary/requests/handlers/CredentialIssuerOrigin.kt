package com.microsoft.walletlibrary.requests.handlers

import java.net.IDN
import java.net.URI
import java.util.Locale

internal object CredentialIssuerOrigin {
    fun fromCredentialIssuer(credentialIssuer: String): String {
        val uri = parseHttpsUri(credentialIssuer)
        if (uri.rawQuery != null || uri.rawFragment != null) {
            throw IllegalArgumentException("Credential issuer must not contain a query or fragment.")
        }
        return canonicalOrigin(uri)
    }

    fun fromLinkedDomain(linkedDomain: String): String {
        val uri = parseHttpsUri(
            if (linkedDomain.contains("://")) linkedDomain else "https://$linkedDomain"
        )
        return canonicalOrigin(uri)
    }

    private fun parseHttpsUri(value: String): URI {
        val uri = URI(value)
        if (!uri.isAbsolute ||
            !uri.scheme.equals("https", ignoreCase = true) ||
            uri.rawAuthority.isNullOrBlank() ||
            uri.rawUserInfo != null ||
            uri.host.isNullOrBlank() ||
            (uri.port != -1 && uri.port !in 1..65535)
        ) {
            throw IllegalArgumentException("Value must be an absolute HTTPS URL.")
        }
        return uri
    }

    private fun canonicalOrigin(uri: URI): String {
        val host = uri.host.lowercase(Locale.ROOT)
        val canonicalHost = if (host.contains(":")) {
            "[${host.trim('[', ']')}]"
        } else {
            IDN.toASCII(host)
        }
        val port = if (uri.port == 443) -1 else uri.port
        return "https://$canonicalHost${if (port == -1) "" else ":$port"}"
    }
}
