/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests

import com.microsoft.walletlibrary.requests.contract.Contract
import com.microsoft.walletlibrary.requests.requirements.CredentialFormat
import com.microsoft.walletlibrary.requests.requirements.PinRequirement

sealed class Request(open val requester: String)

class IssuanceRequest(
    // URL of the issuer
    override val requester: String,

    // State is sent back with issuance completion response
    var state: String = ""
): Request(requester) {
    // Information such as Contract URL to indicate where to get the contract
    val credentialIssuerMetadata: List<String> = emptyList()

    // List of Contracts that can be used to issue the Verified ID
    val contracts: List<Contract> = emptyList()

    // Properties needed to display pin for request
    var pinRequirement: PinRequirement? = null

    // Properties that describe the formats of requested Verified IDs
    internal val credentialFormats: List<CredentialFormat> = emptyList()

    // Raw representation of issuance request
    internal var raw: String = ""
}