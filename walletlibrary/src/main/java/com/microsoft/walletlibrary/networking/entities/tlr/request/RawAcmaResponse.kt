// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.networking.entities.tlr.request

import kotlinx.serialization.Serializable

@Serializable
internal data class RawAcmaResponse(
    val id: String,
    val type: String,
    val continuationToken: String,
    val state: String,
    val presentationRequestUrl: PresentationRequestUrl,
    val links: Map<String, AcmaLink>
)