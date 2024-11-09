// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.identifier

import com.microsoft.walletlibrary.did.sdk.crypto.KeyGenAlgorithm

internal enum class DidMethod(val value: String) {
    DID_JWK("did:jwk")
}

internal enum class KeyGenAlgorithms {
    EC
}

internal enum class JsonWebAlgorithm(val value: KeyGenAlgorithm) {
    ES256(KeyGenAlgorithm.P256),
    ES256K(KeyGenAlgorithm.Secp256k1)
}