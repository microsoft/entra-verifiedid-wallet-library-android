package com.microsoft.walletlibrary.networking.entities.openid4vci.request

import kotlinx.serialization.Serializable

/**
 * The proof needed to get the credential represented in JWT format.
 */
@Serializable
internal data class OpenID4VCIJWTProof(
    // The format that the proof is in.
    val proof_type: String = "jwt",

    // The proof in JWT format.
    val jwt: String
)