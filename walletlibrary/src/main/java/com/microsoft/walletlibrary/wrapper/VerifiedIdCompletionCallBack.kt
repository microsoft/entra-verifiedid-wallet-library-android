package com.microsoft.walletlibrary.wrapper

import com.microsoft.did.sdk.VerifiableCredentialSdk
import com.microsoft.did.sdk.credential.service.models.issuancecallback.IssuanceCompletionResponse
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.walletlibrary.util.VerifiedIdCompletionCallbackException

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
            throw VerifiedIdCompletionCallbackException(
                "Unable to send issuance completion callback",
                callbackResult.payload
            )
        }
    }
}