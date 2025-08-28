// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.requests.tlr

import com.microsoft.walletlibrary.util.VerifiedIdResult

interface BaseAcmaRequest {
    // Completes the request and returns a result if successful.
    suspend fun complete(): VerifiedIdResult<AcmaResponse>
}