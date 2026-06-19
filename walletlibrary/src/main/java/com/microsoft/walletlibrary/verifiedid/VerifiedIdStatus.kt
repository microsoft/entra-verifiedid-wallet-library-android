/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.verifiedid

/**
 * The status of a [VerifiedId], returned by
 * [com.microsoft.walletlibrary.VerifiedIdClient.checkVerifiedIdStatus]. See each subtype below.
 */
sealed class VerifiedIdStatus {
    /** The credential is valid and has not been revoked, suspended, or expired. */
    object Valid : VerifiedIdStatus()

    /** The issuer has revoked this credential. */
    object Revoked : VerifiedIdStatus()

    /** The issuer has temporarily suspended this credential. */
    object Suspended : VerifiedIdStatus()

    /**
     * The credential's `expiresOn` date has passed. Determined from the credential's own
     * `expiresOn` field, without fetching the issuer's status list — unlike [Revoked]/[Suspended].
     */
    object Expired : VerifiedIdStatus()

    /**
     * Status could not be determined (e.g. network error, unrecognised response format, or the
     * status list URL uses a DID method not yet supported).
     */
    object Unknown : VerifiedIdStatus()

    /**
     * The credential does not carry a `credentialStatus` field, so no status endpoint exists.
     */
    object NoStatusEndpoint : VerifiedIdStatus()
}
