// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.requests.requirements

import com.microsoft.walletlibrary.identifier.HolderIdentifier

/**
 * Matches only HolderIdentifiers with the same identifier as subject.
 */
class MatchingSubjectCryptoRequirement(private val subject: String) : CryptoRequirement {

    override fun isSupported(holderIdentifier: HolderIdentifier): Boolean {
        return holderIdentifier.id == this.subject
    }
}