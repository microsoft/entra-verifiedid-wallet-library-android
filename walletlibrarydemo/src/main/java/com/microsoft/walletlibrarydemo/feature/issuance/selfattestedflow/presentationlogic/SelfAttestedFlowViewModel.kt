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
    var verifiedIdRequestResult: Result<VerifiedIdRequest<*>>? = null
    var verifiedIdResult: Result<Any?>? = null

    suspend fun initiateIssuance() {
        val verifiedIdClient = VerifiedIdClientBuilder(context).build()
        // Use the test uri here
        val verifiedIdRequestUrl =
            VerifiedIdRequestURL(Uri.parse(""))
        verifiedIdRequestResult = verifiedIdClient.createRequest(verifiedIdRequestUrl)
    }

    suspend fun completeIssuance() {
        if (verifiedIdRequestResult?.isSuccess == true) {
            verifiedIdResult = verifiedIdRequestResult?.getOrNull()?.complete()
        }
    }
}