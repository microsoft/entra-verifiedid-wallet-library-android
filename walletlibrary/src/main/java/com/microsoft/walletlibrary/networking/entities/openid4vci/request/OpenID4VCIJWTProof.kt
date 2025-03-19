package com.microsoft.walletlibrary.networking.entities.openid4vci.request

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

/**
 * The proof needed to get the credential represented in JWT format.
 */
@Serializable
internal data class OpenID4VCIJWTProof constructor(
    // The proof in JWT format.
    val jwt: String,

    // The format that the proof is in.
    @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
    @EncodeDefault
    val proof_type: String = "jwt"
)