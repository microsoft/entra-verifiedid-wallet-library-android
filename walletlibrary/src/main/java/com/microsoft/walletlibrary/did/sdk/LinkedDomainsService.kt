// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.did.sdk

import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainMissing
import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainResult
import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainUnVerified
import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainVerified
import com.microsoft.walletlibrary.did.sdk.credential.service.validators.DomainLinkageCredentialValidator
import com.microsoft.walletlibrary.did.sdk.datasource.network.apis.ApiProvider
import com.microsoft.walletlibrary.did.sdk.datasource.network.linkedDomainsOperations.FetchWellKnownConfigDocumentNetworkOperation
import com.microsoft.walletlibrary.did.sdk.identifier.resolvers.Resolver
import com.microsoft.walletlibrary.did.sdk.util.Constants
import com.microsoft.walletlibrary.did.sdk.util.controlflow.Result
import com.microsoft.walletlibrary.did.sdk.util.controlflow.map
import com.microsoft.walletlibrary.did.sdk.util.controlflow.runResultTry
import com.microsoft.walletlibrary.did.sdk.util.log.SdkLog
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class LinkedDomainsService @Inject constructor(
    private val apiProvider: ApiProvider,
    private val resolver: Resolver,
    private val jwtDomainLinkageCredentialValidator: DomainLinkageCredentialValidator
) {
    suspend fun fetchAndVerifyLinkedDomains(relyingPartyDid: String): Result<LinkedDomainResult> {
        return runResultTry {
            val domainUrls = getLinkedDomainsFromDid(relyingPartyDid).abortOnError()
            verifyLinkedDomains(domainUrls, relyingPartyDid)
        }
    }

    private suspend fun verifyLinkedDomains(domainUrls: List<String>, relyingPartyDid: String): Result<LinkedDomainResult> {
        return runResultTry {
            if (domainUrls.isEmpty())
                return@runResultTry Result.Success(LinkedDomainMissing)
            val domainUrl = domainUrls.first()
            val hostname = URL(domainUrl).host
            val wellKnownConfigDocumentResult = getWellKnownConfigDocument(domainUrl)
            if (wellKnownConfigDocumentResult is Result.Success) {
                val wellKnownConfigDocument = wellKnownConfigDocumentResult.payload
                wellKnownConfigDocument.linkedDids.forEach { linkedDidJwt ->
                    val isDomainLinked = jwtDomainLinkageCredentialValidator.validate(linkedDidJwt, relyingPartyDid, domainUrl)
                    if (isDomainLinked) return@runResultTry Result.Success(LinkedDomainVerified(hostname))
                }
            } else SdkLog.d("Unable to fetch well-known config document from $domainUrl")
            Result.Success(LinkedDomainUnVerified(hostname))
        }
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