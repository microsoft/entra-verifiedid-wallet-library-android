package com.microsoft.walletlibrary.networking.entities.openid4vci.request

import kotlinx.serialization.Serializable

/**
 * The Raw OpenID4VCI Request Data Model to send to the Issuance Service from Wallet.
 */
@Serializable
internal data class RawOpenID4VCIRequest(
    // Describes the credential to be issued.
    val credential_configuration_id: String,

    // The issuer state of the credential offering.
    val issuer_session: String,

    // Proof needed to get the credential.
    val proof: OpenID4VCIJWTProof
)