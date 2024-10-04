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
import com.microsoft.walletlibrary.util.IdentifierDocumentResolutionException
import java.net.URL
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
internal class LinkedDomainsService @Inject constructor(
    private val apiProvider: HttpAgentApiProvider,
    private val resolver: Resolver,
    private val jwtDomainLinkageCredentialValidator: DomainLinkageCredentialValidator,
    @Named("rootOfTrustResolver") private val rootOfTrustResolver: RootOfTrustResolver? = null
) {
    internal suspend fun resolveIdentifierDocument(relyingPartyDid: String): Result<IdentifierDocument> {
        return resolver.resolve(relyingPartyDid)
    }

    suspend fun validateLinkedDomains(identifierDocument: IdentifierDocument): Result<LinkedDomainResult> {
        return try {
            rootOfTrustResolver?.resolve(identifierDocument)
                ?.let { Result.success(it.toLinkedDomainResult()) }
                ?: throw SdkException("Root of trust resolver is not configured")
        } catch (ex: SdkException) {
            SdkLog.i(
                "Linked Domains verification using resolver failed with exception $ex. " +
                        "Verifying it using Well Known Document.",
                ex
            )
            val linkedDomains = verifyLinkedDomainsUsingWellKnownDocument(identifierDocument)
            Result.success(linkedDomains)
        }
    }

    suspend fun fetchDocumentAndVerifyLinkedDomains(relyingPartyDid: String): Result<LinkedDomainResult> {
        resolveIdentifierDocument(relyingPartyDid)
            .onSuccess { return validateLinkedDomains(it) }
            .onFailure { return Result.failure(it) }
        return Result.failure(IdentifierDocumentResolutionException("Failed to resolve identifier document"))
    }

    private suspend fun verifyLinkedDomainsUsingWellKnownDocument(identifierDocument: IdentifierDocument): LinkedDomainResult {
        val linkedDomains = getLinkedDomainsFromDidDocument(identifierDocument)
        verifyLinkedDomains(linkedDomains, identifierDocument.id)
            .onSuccess { return it }
            .onFailure { throw it }
        return LinkedDomainMissing
    }

    private suspend fun verifyLinkedDomains(
        domainUrls: List<String>,
        relyingPartyDid: String
    ): Result<LinkedDomainResult> {
        if (domainUrls.isEmpty())
            return Result.success(LinkedDomainMissing)
        val domainUrl = domainUrls.first()
        val hostname = URL(domainUrl).host
        getWellKnownConfigDocument(domainUrl)
            .onSuccess { wellKnownConfigDocument ->
                wellKnownConfigDocument.linkedDids.firstNotNullOf { linkedDidJwt ->
                    val isDomainLinked = jwtDomainLinkageCredentialValidator.validate(
                        linkedDidJwt,
                        relyingPartyDid,
                        domainUrl
                    )
                    return if (isDomainLinked)
                        Result.success(LinkedDomainVerified(hostname))
                    else
                        Result.success(LinkedDomainUnVerified(hostname))
                }
            }
            .onFailure {
                SdkLog.i("Unable to fetch well-known config document from $domainUrl because of ${it.message}")
                return Result.success(LinkedDomainUnVerified(hostname))
            }
        return Result.success(LinkedDomainMissing)
    }

    private fun getLinkedDomainsFromDidDocument(identifierDocument: IdentifierDocument): List<String> {
        val linkedDomainsServices =
            identifierDocument.service.filter { service ->
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