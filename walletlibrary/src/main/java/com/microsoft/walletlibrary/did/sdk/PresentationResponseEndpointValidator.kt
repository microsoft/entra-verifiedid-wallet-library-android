package com.microsoft.walletlibrary.did.sdk

import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainResult
import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainVerified
import com.microsoft.walletlibrary.did.sdk.util.controlflow.PresentationException
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

internal object PresentationResponseEndpointValidator {
    fun validate(redirectUrl: String, linkedDomainResult: LinkedDomainResult): String {
        val verifiedLinkedDomain = linkedDomainResult as? LinkedDomainVerified
            ?: throw PresentationException("Presentation response destination is not linked to a verified domain.")
        val redirectEndpoint = parseHttpsUrl(redirectUrl)
        val linkedDomainValue = verifiedLinkedDomain.origin
            ?: verifiedLinkedDomain.validatedOrigin
            ?: verifiedLinkedDomain.domainUrl
        val linkedDomainEndpoint = parseHttpsUrl(
            if (linkedDomainValue.contains("://")) linkedDomainValue else "https://$linkedDomainValue"
        )

        if (!hasMatchingDestination(redirectEndpoint, linkedDomainEndpoint)) {
            throw PresentationException("Presentation response destination does not match the verified domain.")
        }
        return redirectEndpoint.toString()
    }

    private fun hasMatchingDestination(
        redirectEndpoint: HttpUrl,
        linkedDomainEndpoint: HttpUrl
    ): Boolean {
        val redirectHost = redirectEndpoint.host.trimEnd('.')
        val linkedDomainHost = linkedDomainEndpoint.host.trimEnd('.')
        val hasMatchingHost = redirectHost == linkedDomainHost ||
            (!isIpLiteral(linkedDomainHost) && redirectHost.endsWith(".$linkedDomainHost"))
        return hasMatchingHost && redirectEndpoint.port == linkedDomainEndpoint.port
    }

    private fun parseHttpsUrl(value: String): HttpUrl {
        val url = value.toHttpUrlOrNull()
        if (url == null ||
            !url.isHttps ||
            url.username.isNotEmpty() ||
            url.password.isNotEmpty() ||
            url.fragment != null
        ) {
            throw PresentationException("Presentation response destination must be an absolute HTTPS URL.")
        }
        return url
    }

    private fun isIpLiteral(host: String): Boolean =
        host.contains(":") || host.matches(Regex("^[0-9.]+$"))
}
