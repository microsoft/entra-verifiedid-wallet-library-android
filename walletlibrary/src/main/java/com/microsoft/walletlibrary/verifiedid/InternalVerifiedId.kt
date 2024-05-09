// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.verifiedid

import com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredential

internal interface InternalVerifiedId : VerifiedId {
    val raw: VerifiableCredential
    override val types: List<String>
}