// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.networking.entities.openid4vci.credentialoffer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class CredentialOfferPinDetails (
    // The length of the pin.
    val length: Int,

    // The type of the pin (eg. numeric, alphanumeric, etc.).
    @SerialName("input_mode")
    val inputMode: String
)