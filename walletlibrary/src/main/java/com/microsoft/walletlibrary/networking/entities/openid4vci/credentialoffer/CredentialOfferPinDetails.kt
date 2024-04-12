package com.microsoft.walletlibrary.networking.entities.openid4vci.credentialoffer

import kotlinx.serialization.Serializable

@Serializable
internal data class CredentialOfferPinDetails (
    // The length of the pin.
    val length: Int? = null,

    // The type of the pin (eg. numeric, alphanumeric, etc.).
    val input_mode: String? = null
)