// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.did.sdk.identifier.resolvers

import com.microsoft.walletlibrary.did.sdk.identifier.models.identifierdocument.DidMetadata
import com.microsoft.walletlibrary.did.sdk.util.controlflow.SdkException
import com.microsoft.walletlibrary.requests.RootOfTrust

class MockRootOfTrustResolverFailure : RootOfTrustResolver {
    override suspend fun resolve(didMetadata: DidMetadata): RootOfTrust {
        throw SdkException("Root of trust resolver is not configured")
    }
}