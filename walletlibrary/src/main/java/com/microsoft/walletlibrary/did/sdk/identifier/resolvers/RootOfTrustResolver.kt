package com.microsoft.walletlibrary.did.sdk.identifier.resolvers

import com.microsoft.walletlibrary.did.sdk.identifier.models.identifierdocument.DidMetadata
import com.microsoft.walletlibrary.requests.RootOfTrust

interface RootOfTrustResolver {
    suspend fun resolve(didMetadata: DidMetadata): RootOfTrust
}