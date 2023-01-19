package com.microsoft.walletlibrary

import android.content.Context
import com.microsoft.did.sdk.VerifiableCredentialSdk
import com.microsoft.did.sdk.credential.service.IssuanceResponse
import com.microsoft.walletlibrary.requests.FlowType
import com.microsoft.walletlibrary.requests.IssuanceRequest
import com.microsoft.walletlibrary.requests.Request
import com.microsoft.walletlibrary.requests.contract.Contract
import com.microsoft.walletlibrary.requests.requirements.AccessTokenRequirement
import com.microsoft.walletlibrary.requests.requirements.IdTokenRequirement
import com.microsoft.walletlibrary.requests.requirements.SelfAttestedClaimRequirement
import com.microsoft.walletlibrary.requests.requirements.VerifiedIdRequirement

class VerifiedIdFlow {

    fun init(context: Context) {
        VerifiableCredentialSdk.init(context, "testingLibrary/1.0")
    }

    suspend fun testIssuance(context: Context): String {
        init(context)
        val issuanceRequest =
            VerifiableCredentialSdk.issuanceService.getRequest("https://verifiedid.did.msidentity.com/v1.0/tenants/9c59be8b-bd18-45d9-b9d9-082bc07c094f/verifiableCredentials/contracts/OWM1OWJlOGItYmQxOC00NWQ5LWI5ZDktMDgyYmMwN2MwOTRmdmVyaWZpZWQgYnVzaW5lc3MgY2FyZA/manifest")
        if (issuanceRequest is com.microsoft.did.sdk.util.controlflow.Result.Success) {
            val issuanceResponse = IssuanceResponse(issuanceRequest.payload)
            issuanceResponse.requestedSelfAttestedClaimMap["name"] = "ng"
            issuanceResponse.requestedSelfAttestedClaimMap["company"] = "NG"
            val vc = VerifiableCredentialSdk.issuanceService.sendResponse(issuanceResponse)
            if (vc is com.microsoft.did.sdk.util.controlflow.Result.Success)
                return "${vc.payload.contents.vc.type.last()} issued by ${vc.payload.contents.iss} with claims ${vc.payload.contents.vc.credentialSubject.entries}"
        }
        return "test failed"
    }

    suspend fun initiate(requestUri: String, flowType: FlowType): Request? {
        var request: Request? = null
        when (flowType) {
            FlowType.PRESENTATION -> {}
            FlowType.ISSUANCE ->  {
                val result = VerifiableCredentialSdk.issuanceService.getRequest(requestUri)
                if (result is com.microsoft.did.sdk.util.controlflow.Result.Success) {
                    val issuanceRequest = IssuanceRequest(result.payload.entityName)
                    val accessTokenAttestations = result.payload.contract.input.attestations.accessTokens
                    val idTokenAttestations = result.payload.contract.input.attestations.idTokens
                    val selfIssuedAttestations = result.payload.contract.input.attestations.selfIssued
                    val credentialAttestations = result.payload.contract.input.attestations.presentations
                    val contract = Contract("")
                    contract.accessTokenRequirements = accessTokenAttestations.map { AccessTokenRequirement(it) }
                    contract.idTokenRequirements = idTokenAttestations.map { IdTokenRequirement(it) }
                    contract.selfAttestedClaimRequirements = listOf(SelfAttestedClaimRequirement(selfIssuedAttestations))
                    contract.verifiedIdRequirements = credentialAttestations.map { VerifiedIdRequirement(it) }
                    issuanceRequest.contracts = listOf(contract)
                    request = issuanceRequest
                }
            }
        }
        return request
    }
}