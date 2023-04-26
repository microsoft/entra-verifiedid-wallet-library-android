package com.microsoft.did.sdk.credential.service.validators

import com.microsoft.did.sdk.credential.service.PresentationRequest

internal interface PresentationRequestValidator {

    suspend fun validate(request: PresentationRequest)
}