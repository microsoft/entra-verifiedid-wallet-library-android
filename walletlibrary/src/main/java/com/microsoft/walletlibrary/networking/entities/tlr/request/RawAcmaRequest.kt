// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.networking.entities.tlr.request

import kotlinx.serialization.Serializable

@Serializable
internal data class RawAcmaRequest(
    // ACMA continuation token.
    val continuationToken: String
)
