package com.microsoft.walletlibrarydemo.feature.issuance.selfattestedflow.presentationlogic

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.microsoft.walletlibrary.VerifiedIdClientBuilder
import com.microsoft.walletlibrary.requests.VerifiedIdRequest
import com.microsoft.walletlibrary.requests.input.VerifiedIdRequestURL

//TODO(Add dependency injection)
class SelfAttestedFlowViewModel(@SuppressLint("StaticFieldLeak") val context: Context):
    ViewModel() {
    var verifiedIdRequest: VerifiedIdRequest<*>? = null
    var verifiedIdResult: Result<Any?>? = null

    suspend fun initiateIssuance() {
        val verifiedIdClient = VerifiedIdClientBuilder(context).build()
        // Use the test uri here
        val verifiedIdRequestUrl =
            VerifiedIdRequestURL(Uri.parse("openid-vc://?request_uri=https://verifiedid.did.msidentity.com/v1.0/tenants/9c59be8b-bd18-45d9-b9d9-082bc07c094f/verifiableCredentials/issuanceRequests/d65f5579-a03c-4ef1-9437-85d70cfb8c54"))
        verifiedIdRequest = verifiedIdClient.createRequest(verifiedIdRequestUrl)
    }

    suspend fun completeIssuance() {
        verifiedIdResult = verifiedIdRequest?.complete()
    }
}