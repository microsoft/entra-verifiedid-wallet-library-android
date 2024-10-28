// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.wrapper

import com.microsoft.walletlibrary.did.sdk.VerifiableCredentialSdk
import com.microsoft.walletlibrary.did.sdk.identifier.models.Identifier
import com.microsoft.walletlibrary.util.IdentifierFetchException

internal object IdentifierManager {
    internal suspend fun getMasterIdentifier(): Identifier {
        when (val identifierResult = VerifiableCredentialSdk.identifierService.getMasterIdentifier()) {
            is com.microsoft.walletlibrary.did.sdk.util.controlflow.Result.Success -> {
                return identifierResult.payload
            }
            is com.microsoft.walletlibrary.did.sdk.util.controlflow.Result.Failure -> {
                throw IdentifierFetchException(
                    "Unable to fetch master identifier",
                    identifierResult.payload
                )
            }
        }

    }
}