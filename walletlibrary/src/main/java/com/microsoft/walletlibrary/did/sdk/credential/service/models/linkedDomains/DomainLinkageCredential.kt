// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class DomainLinkageCredential(
    @SerialName("sub")
    val subject: String,

    @SerialName("iss")
    val issuer: String,

    @SerialName("nbf")
    val notValidBefore: Long,

    val vc: DomainLinkageCredentialContent
)