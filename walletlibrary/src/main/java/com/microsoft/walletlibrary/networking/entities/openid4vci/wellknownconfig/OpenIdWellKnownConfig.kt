package com.microsoft.walletlibrary.networking.entities.openid4vci.wellknownconfig

import kotlinx.serialization.Serializable

/**
 * The openId well known configuration document.
 */
@Serializable
internal data class OpenIdWellKnownConfig(
    // The issuer of the configuration.
    val issuer: String,

    // Token endpoint to get the access token from the issuer.
    val token_endpoint: String,

    // Request types supported by the issuer.
    val grant_types_supported: List<String>
)