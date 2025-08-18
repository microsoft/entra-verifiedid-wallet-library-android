// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.did.sdk.credential.service.models.oidc

import kotlinx.serialization.Serializable

@Serializable
internal data class Continuation (
    // Uniquely identifies subject of continuation token.
    val upn: String,

    // Recovery URL.
    val url: String,

    // Actual continuation token.
    val payload: String,
)
