// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.requests.requirements

import com.microsoft.walletlibrary.identifier.HolderIdentifier

/**
 * Interface for a requirement of cryptographic operations that in Identifier must satisfy.
 */
internal interface CryptoRequirement {

    // Determines if the provided HolderIdentifier satisfies the cryptographic requirement.
    fun isSupported(holderIdentifier: HolderIdentifier) : Boolean
}