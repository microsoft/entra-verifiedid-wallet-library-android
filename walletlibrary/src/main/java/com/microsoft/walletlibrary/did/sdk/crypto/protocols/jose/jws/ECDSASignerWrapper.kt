// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.did.sdk.crypto.protocols.jose.jws

import com.microsoft.walletlibrary.identifier.HolderIdentifier
import com.microsoft.walletlibrary.util.CryptoException
import com.microsoft.walletlibrary.util.VerifiedIdExceptions
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSSigner
import com.nimbusds.jose.jca.JCAContext
import com.nimbusds.jose.util.Base64URL

internal class ECDSASignerWrapper(private val holderIdentifier: HolderIdentifier): JWSSigner {
    override fun getJCAContext(): JCAContext {
        TODO("Not yet implemented")
    }

    override fun supportedJWSAlgorithms(): MutableSet<JWSAlgorithm> {
        return mutableSetOf(JWSAlgorithm(holderIdentifier.algorithm))
    }

    override fun sign(header: JWSHeader?, signingInput: ByteArray?): Base64URL {
        return signingInput?.let { Base64URL.encode(holderIdentifier.sign(signingInput)) }
            ?: throw CryptoException("Data to sign is null", VerifiedIdExceptions.CRYPTO_EXCEPTION.value)
    }
}