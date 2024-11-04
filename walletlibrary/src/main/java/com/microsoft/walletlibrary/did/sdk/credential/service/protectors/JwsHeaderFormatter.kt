// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.did.sdk.credential.service.protectors

import com.microsoft.walletlibrary.identifier.HolderIdentifier
import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader

internal object JwsHeaderFormatter {

    fun formatHeader(jwsAlgorithm: String, keyId: String, type: String = "JWT"): JWSHeader {
        return JWSHeader.Builder(JWSAlgorithm(jwsAlgorithm))
            .type(JOSEObjectType(type))
            .keyID(keyId)
            .build()
    }

    fun formatHeader(holderIdentifier: HolderIdentifier, type: String = "JWT"): JWSHeader {
        val keyId = "${holderIdentifier.id}#${holderIdentifier.keyReference}"
        return JWSHeader.Builder(JWSAlgorithm(holderIdentifier.algorithm))
            .type(JOSEObjectType(type))
            .keyID(keyId)
            .build()
    }
}