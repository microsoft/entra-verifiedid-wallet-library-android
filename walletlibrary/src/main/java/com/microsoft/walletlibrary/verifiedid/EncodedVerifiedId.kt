// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.verifiedid

import kotlinx.serialization.Serializable

@Serializable
data class EncodedVerifiedId(val type: String, val encoded: String)