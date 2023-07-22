// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.did.sdk.crypto.spi

import java.security.interfaces.ECPrivateKey
import java.security.spec.KeySpec

internal class EcPairwisePublicKeySpec(
    val privateKey: ECPrivateKey
) : KeySpec