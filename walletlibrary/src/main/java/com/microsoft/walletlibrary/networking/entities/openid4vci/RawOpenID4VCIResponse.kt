package com.microsoft.walletlibrary.networking.entities.openid4vci

import kotlinx.serialization.Serializable

/**
 * The Raw Response from the Issuance Service that issues Wallet a Verified ID using OpenID4VCI.
 */
@Serializable
internal data class RawOpenID4VCIResponse(
    // The Verified ID that was issued.
    val credential: String? = null,

    // Notification ID to pass to notification endpoint.
    val notification_id: String? = null
)