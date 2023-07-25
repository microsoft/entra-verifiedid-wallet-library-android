package com.microsoft.walletlibrary.wrapper

import com.microsoft.walletlibrary.did.sdk.VerifiableCredentialSdk
import com.microsoft.walletlibrary.did.sdk.credential.service.models.issuancecallback.IssuanceCompletionResponse
import com.microsoft.walletlibrary.did.sdk.util.controlflow.Result
import com.microsoft.walletlibrary.util.VerifiedIdIssuanceCompletionCallbackException

/**
 * Wrapper class to wrap the send issuance completion callback to VC SDK.
 */
object VerifiedIdCompletionCallBack {

    // sends the issuance callback to VC SDK and returns nothing if successful.
    internal suspend fun sendIssuanceCompletionResponse(
        issuanceCompletionResponse: IssuanceCompletionResponse,
        redirectUrl: String
    ) {
        val callbackResult = VerifiableCredentialSdk.issuanceService.sendCompletionResponse(
            issuanceCompletionResponse,
            redirectUrl
        )
        if (callbackResult is Result.Failure) {
            throw VerifiedIdIssuanceCompletionCallbackException(
                "Unable to send issuance completion callback",
                callbackResult.payload
            )
        }
    }
}