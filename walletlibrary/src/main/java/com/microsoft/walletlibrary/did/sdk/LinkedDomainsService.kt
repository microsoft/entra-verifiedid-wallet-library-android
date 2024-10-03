// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.did.sdk

import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainMissing
import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainResult
import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainUnVerified
import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainVerified
import com.microsoft.walletlibrary.did.sdk.credential.service.validators.DomainLinkageCredentialValidator
import com.microsoft.walletlibrary.did.sdk.datasource.network.apis.HttpAgentApiProvider
import com.microsoft.walletlibrary.did.sdk.datasource.network.linkedDomainsOperations.FetchWellKnownConfigDocumentNetworkOperation
import com.microsoft.walletlibrary.did.sdk.identifier.models.identifierdocument.IdentifierDocument
import com.microsoft.walletlibrary.did.sdk.identifier.resolvers.Resolver
import com.microsoft.walletlibrary.did.sdk.identifier.resolvers.RootOfTrustResolver
import com.microsoft.walletlibrary.did.sdk.util.Constants
import com.microsoft.walletlibrary.did.sdk.util.controlflow.SdkException
import com.microsoft.walletlibrary.did.sdk.util.log.SdkLog
import com.microsoft.walletlibrary.mappings.toLinkedDomainResult
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class LinkedDomainsService @Inject constructor(
    private val apiProvider: HttpAgentApiProvider,
    private val resolver: Resolver,
    private val jwtDomainLinkageCredentialValidator: DomainLinkageCredentialValidator,
    private val rootOfTrustResolver: RootOfTrustResolver? = null
) {
    suspend fun fetchAndVerifyLinkedDomains(relyingPartyDid: String): Result<LinkedDomainResult> {
        return try {
            val verifiedDomains = verifyLinkedDomainsUsingResolver(relyingPartyDid)
            Result.success(verifiedDomains)
        } catch (ex: SdkException) {
            SdkLog.i(
                "Linked Domains verification using resolver failed with exception $ex. " +
                    "Verifying it using Well Known Document.",
                ex
            )
            verifyLinkedDomainsUsingWellKnownDocument(relyingPartyDid)
        }
/*        getLinkedDomainsFromDid(relyingPartyDid)
            .onSuccess { domainUrls ->
                return verifyLinkedDomains(domainUrls, relyingPartyDid)
            }
            .onFailure {
                return Result.failure(it)
            }
        return Result.failure(SdkException("Failed while verifying linked domains"))*/
    }

    private suspend fun verifyLinkedDomainsUsingResolver(relyingPartyDid: String): LinkedDomainResult {
        rootOfTrustResolver ?: throw SdkException("Root of trust resolver is not configured")
        val linkedDomainResult = rootOfTrustResolver.resolve(relyingPartyDid).toLinkedDomainResult()
        if (linkedDomainResult is LinkedDomainVerified) return linkedDomainResult
        else throw SdkException("Root of trust resolver did not return a verified domain")
    }

    private suspend fun verifyLinkedDomainsUsingWellKnownDocument(relyingPartyDid: String): Result<LinkedDomainResult> {
        return getLinkedDomainsFromDid(relyingPartyDid).map {
            verifyLinkedDomains(it, relyingPartyDid)
        }.getOrThrow()
    }

    internal suspend fun verifyLinkedDomains(
        domainUrls: List<String>,
        relyingPartyDid: String
    ): Result<LinkedDomainResult> {
        if (domainUrls.isEmpty())
            return Result.success(LinkedDomainMissing)
        val domainUrl = domainUrls.first()
        val hostname = URL(domainUrl).host
        return getWellKnownConfigDocument(domainUrl)
            .map { wellKnownConfigDocument ->
                wellKnownConfigDocument.linkedDids.firstNotNullOf { linkedDidJwt ->
                    val isDomainLinked = jwtDomainLinkageCredentialValidator.validate(
                        linkedDidJwt,
                        relyingPartyDid,
                        domainUrl
                    )
                    if (isDomainLinked)
                        LinkedDomainVerified(hostname)
                    else
                        null
                }
            }.onFailure {
                SdkLog.d("Unable to fetch well-known config document from $domainUrl")
            }.recover {
                LinkedDomainUnVerified(hostname)
            }
    }

    private suspend fun getLinkedDomainsFromDid(relyingPartyDid: String): Result<List<String>> {
        val didDocumentResult = resolveIdentifierDocument(relyingPartyDid)
        return didDocumentResult.map { didDocument ->
            getLinkedDomainsFromDidDocument(didDocument)
        }
    }

    internal suspend fun resolveIdentifierDocument(relyingPartyDid: String): Result<IdentifierDocument> {
        return resolver.resolve(relyingPartyDid)
    }

    internal fun getLinkedDomainsFromDidDocument(didDocument: IdentifierDocument): List<String> {
        val linkedDomainsServices =
            didDocument.service.filter { service ->
                service.type.equals(
                    Constants.LINKED_DOMAINS_SERVICE_ENDPOINT_TYPE,
                    true
                )
            }
        return linkedDomainsServices.map { it.serviceEndpoint }.flatten()
    }

    private suspend fun getWellKnownConfigDocument(domainUrl: String) =
        FetchWellKnownConfigDocumentNetworkOperation(
            domainUrl,
            apiProvider
        ).fire()
}