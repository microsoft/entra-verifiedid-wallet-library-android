/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.did.sdk.util

import com.microsoft.walletlibrary.util.WalletLibraryLogger

internal object DidResolverHardeningTelemetry {
    private const val EVENT_NAME = "DIDResolverHardeningCheck"
    private const val CHECK = "check"
    private const val HARDENING_ENABLED = "hardening_enabled"
    private const val OUTCOME = "outcome"

    fun record(check: Check, hardeningEnabled: Boolean, outcome: Outcome) {
        WalletLibraryLogger.event(
            EVENT_NAME,
            mapOf(
                CHECK to check.value,
                HARDENING_ENABLED to hardeningEnabled.toString(),
                OUTCOME to outcome.value
            )
        )
    }

    enum class Check(val value: String) {
        IdentifierSyntax("identifier_syntax"),
        ResolvedDocumentId("resolved_document_id"),
        VerificationMethodId("verification_method_id")
    }

    enum class Outcome(val value: String) {
        Accepted("accepted"),
        Rejected("rejected")
    }
}
