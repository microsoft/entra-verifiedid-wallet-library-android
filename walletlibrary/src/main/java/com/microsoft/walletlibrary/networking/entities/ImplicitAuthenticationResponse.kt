// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.networking.entities

import com.microsoft.walletlibrary.verifiedid.SuccessfulCompletionResult
import kotlinx.serialization.Serializable

@Serializable
internal data class SuccessfulCompletionResponse (
    override val redirectUri: String? = null
) : SuccessfulCompletionResult