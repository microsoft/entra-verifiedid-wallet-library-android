// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.did.sdk.identifier.resolvers

import com.microsoft.walletlibrary.did.sdk.identifier.models.identifierdocument.DidMetadata
import com.microsoft.walletlibrary.did.sdk.util.controlflow.SdkException
import com.microsoft.walletlibrary.requests.RootOfTrust

class MockRootOfTrustResolver(val verified: Boolean? = null) : RootOfTrustResolver {
    override suspend fun resolve(didMetadata: DidMetadata): RootOfTrust {
        return when (verified) {
            true -> RootOfTrust(
                "discover.did.microsoft.com",
                true
            )
            false -> RootOfTrust(
                "discover.did.microsoft.com",
                false
            )
            else -> throw SdkException("Root of trust resolver is not configured")
        }
    }
}