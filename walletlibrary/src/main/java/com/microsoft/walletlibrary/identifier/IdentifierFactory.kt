// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.identifier

import com.microsoft.walletlibrary.requests.requirements.CryptoRequirement

/**
 * IdentifierFactory holds a list of identifiers and returns a identifier based on the cryptographic requirement.
 */
internal class IdentifierFactory {
    // List of available identifiers, arranged with FIPS compliant identifiers first.
    internal val identifiers = mutableListOf<HolderIdentifier>()

    // Returns the first identifier in the list that satisfies the provided cryptographic requirement.
    internal fun getIdentifier(cryptoRequirement: CryptoRequirement) : HolderIdentifier? {
        return identifiers.firstOrNull { cryptoRequirement.isSupported(it) }
    }
}