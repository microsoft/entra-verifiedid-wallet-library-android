// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.did.sdk.credential.service.models.presentationexchange

import kotlinx.serialization.Serializable

@Serializable
internal data class Fields(
    val path: List<String>,
    var purpose: String = ""
) {
    var filter: Filter? = null
}
