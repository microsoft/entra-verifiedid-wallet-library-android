// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.networking.entities

import com.microsoft.walletlibrary.verifiedid.ImplicitAuthenticationResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ImplicitAuthenticationResponse(
    @SerialName(REDIRECT_URI_KEY_NAME)
    override val redirectUri: String,

    override val payload: String? = null
) : ImplicitAuthenticationResult {
    companion object {
        internal const val REDIRECT_URI_KEY_NAME = "redirect_uri"
    }
}