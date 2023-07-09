package com.microsoft.walletlibrary.did.sdk.credential.service.validators

import com.microsoft.walletlibrary.did.sdk.credential.service.PresentationRequest

interface PresentationRequestValidator {

    suspend fun validate(request: PresentationRequest)
}