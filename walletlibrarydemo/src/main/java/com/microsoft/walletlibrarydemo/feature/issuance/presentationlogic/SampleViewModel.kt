package com.microsoft.walletlibrarydemo.feature.issuance.presentationlogic

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.microsoft.walletlibrary.VerifiedIdClientBuilder
import com.microsoft.walletlibrary.requests.VerifiedIdRequest
import com.microsoft.walletlibrary.requests.input.VerifiedIdRequestURL
import com.microsoft.walletlibrary.verifiedid.VerifiedId
import com.microsoft.walletlibrarydemo.db.VerifiedIdDatabase

class SampleViewModel(@SuppressLint("StaticFieldLeak") val context: Context): ViewModel() {
    var verifiedIdRequestResult: Result<VerifiedIdRequest<*>>? = null
    var verifiedIdResult: Result<Any?>? = null
    private val verifiedIdClient = VerifiedIdClientBuilder(context).build()
    private val verifiedIdDao = VerifiedIdDatabase.getInstance(context).verifiedIdDao()

    suspend fun initiateRequest(requestUrl: String) {
        val verifiedIdRequestUrl =
            VerifiedIdRequestURL(Uri.parse(requestUrl))
        verifiedIdRequestResult = verifiedIdClient.createRequest(verifiedIdRequestUrl)
    }

    suspend fun completeIssuance() {
        verifiedIdResult = verifiedIdRequestResult?.getOrNull()?.complete()
        if (verifiedIdResult?.isSuccess == true) {
            val verifiedId = verifiedIdResult?.getOrNull() as VerifiedId
            val encodedVerifiedId = verifiedIdClient.encode(verifiedId).getOrNull()
            encodedVerifiedId?.let {
                val vc = com.microsoft.walletlibrarydemo.db.entities.VerifiedId(verifiedId.id, encodedVerifiedId)
                verifiedIdDao.insert(vc)
            }
        }
    }

    suspend fun getVerifiedIds(): ArrayList<VerifiedId> {
        val encodedVerifiedIds = verifiedIdDao.queryVerifiedIds() as ArrayList<com.microsoft.walletlibrarydemo.db.entities.VerifiedId>
        val decodedVerifiedIds = ArrayList<VerifiedId>()
        encodedVerifiedIds.forEach { encodedVerifiedId -> verifiedIdClient.decodeVerifiedId(encodedVerifiedId.verifiedId).getOrNull()?.let { decodedVerifiedIds.add(it) } }
        return decodedVerifiedIds
    }

    suspend fun completePresentation() {
        if (verifiedIdRequestResult?.isSuccess == true) {
            val verifiedIdRequest = verifiedIdRequestResult?.getOrNull()
                verifiedIdResult = verifiedIdRequest?.complete()
            }
    }
}