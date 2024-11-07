// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.identifier

import com.microsoft.walletlibrary.requests.requirements.CryptoRequirement
import com.microsoft.walletlibrary.util.HolderIdentifierMissingException
import com.microsoft.walletlibrary.util.VerifiedIdExceptions

/**
 * IdentifierFactory holds a list of identifiers and returns a identifier based on the cryptographic requirement.
 */
internal class IdentifierFactory {
    // List of available identifiers, arranged with FIPS compliant identifiers first.
    internal val identifiers = ArrayList<HolderIdentifier>()

    // Returns the first identifier in the list that satisfies the provided cryptographic requirement.
    internal fun getIdentifier(cryptoRequirement: CryptoRequirement? = null): HolderIdentifier {
        if (identifiers.isEmpty()) throw HolderIdentifierMissingException(
            "No identifiers available.",
            VerifiedIdExceptions.HOLDER_IDENTIFIER_EXCEPTION.value
        )
        val firstIdentifier = identifiers.first()
        cryptoRequirement?.let {
            return identifiers.first { cryptoRequirement.isSupported(it) }
        } ?: return firstIdentifier
    }
}