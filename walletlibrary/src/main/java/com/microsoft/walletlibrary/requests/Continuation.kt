// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.requests

data class Continuation (
    // Uniquely identifies subject of continuation token.
    val upn: String,

    // Recovery URL.
    val url: String,

    // Actual continuation token.
    val payload: String,
)