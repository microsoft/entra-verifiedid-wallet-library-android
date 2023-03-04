package com.microsoft.walletlibrarydemo.feature.issuance.idtokenhintflow.presentationlogic

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.microsoft.walletlibrary.VerifiedIdClientBuilder
import com.microsoft.walletlibrary.requests.ManifestIssuanceRequest
import com.microsoft.walletlibrary.requests.VerifiedIdRequest
import com.microsoft.walletlibrary.requests.input.VerifiedIdRequestURL
import com.microsoft.walletlibrary.requests.requirements.GroupRequirement
import com.microsoft.walletlibrary.requests.requirements.PinRequirement
import com.microsoft.walletlibrary.verifiedid.VerifiedId

//TODO(Add dependency injection)
class IdTokenFlowViewModel(@SuppressLint("StaticFieldLeak") val context: Context): ViewModel() {
    var verifiedIdRequest: VerifiedIdRequest<*>? = null
    var verifiedIdResult: Result<VerifiedId>? = null
    var pin: String? = null

    suspend fun initiateIssuance() {
        val verifiedIdClient = VerifiedIdClientBuilder(context).build()
        // Use the test uri here
        val verifiedIdRequestUrl =
            VerifiedIdRequestURL(Uri.parse("openid-vc://?request_uri=https://verifiedid.did.msidentity.com/v1.0/tenants/9c59be8b-bd18-45d9-b9d9-082bc07c094f/verifiableCredentials/issuanceRequests/c662eff3-3b57-4a0c-ba03-ec11f6e46dde"))
        verifiedIdRequest = verifiedIdClient.createRequest(verifiedIdRequestUrl)
    }

    suspend fun completeIssuance() {
        when (val requirement = verifiedIdRequest?.requirement) {
            is GroupRequirement -> {
                val requirementsInGroup = requirement.requirements
                for (requirementInGroup in requirementsInGroup) {
                    if (requirementInGroup is PinRequirement)
                        pin?.let { requirementInGroup.fulfill(it) }
                }
                requirement.validate()
            }
        }
        verifiedIdResult = (verifiedIdRequest as ManifestIssuanceRequest).complete()
    }
}