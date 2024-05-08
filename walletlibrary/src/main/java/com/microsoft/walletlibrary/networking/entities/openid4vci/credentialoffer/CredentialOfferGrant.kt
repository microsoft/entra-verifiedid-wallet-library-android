package com.microsoft.walletlibrary.networking.entities.openid4vci.credentialoffer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class CredentialOfferGrant(
    /**
     * A string that the Wallet can use to identify the Authorization Server to use with this grant
     * type when authorization_servers parameter in the Credential Issuer metadata has multiple
     * entries. MUST NOT be used otherwise. The value of this parameter MUST match with one of the
     * values in the authorization_servers array obtained from the Credential Issuer metadata.
     */
    val authorization_server: String,

    /**
     * The code representing the Credential Issuer's authorization for the Wallet to obtain
     * Credentials of a certain type. This code MUST be short lived and single use. If the Wallet
     * decides to use the Pre-Authorized Code Flow, this parameter value MUST be included in the
     * subsequent Token Request with the Pre-Authorized Code Flow.
     */
    @SerialName("pre-authorized_code")
    val preAuthorizedCode: String? = null,

    // Pin requirements for the credential.
    val tx_code: CredentialOfferPinDetails? = null,

    val user_pin_required: Boolean? = null
)