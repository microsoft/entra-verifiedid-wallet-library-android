// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.did.sdk

import com.microsoft.walletlibrary.WalletLibraryFlight
import com.microsoft.walletlibrary.WalletLibraryFlightProvider
import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainMissing
import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainResult
import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainUnVerified
import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainVerified
import com.microsoft.walletlibrary.did.sdk.credential.service.validators.DomainLinkageCredentialValidator
import com.microsoft.walletlibrary.did.sdk.credential.service.validators.DomainLinkageCredentialValidationResult
import com.microsoft.walletlibrary.did.sdk.datasource.network.apis.HttpAgentApiProvider
import com.microsoft.walletlibrary.did.sdk.datasource.network.linkedDomainsOperations.FetchWellKnownConfigDocumentNetworkOperation
import com.microsoft.walletlibrary.did.sdk.identifier.models.identifierdocument.IdentifierDocument
import com.microsoft.walletlibrary.did.sdk.identifier.resolvers.Resolver
import com.microsoft.walletlibrary.did.sdk.identifier.resolvers.RootOfTrustResolver
import com.microsoft.walletlibrary.did.sdk.util.Constants
import com.microsoft.walletlibrary.did.sdk.util.controlflow.SdkException
import com.microsoft.walletlibrary.did.sdk.util.log.SdkLog
import com.microsoft.walletlibrary.mappings.toLinkedDomainResult
import java.net.URI
import java.net.URL
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
internal class LinkedDomainsService @Inject constructor(
    private val apiProvider: HttpAgentApiProvider,
    private val resolver: Resolver,
    private val jwtDomainLinkageCredentialValidator: DomainLinkageCredentialValidator,
    @Named("rootOfTrustResolver") private val rootOfTrustResolver: RootOfTrustResolver? = null,
    private val flightProvider: WalletLibraryFlightProvider = WalletLibraryFlightProvider { false }
) {
    internal suspend fun resolveIdentifierDocument(relyingPartyDid: String): Result<IdentifierDocument> {
        return resolver.resolve(relyingPartyDid)
    }

    suspend fun validateLinkedDomains(
        identifierDocument: IdentifierDocument,
        flow: LinkedDomainValidationFlow = LinkedDomainValidationFlow.UNKNOWN
    ): Result<LinkedDomainResult> {
        val linkedDomainValidationHardeningEnabled =
            flightProvider.isEnabled(WalletLibraryFlight.LinkedDomainValidationHardening)
        val validationAttempt = try {
            rootOfTrustResolver?.resolve(identifierDocument)?.let {
                val result = it.toLinkedDomainResult()
                LinkedDomainValidationAttempt(
                    result = result,
                    source = LinkedDomainValidationSource.TRUSTED_RESOLVER,
                    flow = flow,
                    failureStage = if (result is LinkedDomainVerified) {
                        LinkedDomainValidationFailureStage.NONE
                    } else {
                        LinkedDomainValidationFailureStage.RESOLVER_REJECTED
                    }
                )
            }
                ?: throw SdkException("Root of trust resolver is not configured")
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: Exception) {
            SdkLog.w(
                "Linked Domains verification using resolver failed. Verifying using well-known document.",
                ex
            )
            try {
                verifyLinkedDomainsUsingWellKnownDocument(identifierDocument).copy(flow = flow)
            } catch (ex: CancellationException) {
                throw ex
            } catch (ex: Exception) {
                SdkLog.w("Linked Domains verification failed.", ex)
                LinkedDomainValidationAttempt(
                    result = LinkedDomainMissing,
                    source = LinkedDomainValidationSource.WELL_KNOWN,
                    flow = flow,
                    failureStage = LinkedDomainValidationFailureStage.DOCUMENT_INVALID
                )
            }
        }
        val attributedAttempt = validationAttempt.copy(
            validationMode = if (linkedDomainValidationHardeningEnabled) {
                LinkedDomainValidationMode.HARDENED
            } else {
                LinkedDomainValidationMode.LEGACY
            }
        )
        attributedAttempt.emit()
        return Result.success(attributedAttempt.result)
    }

    suspend fun fetchDocumentAndVerifyLinkedDomains(
        relyingPartyDid: String,
        flow: LinkedDomainValidationFlow = LinkedDomainValidationFlow.UNKNOWN
    ): Result<LinkedDomainResult> {
        resolveIdentifierDocument(relyingPartyDid)
            .onSuccess {
                val linkedDomainsValidationResult = validateLinkedDomains(it, flow)
                if (linkedDomainsValidationResult.isFailure)
                    SdkLog.w("Linked Domains validation failed")
                return linkedDomainsValidationResult
            }
            .onFailure {
                SdkLog.w("Failed to fetch identifier document.", it)
                return Result.failure(it)
            }
        SdkLog.w("Failed to fetch identifier document.")
        return Result.failure(SdkException("Failed to fetch identifier document"))
    }

    private suspend fun verifyLinkedDomainsUsingWellKnownDocument(
        identifierDocument: IdentifierDocument
    ): LinkedDomainValidationAttempt {
        val linkedDomains = getLinkedDomainsFromDidDocument(identifierDocument)
        verifyLinkedDomains(linkedDomains, identifierDocument.id)
            .onSuccess { return it }
            .onFailure { throw it }
        return LinkedDomainValidationAttempt(
            result = LinkedDomainMissing,
            source = LinkedDomainValidationSource.WELL_KNOWN,
            failureStage = LinkedDomainValidationFailureStage.DOCUMENT_INVALID
        )
    }

    private suspend fun verifyLinkedDomains(
        domainUrls: List<String>,
        relyingPartyDid: String
    ): Result<LinkedDomainValidationAttempt> {
        val linkedDomainValidationHardeningEnabled =
            flightProvider.isEnabled(WalletLibraryFlight.LinkedDomainValidationHardening)
        val endpointCountBucket = LinkedDomainValidationCountBucket.from(domainUrls.size)
        if (domainUrls.isEmpty()) {
            return Result.success(
                LinkedDomainValidationAttempt(
                    result = LinkedDomainMissing,
                    source = LinkedDomainValidationSource.WELL_KNOWN,
                    failureStage = LinkedDomainValidationFailureStage.ENDPOINT_MISSING,
                    endpointCountBucket = endpointCountBucket
                )
            )
        }
        val domainUrl = domainUrls.first()
        val domainOrigin = if (linkedDomainValidationHardeningEnabled) {
            runCatching {
                canonicalizeLinkedDomainOrigin(domainUrl)
            }.getOrElse { throwable ->
                val hostname = runCatching { URI(domainUrl).host }.getOrNull()
                val result = if (hostname == null) LinkedDomainMissing else LinkedDomainUnVerified(hostname)
                SdkLog.w("Rejected invalid Linked Domains service endpoint", throwable)
                return Result.success(
                    LinkedDomainValidationAttempt(
                        result = result,
                        source = LinkedDomainValidationSource.WELL_KNOWN,
                        failureStage = LinkedDomainValidationFailureStage.ENDPOINT_INVALID,
                        endpointCountBucket = endpointCountBucket
                    )
                )
            }
        } else {
            domainUrl
        }
        val hostname = if (linkedDomainValidationHardeningEnabled) {
            URI(domainOrigin).host
        } else {
            URL(domainOrigin).host
        }
        getWellKnownConfigDocument(domainOrigin)
            .onSuccess { wellKnownConfigDocument ->
                val credentialCountBucket =
                    LinkedDomainValidationCountBucket.from(wellKnownConfigDocument.linkedDids.size)
                if (wellKnownConfigDocument.linkedDids.isEmpty()) {
                    return Result.success(
                        LinkedDomainValidationAttempt(
                            result = LinkedDomainMissing,
                            source = LinkedDomainValidationSource.WELL_KNOWN,
                            failureStage = LinkedDomainValidationFailureStage.NO_CREDENTIALS,
                            endpointCountBucket = endpointCountBucket,
                            credentialCountBucket = credentialCountBucket,
                            httpStatusClass = LinkedDomainValidationHttpStatusClass.SUCCESS
                        )
                    )
                }
                wellKnownConfigDocument.linkedDids.firstNotNullOf { linkedDidJwt ->
                    val credentialValidationResult = jwtDomainLinkageCredentialValidator.validate(
                        linkedDidJwt,
                        relyingPartyDid,
                        domainOrigin
                    )
                    return Result.success(
                        LinkedDomainValidationAttempt(
                            result = if (credentialValidationResult == DomainLinkageCredentialValidationResult.VALID) {
                                LinkedDomainVerified(
                                    if (linkedDomainValidationHardeningEnabled) domainOrigin else hostname
                                )
                            } else {
                                LinkedDomainUnVerified(hostname)
                            },
                            source = LinkedDomainValidationSource.WELL_KNOWN,
                            failureStage = credentialValidationResult.toFailureStage(),
                            endpointCountBucket = endpointCountBucket,
                            credentialCountBucket = credentialCountBucket,
                            httpStatusClass = LinkedDomainValidationHttpStatusClass.SUCCESS
                        )
                    )
                }
            }
            .onFailure { throwable ->
                SdkLog.w("Unable to fetch well-known config document.", throwable)
                val httpStatusClass = LinkedDomainValidationHttpStatusClass.from(throwable)
                return Result.success(
                    LinkedDomainValidationAttempt(
                        result = LinkedDomainUnVerified(hostname),
                        source = LinkedDomainValidationSource.WELL_KNOWN,
                        failureStage = if (httpStatusClass == LinkedDomainValidationHttpStatusClass.REDIRECTION) {
                            LinkedDomainValidationFailureStage.REDIRECT_RESPONSE
                        } else {
                            LinkedDomainValidationFailureStage.FETCH_FAILED
                        },
                        endpointCountBucket = endpointCountBucket,
                        httpStatusClass = httpStatusClass
                    )
                )
            }
        return Result.success(
            LinkedDomainValidationAttempt(
                result = LinkedDomainMissing,
                source = LinkedDomainValidationSource.WELL_KNOWN,
                failureStage = LinkedDomainValidationFailureStage.DOCUMENT_INVALID,
                endpointCountBucket = endpointCountBucket
            )
        )
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
            apiProvider,
            flightProvider.isEnabled(WalletLibraryFlight.LinkedDomainValidationHardening)
        ).fire()
}

private fun DomainLinkageCredentialValidationResult.toFailureStage(): LinkedDomainValidationFailureStage =
    when (this) {
        DomainLinkageCredentialValidationResult.VALID -> LinkedDomainValidationFailureStage.NONE
        DomainLinkageCredentialValidationResult.SIGNATURE_INVALID ->
            LinkedDomainValidationFailureStage.SIGNATURE_INVALID
        DomainLinkageCredentialValidationResult.CLAIMS_INVALID ->
            LinkedDomainValidationFailureStage.CLAIMS_INVALID
        DomainLinkageCredentialValidationResult.DID_MISMATCH ->
            LinkedDomainValidationFailureStage.DID_MISMATCH
        DomainLinkageCredentialValidationResult.ORIGIN_MISMATCH ->
            LinkedDomainValidationFailureStage.ORIGIN_MISMATCH
    }