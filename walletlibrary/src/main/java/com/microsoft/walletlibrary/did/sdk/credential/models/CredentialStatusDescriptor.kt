/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.did.sdk.credential.models

import kotlinx.serialization.Serializable

/**
 * Credential status entry embedded in a Verifiable Credential JWT. Covers the Entra/W3C variants
 * (RevocationList2021Status, StatusList2021Entry, RevocationList2020Status), which differ only in
 * field names — use [effectiveStatusListCredential]/[effectiveStatusListIndex] so callers need not
 * branch on [type].
 */
@Serializable
internal data class CredentialStatusDescriptor(
    val id: String,
    val type: String,
    // W3C StatusList2021Entry: whether a set bit means revocation or suspension. Older
    // RevocationList2021Status credentials may omit it, so it defaults to empty ("no constraint").
    val statusPurpose: String = "",
    // RevocationList2021Status / StatusList2021Entry field names
    val statusListIndex: Int = 0,
    val statusListCredential: String = "",
    // RevocationList2020Status field names
    val revocationListIndex: Int = 0,
    val revocationListCredential: String = ""
) {
    /** URL of the status list credential, normalised across all known type variants. */
    val effectiveStatusListCredential: String
        get() = statusListCredential.ifEmpty { revocationListCredential }

    /** Bit index within the status list bitstring, normalised across all known type variants. */
    val effectiveStatusListIndex: Int
        get() = if (statusListCredential.isNotEmpty()) statusListIndex else revocationListIndex
}
