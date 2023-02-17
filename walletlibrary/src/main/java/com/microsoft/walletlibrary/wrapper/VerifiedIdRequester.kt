package com.microsoft.walletlibrary.wrapper

import com.microsoft.did.sdk.VerifiableCredentialSdk
import com.microsoft.did.sdk.credential.service.IssuanceResponse
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.walletlibrary.mappings.issuance.addRequirements
import com.microsoft.walletlibrary.mappings.issuance.toVerifiedId
import com.microsoft.walletlibrary.requests.ContractIssuanceRequest
import com.microsoft.walletlibrary.verifiedid.VerifiedId

object VerifiedIdRequester {
    internal suspend fun sendIssuanceResponse(verifiedIdRequest: ContractIssuanceRequest): VerifiedId {
        val issuanceRequest = verifiedIdRequest.request.rawRequest
        val issuanceResponse = IssuanceResponse(issuanceRequest)
        val requirement = verifiedIdRequest.requirement
        issuanceResponse.addRequirements(requirement)
        when (val result = VerifiableCredentialSdk.issuanceService.sendResponse(issuanceResponse)) {
            is Result.Success -> return result.payload.toVerifiedId()
            is Result.Failure -> throw result.payload
        }
    }
}