/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.verifiedid

/**
 * Bounded diagnostics for a Verified ID status check.
 */
enum class VerifiedIdStatusCheckOutcome {
    Passed,
    UnsupportedAlgorithm,
    SignatureVerificationFailed,
    MalformedResponse,
    NetworkError,
    Timeout,
    Unknown
}

/**
 * The credential [status] and the diagnostic [outcome] of determining it.
 */
data class VerifiedIdStatusResult(
    val status: VerifiedIdStatus,
    val outcome: VerifiedIdStatusCheckOutcome
)
