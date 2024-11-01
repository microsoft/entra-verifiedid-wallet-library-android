// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.mappings.identifier

import com.microsoft.walletlibrary.did.sdk.crypto.CryptoOperations
import com.microsoft.walletlibrary.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.walletlibrary.did.sdk.identifier.models.Identifier
import com.microsoft.walletlibrary.identifier.EncryptedSharedPreferencesIdentifier
import com.microsoft.walletlibrary.identifier.HolderIdentifier

internal fun Identifier.toHolderIdentifier(encryptedKeyStore: EncryptedKeyStore): HolderIdentifier {
    return EncryptedSharedPreferencesIdentifier(
        id = id,
        algorithm = "ES256K",
        method = "did:ion",
        keyReference = signatureKeyReference,
        cryptoOperations = CryptoOperations,
        keyStore = encryptedKeyStore
    )
}