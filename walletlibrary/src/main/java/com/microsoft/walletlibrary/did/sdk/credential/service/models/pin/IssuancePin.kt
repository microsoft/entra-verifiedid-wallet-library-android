// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.did.sdk.credential.service.models.pin

import kotlinx.serialization.Serializable

@Serializable
internal data class IssuancePin(var pin: String) {
    var pinSalt: String? = null
    var pinAlg: String? = null
    var iterations: Int = 0
}