package com.microsoft.walletlibrary.networking.entities.openid4vci.wellknownconfig

import kotlinx.serialization.Serializable

@Serializable
internal data class OpenIdWellKnownConfig(
    val issuer: String,
    val token_endpoint: String,
    val grant_types_supported: List<String>
)