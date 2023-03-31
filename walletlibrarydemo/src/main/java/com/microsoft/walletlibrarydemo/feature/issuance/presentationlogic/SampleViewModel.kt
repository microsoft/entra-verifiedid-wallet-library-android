package com.microsoft.walletlibrarydemo.feature.issuance.presentationlogic

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.microsoft.walletlibrary.VerifiedIdClientBuilder
import com.microsoft.walletlibrary.requests.VerifiedIdRequest
import com.microsoft.walletlibrary.requests.input.VerifiedIdRequestURL
import com.microsoft.walletlibrary.verifiedid.VerifiableCredential
import com.microsoft.walletlibrary.verifiedid.VerifiedId

class SampleViewModel(@SuppressLint("StaticFieldLeak") val context: Context): ViewModel() {
    var verifiedIdRequestResult: Result<VerifiedIdRequest<*>>? = null
    var verifiedIdResult: Result<Any?>? = null
    private val verifiedIdClient = VerifiedIdClientBuilder(context).build()
//    private val verifiedIdDao = VerifiedIdDatabase.getInstance(context).verifiedIdDao()

    suspend fun initiateRequest(requestUrl: String) {
        // Use the test uri here
        val verifiedIdRequestUrl =
            VerifiedIdRequestURL(Uri.parse(requestUrl))
        verifiedIdRequestResult = verifiedIdClient.createRequest(verifiedIdRequestUrl)
    }

    suspend fun completeIssuance() {
        verifiedIdResult = verifiedIdRequestResult?.getOrNull()?.complete()
        if (verifiedIdResult?.isSuccess == true) {
            val verifiedId = verifiedIdResult?.getOrNull() as VerifiedId
            val vc = com.microsoft.walletlibrarydemo.db.entities.VerifiedId(verifiedId.id, verifiedId as VerifiableCredential)
//            verifiedIdDao.insert(vc)
        }
    }

    suspend fun getVerifiedIds(): ArrayList<com.microsoft.walletlibrarydemo.db.entities.VerifiedId> {
        return emptyList<com.microsoft.walletlibrarydemo.db.entities.VerifiedId>() as ArrayList<com.microsoft.walletlibrarydemo.db.entities.VerifiedId>
//        return verifiedIdDao.queryVerifiedIds() as ArrayList<com.microsoft.walletlibrarydemo.db.entities.VerifiedId>
    }

    suspend fun completePresentation() {
        if (verifiedIdRequestResult?.isSuccess == true) {
            val verifiedIdRequest = verifiedIdRequestResult?.getOrNull()
                verifiedIdResult = verifiedIdRequest?.complete()
            }
    }
}