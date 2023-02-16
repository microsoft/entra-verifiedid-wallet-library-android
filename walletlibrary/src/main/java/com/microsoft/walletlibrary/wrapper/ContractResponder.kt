package com.microsoft.walletlibrary.wrapper

import com.microsoft.did.sdk.VerifiableCredentialSdk
import com.microsoft.did.sdk.credential.service.IssuanceResponse
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.walletlibrary.mappings.issuance.toVerifiedId
import com.microsoft.walletlibrary.requests.ContractIssuanceRequest
import com.microsoft.walletlibrary.requests.requirements.GroupRequirement
import com.microsoft.walletlibrary.requests.requirements.SelfAttestedClaimRequirement
import com.microsoft.walletlibrary.util.WalletLibraryException
import com.microsoft.walletlibrary.verifiedid.VerifiedId

object ContractResponder {
    internal suspend fun sendIssuanceResponse(verifiedIdRequest: ContractIssuanceRequest): VerifiedId {
        val issuanceRequest = verifiedIdRequest.request
        val issuanceResponse = issuanceRequest?.let { IssuanceResponse(it) }
        val requirement = verifiedIdRequest.requirement
        if (requirement is GroupRequirement) {
            val requirements = requirement.requirements
            for (req in requirements) {
                if (req is SelfAttestedClaimRequirement) {
                    issuanceResponse?.requestedSelfAttestedClaimMap?.set(req.claim, req.value)
                }
            }
        }
        if (issuanceResponse != null) {
            when (val result = VerifiableCredentialSdk.issuanceService.sendResponse(issuanceResponse)) {
                is Result.Success -> return result.payload.toVerifiedId()
                is Result.Failure -> throw result.payload
            }
        }
        throw WalletLibraryException("")
    }
}