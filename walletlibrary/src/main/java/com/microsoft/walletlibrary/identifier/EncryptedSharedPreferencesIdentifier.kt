// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.identifier

import com.microsoft.walletlibrary.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jose.util.Base64URL

/**
 * Holder Identifier which stores the private key in EncryptedSharedPreferences.
 */
internal class EncryptedSharedPreferencesIdentifier(
    override val id: String,
    override val algorithm: String,
    override val method: String,
    override val keyReference: String,
    private val keyStore: EncryptedKeyStore
) : HolderIdentifier {

    var jwsHeader: JWSHeader? = null

    override fun sign(data: ByteArray?): Base64URL {
        val privateKey = keyStore.getKey(keyReference).toECKey()
        val ecdsaSigner = ECDSASigner(privateKey)
        return ecdsaSigner.sign(jwsHeader, data)
    }
}