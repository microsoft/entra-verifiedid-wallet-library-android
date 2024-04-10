package com.microsoft.walletlibrary.networking.entities.openid4vci.request

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable

/**
 * The proof needed to get the credential represented in JWT format.
 */
@Serializable
internal data class OpenID4VCIJWTProof(
<<<<<<< HEAD
    // The format that the proof is in.
    val proof_type: String,

=======
>>>>>>> dev
    // The proof in JWT format.
    val jwt: String,

    // The format that the proof is in.
    @EncodeDefault
    val proof_type: String = "jwt"
)