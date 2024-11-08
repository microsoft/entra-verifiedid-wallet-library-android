// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.identifier

import com.microsoft.walletlibrary.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.ECDSASigner

/**
 * Holder Identifier which stores the private key in EncryptedSharedPreferences.
 */
internal class EncryptedSharedPreferencesIdentifier(
    override val id: String,
    override val algorithm: String,
    override val method: String,
    override val keyReference: String,
    private val keyStore: EncryptedKeyStore,
    private val keyId: String
) : HolderIdentifier {

    override fun sign(data: ByteArray): ByteArray {
        val privateKey = keyStore.getKey(keyId).toECKey()
        val jwsHeader = JWSHeader.Builder(JWSAlgorithm(algorithm)).build()
        val ecdsaSigner = ECDSASigner(privateKey)
        return ecdsaSigner.sign(jwsHeader, data).decode()
    }
}