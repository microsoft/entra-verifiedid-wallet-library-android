// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.identifier

import com.microsoft.walletlibrary.did.sdk.crypto.CryptoOperations
import com.microsoft.walletlibrary.did.sdk.crypto.keyStore.EncryptedKeyStore

/**
 * Holder Identifier which stores the private key in EncryptedSharedPreferences.
 */
internal class EncryptedSharedPreferencesIdentifier(
    override val id: String,
    override val algorithm: String,
    override val method: String,
    override val keyReference: String,
    private val cryptoOperations: CryptoOperations,
    private val keyStore: EncryptedKeyStore
) : HolderIdentifier {

    override fun sign(data: String): String {
        val keyId = "$id#$keyReference"
        val privateKey = keyStore.getKey(keyReference)
        return cryptoOperations.sign(data, privateKey, algorithm, keyId)
    }
}