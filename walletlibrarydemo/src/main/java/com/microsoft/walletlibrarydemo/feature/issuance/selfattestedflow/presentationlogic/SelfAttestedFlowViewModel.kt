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
            VerifiedIdRequestURL(Uri.parse(""))
        verifiedIdRequest = verifiedIdClient.createRequest(verifiedIdRequestUrl)
    }

    suspend fun completeIssuance() {
        verifiedIdResult = verifiedIdRequest?.complete()
    }
}