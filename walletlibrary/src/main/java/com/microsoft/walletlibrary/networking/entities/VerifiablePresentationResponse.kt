// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.networking.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VerifiablePresentationResponse (
    @SerialName("redirect_uri")
    val redirectUrl: String? = null
)