// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.mappings.identifier

import com.microsoft.walletlibrary.did.sdk.credential.service.protectors.JwsHeaderFormatter
import com.microsoft.walletlibrary.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.walletlibrary.did.sdk.identifier.models.Identifier
import com.microsoft.walletlibrary.identifier.EncryptedSharedPreferencesIdentifier
import com.microsoft.walletlibrary.identifier.HolderIdentifier

internal fun Identifier.toHolderIdentifier(encryptedKeyStore: EncryptedKeyStore): HolderIdentifier {
    val jwsHeader = JwsHeaderFormatter.formatHeader("ES256K", "$id#$signatureKeyReference")
    val encryptedSharedPreferencesIdentifier = EncryptedSharedPreferencesIdentifier(
        id = id,
        algorithm = "ES256K",
        method = "did:ion",
        keyReference = signatureKeyReference,
        keyStore = encryptedKeyStore
    )
    encryptedSharedPreferencesIdentifier.jwsHeader = jwsHeader
    return encryptedSharedPreferencesIdentifier
}