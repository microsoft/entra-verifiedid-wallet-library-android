// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.identifier

import android.util.Base64
import com.microsoft.walletlibrary.did.sdk.util.Constants
import com.microsoft.walletlibrary.util.HolderIdentifierCreationException
import com.microsoft.walletlibrary.util.VerifiedIdExceptions
import com.nimbusds.jose.jwk.JWK
import org.erdtman.jcs.JsonCanonicalizer

internal object DidCreator {

    // Creates a DID from Public Key JWK and a DID method. Currently only P-256 keys and did:jwk are supported.
    fun createDid(jwk: JWK, didMethod: String): String {
        if (jwk.keyType.value != "EC") {
            throw HolderIdentifierCreationException("Only EC keys are supported", VerifiedIdExceptions.HOLDER_IDENTIFIER_EXCEPTION.value)
        }
        if (didMethod != "did:jwk") {
            throw HolderIdentifierCreationException("Only did:jwk is supported", VerifiedIdExceptions.HOLDER_IDENTIFIER_EXCEPTION.value)
        }
        val utf8EncodedJwk = JsonCanonicalizer(jwk.toJSONString()).encodedUTF8
        return didMethod + ":" + Base64.encodeToString(utf8EncodedJwk, Constants.BASE64_URL_SAFE)
    }
}