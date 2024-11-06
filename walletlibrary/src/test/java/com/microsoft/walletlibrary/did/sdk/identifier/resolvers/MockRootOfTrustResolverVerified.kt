// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.did.sdk.identifier.resolvers

import com.microsoft.walletlibrary.did.sdk.identifier.models.identifierdocument.DidMetadata
import com.microsoft.walletlibrary.requests.RootOfTrust

class MockRootOfTrustResolverVerified : RootOfTrustResolver {
    override suspend fun resolve(didMetadata: DidMetadata): RootOfTrust {
        return RootOfTrust(
            "discover.did.microsoft.com",
            true
        )
    }
}