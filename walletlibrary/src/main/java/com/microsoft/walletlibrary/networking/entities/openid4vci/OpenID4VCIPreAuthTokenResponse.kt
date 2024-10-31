package com.microsoft.walletlibrary.networking.entities.openid4vci

import kotlinx.serialization.Serializable

@Serializable
internal data class OpenID4VCIPreAuthTokenResponse(
    // The access token to complete the Verified ID issuance using Pre-Auth flow.
    val access_token: String? = null,

    // Type of token returned in access_token variable (eg. bearer).
    val token_type: String? = null,

    // Token expiration time.
    val expires_in: String? = null
)