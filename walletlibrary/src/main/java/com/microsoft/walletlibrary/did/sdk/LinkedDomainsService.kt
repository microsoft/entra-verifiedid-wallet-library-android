// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.did.sdk

import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainMissing
import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainResult
import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainUnVerified
import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainVerified
import com.microsoft.walletlibrary.did.sdk.credential.service.validators.DomainLinkageCredentialValidator
import com.microsoft.walletlibrary.did.sdk.datasource.network.apis.HttpAgentApiProvider
import com.microsoft.walletlibrary.did.sdk.datasource.network.linkedDomainsOperations.FetchWellKnownConfigDocumentNetworkOperation
import com.microsoft.walletlibrary.did.sdk.identifier.resolvers.Resolver
import com.microsoft.walletlibrary.did.sdk.identifier.resolvers.RootOfTrustResolver
import com.microsoft.walletlibrary.did.sdk.util.Constants
import com.microsoft.walletlibrary.did.sdk.util.controlflow.SdkException
import com.microsoft.walletlibrary.did.sdk.util.log.SdkLog
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class LinkedDomainsService @Inject constructor(
    private val apiProvider: HttpAgentApiProvider,
    private val resolver: Resolver,
    private val jwtDomainLinkageCredentialValidator: DomainLinkageCredentialValidator
) {

    suspend fun fetchAndVerifyLinkedDomains(
        relyingPartyDid: String,
        rootOfTrustResolver: RootOfTrustResolver? = null
    ): Result<LinkedDomainResult> {
        return try {
            val verifiedDomains = verifyLinkedDomainsUsingResolver(relyingPartyDid, rootOfTrustResolver)
            Result.success(verifiedDomains)
        } catch (ex: SdkException) {
            SdkLog.i(
                "Linked Domains verification using resolver failed with exception $ex. " +
                    "Verifying it using Well Known Document."
            )
            verifyLinkedDomainsUsingWellKnownDocument(relyingPartyDid)
        }
    }

    private suspend fun verifyLinkedDomainsUsingResolver(
        relyingPartyDid: String,
        rootOfTrustResolver: RootOfTrustResolver?
    ): LinkedDomainResult {
        rootOfTrustResolver ?: throw SdkException("Root of trust resolver is not configured")
        val linkedDomainResult = rootOfTrustResolver.resolve(relyingPartyDid)
        if (linkedDomainResult is LinkedDomainVerified) return linkedDomainResult
        else throw SdkException("Root of trust resolver did not return a verified domain")
    }

    private suspend fun verifyLinkedDomainsUsingWellKnownDocument(relyingPartyDid: String): Result<LinkedDomainResult> {
        return getLinkedDomainsFromDid(relyingPartyDid)
            .map{
                domainUrls ->
                verifyLinkedDomains(domainUrls, relyingPartyDid)
            }
    }

    private suspend fun verifyLinkedDomains(domainUrls: List<String>, relyingPartyDid: String): LinkedDomainResult {
        if (domainUrls.isEmpty())
            return LinkedDomainMissing
        val domainUrl = domainUrls.first()
        val hostname = URL(domainUrl).host
        return getWellKnownConfigDocument(domainUrl)
            .map {
                wellKnownConfigDocument ->
                wellKnownConfigDocument.linkedDids.firstNotNullOf { linkedDidJwt ->
                    val isDomainLinked = jwtDomainLinkageCredentialValidator.validate(linkedDidJwt, relyingPartyDid, domainUrl)
                    if (isDomainLinked)
                        LinkedDomainVerified(hostname)
                    else
                        null
                }
            }.onFailure {
                SdkLog.d("Unable to fetch well-known config document from $domainUrl")
            }.getOrDefault(LinkedDomainUnVerified(hostname))
    }

    private suspend fun getLinkedDomainsFromDid(relyingPartyDid: String): Result<List<String>> {
        val didDocumentResult = resolver.resolve(relyingPartyDid)
        return didDocumentResult.map { didDocument ->
            val linkedDomainsServices =
                didDocument.service.filter { service -> service.type.equals(Constants.LINKED_DOMAINS_SERVICE_ENDPOINT_TYPE, true) }
            linkedDomainsServices.map { it.serviceEndpoint }.flatten()
        }
    }

    private suspend fun getWellKnownConfigDocument(domainUrl: String) = FetchWellKnownConfigDocumentNetworkOperation(
        domainUrl,
        apiProvider
    ).fire()
}