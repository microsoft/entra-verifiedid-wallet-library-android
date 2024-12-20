package com.microsoft.walletlibrary.wrapper

import com.microsoft.walletlibrary.did.sdk.VerifiableCredentialSdk
import com.microsoft.walletlibrary.util.WalletLibraryLogger
import com.microsoft.walletlibrary.did.sdk.credential.service.models.issuancecallback.IssuanceCompletionResponse
import com.microsoft.walletlibrary.did.sdk.util.controlflow.Result

/**
 * Wrapper class to wrap the send issuance completion callback to VC SDK.
 */
object VerifiedIdCompletionCallBack {

    // sends the issuance callback to VC SDK and returns nothing if successful.
    internal suspend fun sendIssuanceCompletionResponse(
        issuanceCompletionResponse: IssuanceCompletionResponse,
        redirectUrl: String
    ) {
        var callbackResult: Result<Unit>? = null
        var exception: Exception? = null

        try {
            callbackResult = VerifiableCredentialSdk.issuanceService.sendCompletionResponse(
                issuanceCompletionResponse,
                redirectUrl
            )
        } catch (e: Exception) {
            exception = e
        }

        if (callbackResult == null || callbackResult is Result.Failure) {
            WalletLibraryLogger.e("Issuance callback failed.", exception)
        }
    }
}