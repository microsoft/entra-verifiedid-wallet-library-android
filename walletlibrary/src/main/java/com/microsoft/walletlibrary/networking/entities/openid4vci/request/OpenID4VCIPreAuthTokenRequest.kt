package com.microsoft.walletlibrary.networking.entities.openid4vci.request

import kotlinx.serialization.Serializable

@Serializable
internal data class OpenID4VCIPreAuthTokenRequest(
    // Type of request, in this case it is always "urn:ietf:params:oauth:grant-type:pre-authorized_code".
    val grant_type: String,

    // Pre-authorized code found in the credential offer.
    val pre_authorized_code: String,

    // Pin provided by the user.
    val tx_code: String? = null
)