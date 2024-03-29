// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.did.sdk.credential.service.validators

internal interface DomainLinkageCredentialValidator {

    suspend fun validate(domainLinkageCredential: String, rpDid: String, rpDomain: String): Boolean
}