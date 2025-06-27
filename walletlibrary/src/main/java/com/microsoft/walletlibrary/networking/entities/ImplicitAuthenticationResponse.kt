// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.networking.entities

import com.microsoft.walletlibrary.verifiedid.ImplicitAuthenticationResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ImplicitAuthenticationResponse(
    @SerialName("redirect_uri")
    override val redirectUri: String
) : ImplicitAuthenticationResult