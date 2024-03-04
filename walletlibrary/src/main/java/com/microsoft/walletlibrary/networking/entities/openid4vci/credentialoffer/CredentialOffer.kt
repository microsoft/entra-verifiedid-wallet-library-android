package com.microsoft.walletlibrary.networking.entities.openid4vci.credentialoffer

import kotlinx.serialization.Serializable

@Serializable
internal data class CredentialOffer(
    // The end point of the credential issuer metadata.
    val credential_issuer: String,

    // The state of the request. Opaque to the wallet.
    val issuer_session: String,

    // The credential id that will be used to issue the Verified ID.
    val credential_configurations_ids: List<String>,

    // An object indicating to the Wallet the Grant Types the Credential Issuer's AS is prepared to process for this Credential Offer.
    val grants: Map<String, CredentialOfferGrants>
)