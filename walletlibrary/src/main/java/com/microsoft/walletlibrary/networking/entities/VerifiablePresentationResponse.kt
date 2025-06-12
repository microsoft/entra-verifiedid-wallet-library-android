// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.networking.entities

import com.microsoft.walletlibrary.verifiedid.PresentationVerified
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class VerifiablePresentationResponse (
    @SerialName("redirect_uri")
    override val redirectUri: String? = null
) : PresentationVerified