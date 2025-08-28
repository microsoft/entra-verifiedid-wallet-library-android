// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.requests.handlers

import com.microsoft.walletlibrary.requests.Continuation
import com.microsoft.walletlibrary.requests.tlr.AcmaRequest
import com.microsoft.walletlibrary.requests.tlr.BaseAcmaRequest
import com.microsoft.walletlibrary.util.LibraryConfiguration

class AcmaHandler internal constructor(private val libraryConfiguration: LibraryConfiguration) {
    fun createRequest(continuation: Continuation): BaseAcmaRequest {
        return AcmaRequest(continuation, libraryConfiguration)
    }
}