package com.microsoft.walletlibrary.mappings

import com.microsoft.did.sdk.PresentationService
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.walletlibrary.requests.rawrequests.OpenIdRawRequest
import com.microsoft.walletlibrary.util.WalletLibraryException

/**
 * Maps Request object from VC SDK to RawRequest in library
 */
suspend fun PresentationService.getRawRequest(uri: String): OpenIdRawRequest {
    return when (val presentationRequestResult = this.getRequest(uri)) {
        is Result.Success -> { OpenIdRawRequest(presentationRequestResult.payload) }
        else -> throw WalletLibraryException()
    }
}