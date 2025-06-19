// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.networking.entities

import com.microsoft.walletlibrary.verifiedid.ImplicitAuthenticationResult
import kotlinx.serialization.Serializable

@Serializable
internal data class ImplicitAuthenticationResponse(
    override val redirectUri: String
) : ImplicitAuthenticationResult