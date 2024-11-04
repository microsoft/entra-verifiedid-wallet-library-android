// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.identifier

import com.microsoft.walletlibrary.did.sdk.credential.service.protectors.JwsHeaderFormatter
import com.microsoft.walletlibrary.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.walletlibrary.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.nimbusds.jose.JWSHeader

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

    override fun sign(data: String): String {
        val keyId = "$id#$keyReference"
        val privateKey = keyStore.getKey(keyReference)
        val header = jwsHeader ?: JwsHeaderFormatter.formatHeader(algorithm, keyId)
        val jwsToken = JwsToken(data, header)
        jwsToken.sign(privateKey, header)
        return jwsToken.serialize()
    }
}