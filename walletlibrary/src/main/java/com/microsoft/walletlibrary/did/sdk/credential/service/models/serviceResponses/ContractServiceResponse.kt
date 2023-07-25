// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.did.sdk.credential.service.models.serviceResponses

import kotlinx.serialization.Serializable

@Serializable
internal data class ContractServiceResponse(
    val token: String
)