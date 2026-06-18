/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.did.sdk.credential.models

import kotlinx.serialization.Serializable

/**
 * Credential status entry embedded in a Verifiable Credential JWT.
 *
 * Mirrors the credential-status types the Entra status service supports. All use the same
 * GZIP-compressed bitstring encoding; they differ only in which URL/index field names are used:
 *
 * | type                       | URL field                  | index field             | notes                                  |
 * |----------------------------|----------------------------|-------------------------|----------------------------------------|
 * | RevocationList2021Status   | statusListCredential       | statusListIndex         | Entra DID-relative URL (IdentityHub).  |
 * |                            |                            |                         | Bit index may instead come from the    |
 * |                            |                            |                         | `urn:uuid:<id>?bit-index=N` id.        |
 * | StatusList2021Entry        | statusListCredential       | statusListIndex         | W3C StatusList2021 spec; HTTPS URL.    |
 * | RevocationList2020Status   | revocationListCredential   | revocationListIndex     | Older format; HTTPS URL.               |
 *
 * Use [effectiveStatusListCredential] and [effectiveStatusListIndex] instead of the raw fields
 * so callers do not need to branch on [type]. For the `urn:uuid:` IdentityHub case, the bit index
 * is parsed from the id's `bit-index` query parameter by the status check itself.
 */
@Serializable
internal data class CredentialStatusDescriptor(
    val id: String,
    val type: String,
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
