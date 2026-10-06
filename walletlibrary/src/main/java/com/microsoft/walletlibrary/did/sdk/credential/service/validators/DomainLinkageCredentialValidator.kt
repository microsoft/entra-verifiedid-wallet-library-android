// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.did.sdk.credential.service.validators

internal interface DomainLinkageCredentialValidator {

    suspend fun validate(
        domainLinkageCredential: String,
        rpDid: String,
        rpDomain: String
    ): DomainLinkageCredentialValidationResult
}

internal enum class DomainLinkageCredentialValidationResult {
    VALID,
    SIGNATURE_INVALID,
    CLAIMS_INVALID,
    DID_MISMATCH,
    ORIGIN_MISMATCH
}