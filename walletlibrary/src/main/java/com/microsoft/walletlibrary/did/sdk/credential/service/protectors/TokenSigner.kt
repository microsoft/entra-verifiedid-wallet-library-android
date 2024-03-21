/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.did.sdk.credential.service.protectors

import com.microsoft.walletlibrary.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.walletlibrary.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.walletlibrary.did.sdk.identifier.models.Identifier
import com.microsoft.walletlibrary.util.Constants
import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class TokenSigner @Inject constructor(
    private val keyStore: EncryptedKeyStore
) {
    fun signWithIdentifier(payload: String, identifier: Identifier, type: String? = null): String {
        val token = JwsToken(payload, JWSAlgorithm.ES256K)
        val openid4vci = JOSEObjectType(Constants.OPENID4VCI_TYPE_HEADER)
        val typeHeader = type?.let { openid4vci } ?: JOSEObjectType.JWT
        // adding kid value to header.
        val header = JWSHeader.Builder(JWSAlgorithm.ES256K)
            .type(typeHeader)
            .keyID("${identifier.id}#${identifier.signatureKeyReference}")
            .build()
        val privateKey = keyStore.getKey(identifier.signatureKeyReference)
        token.sign(privateKey, header)
        return token.serialize()
    }
}