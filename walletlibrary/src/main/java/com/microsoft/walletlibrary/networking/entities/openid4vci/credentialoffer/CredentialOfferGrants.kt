package com.microsoft.walletlibrary.networking.entities.openid4vci.credentialoffer

import kotlinx.serialization.Serializable

@Serializable
internal data class CredentialOfferGrants(
    /**
     * A string that the Wallet can use to identify the Authorization Server to use with this grant
     * type when authorization_servers parameter in the Credential Issuer metadata has multiple
     * entries. MUST NOT be used otherwise. The value of this parameter MUST match with one of the
     * values in the authorization_servers array obtained from the Credential Issuer metadata.
     */
    val authorization_server: String
)