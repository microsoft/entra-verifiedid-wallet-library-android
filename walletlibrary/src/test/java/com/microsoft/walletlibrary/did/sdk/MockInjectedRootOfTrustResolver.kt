// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.did.sdk

import com.microsoft.walletlibrary.did.sdk.identifier.models.identifierdocument.DidMetadata
import com.microsoft.walletlibrary.did.sdk.identifier.resolvers.RootOfTrustResolver
import com.microsoft.walletlibrary.requests.RootOfTrust

class MockInjectedRootOfTrustResolver : RootOfTrustResolver {
    override suspend fun resolve(didMetadata: DidMetadata): RootOfTrust {
        when (didMetadata.id) {
            MockDidMetadata.VALID_DOMAIN_DID.value -> {
                return RootOfTrust("validDomain", true)
            }
            MockDidMetadata.EMPTY_DOMAIN_DID.value -> {
                throw EmptyDomainListException()
            }
            else -> {
                throw DomainValidationException()
            }
        }
    }
}

class EmptyDomainListException : Exception()

class DomainValidationException : Exception()

enum class MockDidMetadata(val value: String) {
    VALID_DOMAIN_DID("did:web:validDomain"),
    EMPTY_DOMAIN_DID("did:web:emptyDomainList")
}