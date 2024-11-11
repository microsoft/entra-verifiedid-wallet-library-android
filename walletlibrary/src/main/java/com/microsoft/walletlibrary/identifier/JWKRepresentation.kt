// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.identifier

import com.nimbusds.jose.jwk.JWK

internal interface JWKRepresentation {
    fun getPublicKey() : JWK
}