// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.did.sdk.crypto.spi

import java.security.spec.KeySpec

internal class EcPairwisePrivateKeySpec(
    val personaSeed: ByteArray,
    val peerDid: String
) : KeySpec