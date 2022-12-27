package com.microsoft.walletlibrary.requests

import com.microsoft.walletlibrary.requests.contract.Contract
import com.microsoft.walletlibrary.requests.requirements.CredentialFormat
import com.microsoft.walletlibrary.requests.requirements.PinRequirement

sealed class Request(open val requester: String)

class IssuanceRequest(override val requester: String) : Request(requester) {
    var state: String = ""
    val credentialIssuerMetadata: List<String> = mutableListOf()
    val contracts: List<Contract> = mutableListOf()
    var pinRequirement: PinRequirement? = null
    val credentialFormats: List<CredentialFormat> = mutableListOf()
}