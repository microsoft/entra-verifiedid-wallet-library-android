package com.microsoft.walletlibrary.did.sdk

import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainResult
import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainVerified
import com.microsoft.walletlibrary.did.sdk.util.controlflow.PresentationException
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

internal object PresentationResponseEndpointValidator {
    fun validate(redirectUrl: String, linkedDomainResult: LinkedDomainResult): String {
        if (linkedDomainResult !is LinkedDomainVerified) {
            throw PresentationException("Presentation response destination is not linked to a verified domain.")
        }
        val redirectEndpoint = parseHttpsUrl(redirectUrl)
        return redirectEndpoint.toString()
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
}
