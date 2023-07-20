// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.did.sdk.credential.service.models.presentationexchange

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class IssuanceMetadata(
    @SerialName("manifest")
    var issuerContract: String = "",

    @SerialName("did")
    val issuerDid: String = ""
)