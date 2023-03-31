package com.microsoft.walletlibrarydemo.feature.issuance.idtokenhintflow.presentationlogic

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.microsoft.walletlibrary.VerifiedIdClientBuilder
import com.microsoft.walletlibrary.requests.VerifiedIdRequest
import com.microsoft.walletlibrary.requests.input.VerifiedIdRequestURL
import com.microsoft.walletlibrary.requests.requirements.GroupRequirement
import com.microsoft.walletlibrary.requests.requirements.PinRequirement
import com.microsoft.walletlibrary.requests.requirements.VerifiedIdRequirement
import com.microsoft.walletlibrary.verifiedid.VerifiedId

class IdTokenFlowViewModel(@SuppressLint("StaticFieldLeak") val context: Context): ViewModel() {
    var verifiedIdRequestResult: Result<VerifiedIdRequest<*>>? = null
    var verifiedIdResult: Result<Any?>? = null
    private val verifiedIdClient = VerifiedIdClientBuilder(context).build()
    var pin: String? = null

    suspend fun initiateIssuance(requestUri: Uri) {
        // Use the test uri here
        val verifiedIdRequestUrl =
            VerifiedIdRequestURL(requestUri)
        verifiedIdRequestResult = verifiedIdClient.createRequest(verifiedIdRequestUrl)
    }

    suspend fun completeIssuance() {
        if (verifiedIdRequestResult?.isSuccess == true) {
            when (val requirement = verifiedIdRequestResult?.getOrNull()?.requirement) {
                is GroupRequirement -> {
                    val requirementsInGroup = requirement.requirements
                    for (requirementInGroup in requirementsInGroup) {
                        if (requirementInGroup is PinRequirement)
                            pin?.let { requirementInGroup.fulfill(it) }
                    }
                    requirement.validate()
                }
            }
            verifiedIdResult = verifiedIdRequestResult?.getOrNull()?.complete()
        }
    }

    suspend fun initiatePresentation(requestUrl: Uri) {
        // Use the test uri here
        val verifiedIdRequestUrl = VerifiedIdRequestURL(requestUrl)
        verifiedIdRequestResult = verifiedIdClient.createRequest(verifiedIdRequestUrl)
    }

    suspend fun completePresentation() {
        if (verifiedIdRequestResult?.isSuccess == true) {
            val verifiedIdRequest = verifiedIdRequestResult?.getOrNull()
            (verifiedIdRequest?.requirement as VerifiedIdRequirement).fulfill(verifiedIdResult?.getOrDefault("") as VerifiedId)
            verifiedIdResult = verifiedIdRequest.complete()
        }
    }
}